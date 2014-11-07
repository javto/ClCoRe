package image_resizer_server;

import com.amazonaws.services.ec2.model.Instance;
import java.util.Date;

/**
 * Class representing Virtual machine and its performance.
 * @author Adam Kucera
 */
public class VirtualMachine {

    private Instance instance = null;
    private LogEntry entry = null;

    /**
     * Initialize new machine.
     * @param instance Amazon instance.
     */
    public VirtualMachine(Instance instance) {
        this.instance = instance;
        this.entry = new LogEntry(new Date(), 0, 0, 0);
    }

    /**
     * Determines if the machine is running.
     * @return boolean
     */
    public boolean isRunning() {
        return instance.getState().getCode() == 16;
    }

    /**
     * Gets the processor usage of the machine.
     * @return Processor usage.
     */
    public double getProcessorUsage() {
        if (isRunning()) {
            return entry.getProcessorUsage();
        } else {
            return -1;
        }
    }

    /**
     * Gets the memory usage of the machine.
     * @return Memory usage
     */
    public float getMemoryUsage() {
        if (isRunning()) {
            return entry.getMemoryUsage();
        } else {
            return -1;
        }
    }

    /**
     * Gets the number of current users of the machine.
     * @return Number of users.
     */
    public int getNumberOfUser() {
        if (isRunning()) {
            return entry.getNumberOfUsers();
        } else {
            return -1;
        }
    }
    
    /**
     * Gets the public DNS name of the machine
     * @return Public DNS name.
     * @throws ImageResizerException 
     */
    public String getHost() throws ImageResizerException {
        if(instance == null) {
            throw new ImageResizerException("Instance is not available.");
        }
        return instance.getPublicDnsName();
    }
    
   /**
    * Gets the public IP address of the machine.
    * @return IP address
    * @throws ImageResizerException 
    */
   public String getAddress() throws ImageResizerException {
        if(instance == null) {
            throw new ImageResizerException("Instance is not available.");
        }
        return instance.getPublicIpAddress();
    }

    /**
     * Updates the machine with new performance data.
     * @param entry LogEntry
     */
    public void updatePerformance(LogEntry entry) {
        this.entry = entry;
    }
}
