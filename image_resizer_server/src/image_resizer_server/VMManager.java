package src.image_resizer_server;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import src.amazon.AmazonConnector;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;

/**
 *
 * @author Adam Kucera
 */
class VMManager implements Runnable {

	AmazonEC2Client amazonEC2Client = null;
	AmazonConnector amazonConnector = null;
	
	/**
	 * start the amazonEC2client and check if we need less or more machines.
	 */
	@Override
	public void run() {
		//startup the connection to amazon
		amazonConnector = new AmazonConnector(new File(
				"amazon.properties"), "amazon");
		amazonEC2Client = amazonConnector.getAmazonEC2Client();
		
		//prints the number of runninginstances every 10 seconds
		Timer timer = new Timer();
	    timer.schedule(new printNumberOfInstances(), 100, 10000); 

		while (true) {
			//TODO check if we need more or less instances
		}
	}

	private int getNumberOfRunningInstances() {
		return getInstances() != null? getInstances().size() : -1;
	}

	private List<Instance> getInstances() {
		return amazonConnector.getInstances();
	}
	
	private void addInstances(int numberOfInstances) {
		amazonConnector.runInstances(numberOfInstances);
	}
	

	class printNumberOfInstances extends TimerTask {

        public void run() {
            System.out.println("There are " + getNumberOfRunningInstances() + " instances running");
        }
    }
}
