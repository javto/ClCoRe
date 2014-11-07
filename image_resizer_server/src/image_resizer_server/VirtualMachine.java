package image_resizer_server;

import com.amazonaws.services.ec2.model.Instance;

/**
 *
 * @author Adam Kucera
 */
public class VirtualMachine {
	
	private Instance instance = null;

    public Instance getInstance() {
		return instance;
	}

	public VirtualMachine(Instance instance) {
        this.instance = instance;
    }

    public boolean isRunning() {
        return instance.getState().getCode() == 16;
    }

    public double getProcessorUsage() {
        if (isRunning()) {
            //TODO: this is wrong! This asks the Monitor of the master, but it has to connect via SSH to instance
            //and ask the monitor there!!
            //or maybe via socket??? connect as a special client???
            //or maybe the best solution is to send the information to the master every few seconds!
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
    
    public String getHost() throws ImageResizerException {
        if(instance == null) {
            throw new ImageResizerException("Instance is not available.");
        }
        return instance.getPublicDnsName();
    }

}
