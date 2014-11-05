package image_resizer_server;

import com.amazonaws.services.ec2.model.Instance;

/**
 *
 * @author Adam Kucera
 */
public class VirtualMachine {

    private Instance instance = null;
    private boolean available = false;

    public VirtualMachine(Instance instance) {
        this.instance = instance;

        available = instance.getState().getCode() == 16;
    }

    public boolean isRunning() {
        return available;
    }

    public double getProcessorUsage() {
        if (isRunning()) {
            return Monitor.getInstance().getLastEntry().getProcessorUsage();
        } else {
            return -1;
        }
    }

    public float getMemoryUsage() {
        if (isRunning()) {
            return Monitor.getInstance().getLastEntry().getMemoryUsage();
        } else {
            return -1;
        }
    }

    public int getNumberOfUser() {
        if (isRunning()) {
            return Monitor.getInstance().getLastEntry().getNumberOfUsers();
        } else {
            return -1;
        }
    }

}
