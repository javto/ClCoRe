package image_resizer_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * Class responsible for monitoring performance of slave machines.
 *
 * @author Adam Kucera
 */
public class Monitor extends TimerTask {

    private Sigar sigar = new Sigar();
    private ArrayList<LogEntry> log;
    private static Monitor instance = null;
    private int number_of_users;
    private int instance_id;

    public static Monitor getInstance() {
        if (instance == null) {
            instance = new Monitor();
        }
        return instance;
    }

    /**
     * Initializes Monitor.
     */
    private Monitor() {
        log = new ArrayList<>();
        number_of_users = 0;
        SigarLoadMonitor.getInstance();
        Random rand = new Random();
        instance_id = rand.nextInt();
    }

    /**
     * This method is run by Timer every second to insert new performance
     * information in the log.
     */
    @Override
    public void run() {
        if (log == null) {
            log = new ArrayList<>();
        }
        double processor_usage = 0;
        double memory_usage = 0;
        try {
            SigarLoadMonitor slm = SigarLoadMonitor.getInstance();
            processor_usage = slm.getLoad();
            memory_usage = sigar.getMem().getUsedPercent();
        } catch (SigarException ex) {
            System.err.println("Error when retrieving performance data.");
        }
        log.add(new LogEntry(new Date(), processor_usage, memory_usage, number_of_users));
        //every 30 seconds, generate new log file.
        //TODO maybe we need something more effective
        if (log.size() % 5 == 0) {
            this.generateLog();
        }
        if (log.size() % 5 == 0) {
            this.serializeCurrentState();
        }

    }

    /**
     * Generates the log file from the items in log array.
     */
    private void generateLog() {
        File f = new File("log"+instance_id+".txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f);
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot open log file.");
        }
        pw.println("Date\t\tProcessor usage\t\tMemory usage\t\tNumber of users");
        for (LogEntry entry : log) {
            DateFormat format = DateFormat.getDateTimeInstance();
            String str = format.format(entry.getD());
            str = str.concat("\t\t");
            str = str.concat(String.valueOf((long) entry.getProcessorUsage()));
            str = str.concat("\t\t");
            str = str.concat(String.valueOf((long) entry.getMemoryUsage()));
            str = str.concat("\t\t");
            str = str.concat(Integer.toString(entry.getNumberOfUsers()));
            pw.println(str);
        }
        pw.close();
    }

    //from http://www.tutorialspoint.com/java/java_serialization.htm
    private void serializeCurrentState() {
        LogEntry e = this.getLastEntry();
        try {
            FileOutputStream fileOut = new FileOutputStream("logentry.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(e);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            System.err.println("Error when serializing LogEntry.");
        }
    }

    /**
     * Returns the last entry from the log.
     * @return last entry from the log
     */
    public LogEntry getLastEntry() {
        return log.get(log.size() - 1);
    }

    /**
     * Increases the number of current users.
     */
    public void increaseUsers() {
        synchronized (this) {
            number_of_users++;
        }
    }

    /**
     * Decreases the number of current users.
     */
    public void decreaseUsers() {
        synchronized (this) {
            number_of_users--;
        }
    }
}
/**
 * Class responsible for getting information about CPU.
 * @author wrent
 */
//from http://stackoverflow.com/questions/19323364/using-sigar-api-to-get-jvm-cpu-usage
    class SigarLoadMonitor {

        private static final int TOTAL_TIME_UPDATE_LIMIT = 2000;
        private static SigarLoadMonitor instance = null;

        private final Sigar sigar;
        private final int cpuCount;
        private final long pid;
        private ProcCpu prevPc;
        private double load;

        public static SigarLoadMonitor getInstance() {
            if (instance == null) {
                try {
                    instance = new SigarLoadMonitor();
                } catch (SigarException ex) {
                    System.err.println("Error when creating Sigar Monitor.");
                }
            }
            return instance;
        }
        
        private TimerTask updateLoadTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    ProcCpu curPc = sigar.getProcCpu(pid);
                    long totalDelta = curPc.getTotal() - prevPc.getTotal();
                    long timeDelta = curPc.getLastTime() - prevPc.getLastTime();
                    if (totalDelta == 0) {
                        if (timeDelta > TOTAL_TIME_UPDATE_LIMIT) {
                            load = 0;
                        }
                        if (load == 0) {
                            prevPc = curPc;
                        }
                    } else {
                        load = 100. * totalDelta / timeDelta / cpuCount;
                        prevPc = curPc;
                    }
                } catch (SigarException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        private SigarLoadMonitor() throws SigarException {
            sigar = new Sigar();
            cpuCount = sigar.getCpuList().length;
            pid = sigar.getPid();
            prevPc = sigar.getProcCpu(pid);
            load = 0;
            new Timer(true).schedule(updateLoadTask, 0, 1000);
        }

        public double getLoad() {
            return load;
        }
    }
