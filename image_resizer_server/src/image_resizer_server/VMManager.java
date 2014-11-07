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
	 * Returns the list of running machines.
	 * 
	 * @return List of Running VirtualMachines.
	 */
	public ArrayList<VirtualMachine> getRunningMachines() {
		ArrayList<VirtualMachine> running = new ArrayList<>();
		for (VirtualMachine machine : machines) {
			if (machine.isRunning()) {
				running.add(machine);
			}
		}
		return running;
	}

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
		// System.out.println("start instances succeeded: "
		// + startInstances(amazonConnector.getInstanceIDsStrings()));

		List<Instance> instances = getInstances();
		for (Instance instance : instances) {
			machines.add(new VirtualMachine(instance));
		}

		while (running) {

			running = false;
		}
		System.out.println("stop instances succeeded: "
				+ stopInstances(amazonConnector.getInstanceIDsStrings()));
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
				System.out.println("image ID: " + instances.get(i).getImageId()
						+ " state: "
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
