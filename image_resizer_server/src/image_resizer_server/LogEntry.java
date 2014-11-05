package image_resizer_server;

import java.util.Date;

/**
 * Class representing an Entry in the Monitor log.
 * @author Adam Kucera
 */
public class LogEntry {
    private final Date d;
    private final double processor_usage;
    private final float memory_usage;
    private final int number_of_users;

    public LogEntry(Date d, double processor_usage, float memory_usage, int number_of_users) {
        this.d = d;
        this.processor_usage = processor_usage;
        this.memory_usage = memory_usage;
        this.number_of_users = number_of_users;
    }

    public Date getD() {
        return d;
    }

    public double getProcessorUsage() {
        return processor_usage;
    }

    public float getMemoryUsage() {
        return memory_usage;
    }
    
    public int getNumberOfUsers() {
        return number_of_users;
    }
}
