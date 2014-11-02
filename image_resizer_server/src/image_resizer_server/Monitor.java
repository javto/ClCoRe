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
 *
 * @author Adam Kucera
 */
public class Monitor extends TimerTask {

    private Sigar sigar = new Sigar();
    private ArrayList<LogEntry> log;

    /*
     CPUperc - actual percentage value? Have to investigate more
     Mem
     FileSystemUsage
     */
    public void Monitor() {
        log = new ArrayList<>();
    }

    @Override
    public void run() {
        if (log == null) {
            log = new ArrayList<>();
        }
        double processor_usage = 0;
        float memory_usage = 0;
        try {
            processor_usage = sigar.getCpuPerc().getCombined();
            memory_usage = sigar.getMem().getActualUsed();
        } catch (SigarException ex) {
            System.err.println("Error when retrieving performance data.");
        }
        log.add(new LogEntry(new Date(), processor_usage, memory_usage));
        if (log.size() % 30 == 0) {
            this.generateLog();
        }
    }

    public void generateLog() {
        File f = new File("log.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f);
        } catch (FileNotFoundException ex) {
            System.err.println("Cannot open log file.");
        }
        pw.println("Date\t\t\t\t\t\tProcessor usage\t\t\t\t\t\tMemory usage");
        for (LogEntry entry : log) {
            DateFormat format = DateFormat.getDateTimeInstance();
            String str = format.format(entry.getD());
            str = str.concat("\t\t\t\t\t\t");
            str = str.concat(String.valueOf((long) entry.getProcessor_usage()));
            str = str.concat("\t\t\t\t\t\t");
            str = str.concat(Float.toString(entry.getMemory_usage()));
            pw.println(str);
        }
        pw.close();
    }

}
