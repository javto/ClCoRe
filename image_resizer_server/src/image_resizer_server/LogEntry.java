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

    public LogEntry(Date d, double processor_usage, float memory_usage) {
        this.d = d;
        this.processor_usage = processor_usage;
        this.memory_usage = memory_usage;
    }

    public Date getD() {
        return d;
    }

    public double getProcessor_usage() {
        return processor_usage;
    }

    public float getMemory_usage() {
        return memory_usage;
    }
}
