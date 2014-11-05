package image_resizer_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * Class responsible for monitoring performance of slave machines.
 * @author Adam Kucera
 */
public class Monitor extends TimerTask {

    private Sigar sigar = new Sigar();
    private ArrayList<LogEntry> log;
    private static Monitor instance = null;
    
    public static Monitor getInstance() {
        if(instance == null) {
            instance = new Monitor();
        }
        return instance;
    }
    /**
     * Initializes Monitor.
     */
    private Monitor() {
        log = new ArrayList<>();
    }

    /**
     * This method is run by Timer every second to insert new performance information
     * in the log.
     */
    @Override
    public void run() {
        if (log == null) {
            log = new ArrayList<>();
        }
        double processor_usage = 0;
        float memory_usage = 0;
        int number_of_users = 0;
        try {
            //TODO add also disk usage? number of users! processor value shows 0!
            processor_usage = sigar.getCpuPerc().getCombined();
            memory_usage = sigar.getMem().getActualUsed();
        } catch (SigarException ex) {
            System.err.println("Error when retrieving performance data.");
        }
        log.add(new LogEntry(new Date(), processor_usage, memory_usage, number_of_users));
        //every 30 seconds, generate new log file.
        //TODO maybe we need something more effective
        if (log.size() % 30 == 0) {
            this.generateLog();
        }
    }
    
    /**
     * Generates the log file from the items in log array.
     */
    private void generateLog() {
        File f = new File("log.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f);
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot open log file.");
        }
        pw.println("Date\t\t\t\tProcessor usage\t\t\t\tMemory usage\t\t\t\tNumber of users");
        for (LogEntry entry : log) {
            DateFormat format = DateFormat.getDateTimeInstance();
            String str = format.format(entry.getD());
            str = str.concat("\t\t\t\t\t\t");
            str = str.concat(String.valueOf((long) entry.getProcessorUsage()));
            str = str.concat("\t\t\t\t\t\t");
            str = str.concat(Float.toString(entry.getMemoryUsage()));
            str = str.concat("\t\t\t\t\t\t");
            str = str.concat(Float.toString(entry.getNumberOfUsers()));
            pw.println(str);
        }
        pw.close();
    }

    public LogEntry getLastEntry() {
        return log.get(log.size());
    }
}
