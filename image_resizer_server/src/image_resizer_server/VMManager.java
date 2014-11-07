package image_resizer_server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import amazon.AmazonConnector;

import com.amazonaws.services.ec2.model.Instance;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * Singleton VMManager.
 *
 * @author Adam Kucera
 * @author Jaap
 */
class VMManager implements Runnable {

	private AmazonConnector amazonConnector = null;
	private ArrayList<VirtualMachine> machines;
	private static VMManager instance;
	// in miliseconds, how long should the while loop sleep per iteration:
	private static final int UPDATETIME = 100;

	// in miliseconds, how long should the while loop run (should be infinite in
	// rl, but for testing purposes):
	private static final int RUNTIME = 120000;

	// above which utilization should a new machine be started
	private static final double THRESHHOLDHIGH = 0.7;

	// under which utilization should a machine be stopped
	private static final double THRESHHOLDLOW = 0.3;

	/**
	 * Gets the singleton instance of VMManager.
	 *
	 * @return
	 */
	public static VMManager getInstance() {
		if (instance == null) {
			instance = new VMManager();
		}
		return instance;
	}

	/**
	 * Private constructor initializing array of machines.
	 */
	private VMManager() {
		machines = new ArrayList<>();
	}

	/**
	 * start the amazonEC2client and check if we need less or more machines.
	 */
	@Override
	public void run() {
		boolean running = true;
		// startup the connection to amazon
		amazonConnector = new AmazonConnector(
				new File(
						"image_resizer_server/src/image_resizer_server/amazonJaap.properties"));

		// prints the number of running instances every 10 seconds
		Timer timer = new Timer();
		timer.schedule(new PrintNumberOfInstances(), 100, 10000);
		timer.schedule(new PrintVMPool(), 100, 5000);
		System.out.println("start instances succeeded: "
				+ startInstances(amazonConnector.getInstanceIDsStrings()));

		List<Instance> instances = getInstances();
		for (Instance instance : instances) {
			machines.add(new VirtualMachine(instance));
		}
		long startTime = System.currentTimeMillis();
		long stopTime = 0;
		while (running) {
			// update machine info
			updateInstances(getInstances());

			double loadCPU = getNormalizedCPULoad();
			float loadMem = getNormalizedMemoryLoad();

			// check if a new machine needs to be started
			if (loadCPU > THRESHHOLDHIGH || loadMem > THRESHHOLDHIGH) {
				// add machine (could actually start more machines at one time
				boolean started = startMachine(1);
				if(!started) {
					System.out.println("all machines are in use and we would want more");
					//TODO: maybe send a signal that no new tasks should be accepted
				}
			}
			// check if a machine needs to be stopped
			else if (loadCPU < THRESHHOLDLOW || loadMem < THRESHHOLDLOW) {
				// stop machine with lowest load, preferably zero, otherwise
				// don't send any tasks anymore
				
			}

			try {
				Thread.sleep(UPDATETIME);
			} catch (InterruptedException e) {
				running = false;
			}

			stopTime = System.currentTimeMillis();
			if (stopTime - startTime > RUNTIME) {
				running = false;
			}
		}

		// end the thread here

		// stop instances and check if succeeded:
		System.out.println("stop instances succeeded: "
				+ stopInstances(amazonConnector.getInstanceIDsStrings()));
		// stop timed tasks
		timer.cancel();

		// thread can now be dead
	}

	/**
	 * start a machine which isn't running yet
	 * @param numberOfMachines to start
	 * @return if succeeded return true, otherwise false
	 */
	private boolean startMachine(int numberOfMachines) {
		int count = numberOfMachines;
		boolean result = false;
		for(VirtualMachine vm : machines) {
			if(!vm.isRunning()) {
				List<String> vmStarting = new ArrayList<String>();
				vmStarting.add(vm.getInstance().getInstanceId());
				result = startInstances(vmStarting);
				count--;
			} 
			if(count <= 0 ) {
				break;
			}
		}
		return result;
	}

	private double getNormalizedCPULoad() {
		double result = 0.0;
		int counter = 0;
		for (VirtualMachine vm : machines) {
			if (vm.isRunning()) {
				counter++;
				result += vm.getProcessorUsage();
			}
		}
		result /= counter;
		return result;
	}

	private float getNormalizedMemoryLoad() {
		float result = 0.0f;
		int counter = 0;
		for (VirtualMachine vm : machines) {
			if (vm.isRunning()) {
				counter++;
				result += vm.getMemoryUsage();
			}
		}
		result /= counter;
		return result;
	}

	/**
	 * update the instances in machines
	 * 
	 * @param instances
	 */
	private void updateInstances(List<Instance> instances) {
		for (Instance instance : instances) {
			for (VirtualMachine vm : machines) {
				if (vm.getInstance().getInstanceId() == instance
						.getInstanceId()) {
					vm.setInstance(instance);
					break;
				}
			}
		}

	}

	/**
	 * Basic method for load balancing, determines to which machine the job
	 * should be scheduled.
	 *
	 * @return Virtual machine
	 * @throws ImageResizerException
	 */
	// TODO: maybe this greedy policy isnt the best, we should also know, how
	// many jobs are already being processed there?
	public VirtualMachine getMachineWithLowestCPUUtilization()
			throws ImageResizerException {
		VirtualMachine vm = null;
		for (VirtualMachine machine : machines) {
			if (machine.isRunning()) {
				if (vm == null) {
					vm = machine;
				}
				if (machine.getProcessorUsage() < vm.getProcessorUsage()) {
					vm = machine;
				}
			}
		}
		if (vm == null) {
			throw new ImageResizerException("No machine is available.");
		}
		return vm;
	}

	/**
	 * Updates the performance of a machine on the given address.
	 *
	 * @param address
	 *            IP address of machine
	 * @param entry
	 *            LogEntry representing performance
	 * @throws ImageResizerException
	 */
	public void updateMachinePerformance(String address, LogEntry entry)
			throws ImageResizerException {
		VirtualMachine vm = null;
		try {
			for (VirtualMachine machine : machines) {
				if (machine.isRunning()) {
					if (machine.getAddress().equals(address)) {
						vm = machine;
						break;
					}
				}
			}
		} catch (ImageResizerException ex) {
			System.err.println(ex.getMessage());
		}
		if (vm == null) {
			throw new ImageResizerException("No machine with address "
					+ address + " is available.");
		}
		vm.updatePerformance(entry);
	}

	private int getNumberOfInstances() {
		return getInstances() != null ? getInstances().size() : -1;
	}

	private List<Instance> getInstances() {
		return amazonConnector.getInstances();
	}

	private void addInstances(int numberOfInstances) {
		amazonConnector.runInstances(numberOfInstances);
	}

	private boolean startInstances(List<String> instanceIDs) {
		return amazonConnector.startInstances(instanceIDs);
	}

	private boolean stopInstances(List<String> instanceIDs) {
		return amazonConnector.stopInstances(instanceIDs);
	}

	private Map<String, String> getInstancesStates(List<Instance> instances) {
		return amazonConnector.getInstancesStates(instances);
	}

	class PrintVMPool extends TimerTask {

		public void run() {
			for (VirtualMachine machine : machines) {
				System.out.println("machine: "
						+ machine.getInstance().getInstanceId()
						+ " isRunning: " + machine.isRunning());
			}
		}
	}

	class PrintNumberOfInstances extends TimerTask {

		public void run() {
			List<Instance> instances = getInstances();
			Map<String, String> states = getInstancesStates(instances);
			for (int i = 0; i < instances.size(); i++) {
				System.out.println("image ID: "
						+ instances.get(i).getInstanceId() + " state: "
						+ states.get(instances.get(i).getInstanceId()));
			}
		}
	}

	public ArrayList<VirtualMachine> getMachines() {
		return machines;
	}

	/**
	 * Connects to the host via SSH (using identity key) and performs given
	 * command. code from http://www.jcraft.com/jsch/examples/Shell.java.html
	 *
	 * @param host
	 *            host public IP or DNS
	 * @param ssh_key
	 *            identity key for SSH
	 * @param command
	 *            command to be performed
	 */
	public void startApplicationViaSSH(String host, String ssh_key,
			String command) {
		JSch jsch = new JSch();
		String user = "ec2-user";

		try {
			// add identity key
			jsch.addIdentity(ssh_key);
			// create new session
			Session session = jsch.getSession(user, host);
			UserInfo ui = new MyUserInfo() {
				public void showMessage(String message) {
					JOptionPane.showMessageDialog(null, message);
				}

				public boolean promptYesNo(String message) {
					Object[] options = { "yes", "no" };
					int foo = JOptionPane.showOptionDialog(null, message,
							"Warning", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options,
							options[0]);
					return foo == 0;
				}
			};
			session.setUserInfo(ui);
			// dont check key footprints
			session.setConfig("StrictHostKeyChecking", "no");
			// connect to SSH
			session.connect();

			System.out.println("Connected to the server " + host);
			// perform the command
			startSlaveApplication(session, command);
		} catch (JSchException ex) {
			System.err.println("Unable to connect to SSH.");
		}
	}

	/**
	 * Executes given command on SSH session. code from
	 * http://www.jcraft.com/jsch/examples/Exec.java.html
	 *
	 * @param session
	 *            SSH session
	 * @param command
	 *            command to be performed
	 * @throws JSchException
	 */
	private void startSlaveApplication(Session session, String command)
			throws JSchException {
		// perform the command
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);

		// get the result
		try {
			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					System.out.print(new String(tmp, 0, i));
				}

				if (channel.isClosed()) {
					if (in.available() > 0) {
						continue;
					}
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ee) {
				}
			}
		} catch (IOException ex) {
			System.err.println("Error when reading from the server.");
		}
		// end the connection
		channel.disconnect();
		session.disconnect();
	}

	/**
	 * Abstract class required for SSH communication code from
	 * http://www.jcraft.com/jsch/examples/Shell.java.html
	 */
	public static abstract class MyUserInfo implements UserInfo,
			UIKeyboardInteractive {

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptYesNo(String str) {
			return false;
		}

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public boolean promptPassphrase(String message) {
			return false;
		}

		@Override
		public boolean promptPassword(String message) {
			return false;
		}

		@Override
		public void showMessage(String message) {
		}

		@Override
		public String[] promptKeyboardInteractive(String destination,
				String name, String instruction, String[] prompt, boolean[] echo) {
			return null;
		}
	}
}
