package image_resizer_server;

import com.amazonaws.services.ec2.model.Instance;
import java.util.Date;

/**
 * Class representing Virtual machine and its performance.
 *
 * @author Adam Kucera
 */
public class VirtualMachine {

	public enum Sort {
		master, slave_perm, slave
	};

	private Instance instance = null;
	private LogEntry entry = null;
	private boolean shutdown = false;
	private Sort sort = null;

	public enum AppRun {
		yes, pending, no
	};

	private AppRun applicationRunning = null;

	public AppRun getApplicationRunning() {
		return applicationRunning;
	}

	public void setApplicationRunning(AppRun applicationRunning) {
		this.applicationRunning = applicationRunning;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	public Instance getInstance() {
		return instance;
	}

	public Sort getSort() {
		return sort;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	/**
	 * Initialize new machine.
	 *
	 * @param instance
	 *            Amazon instance.
	 */
	public VirtualMachine(Instance instance, Sort sort) {
		this.instance = instance;
		this.entry = new LogEntry(new Date(), 0, 0, 0);
		this.sort = sort;
		this.applicationRunning = AppRun.no;
	}

	/**
	 * Determines if the machine is running.
	 *
	 * @return boolean
	 */
	public boolean isRunning() {
		return instance.getState().getCode() == 16;
	}

	/**
	 * Gets the processor usage of the machine.
	 *
	 * @return Processor usage.
	 */
	public double getProcessorUsage() {
		if (isRunning() && entry != null) {
			return entry.getProcessorUsage();
		} else {
			return -1;
		}
	}

	/**
	 * Gets the memory usage of the machine.
	 *
	 * @return Memory usage
	 */
	public double getMemoryUsage() {
		if (isRunning() && entry != null) {
			return entry.getMemoryUsage();
		} else {
			return -1;
		}
	}

	/**
	 * Gets the number of current users of the machine.
	 *
	 * @return Number of users.
	 */
	public int getNumberOfUser() {
		if (isRunning() && entry != null) {
			return entry.getNumberOfUsers();
		} else {
			return -1;
		}
	}

	/**
	 * Gets the public DNS name of the machine
	 *
	 * @return Public DNS name.
	 * @throws ImageResizerException
	 */
	public String getHost() throws ImageResizerException {
		if (instance == null) {
			throw new ImageResizerException("Instance is not available.");
		}
		return instance.getPublicDnsName();
	}

	/**
	 * Gets the public IP address of the machine.
	 *
	 * @return IP address
	 * @throws ImageResizerException
	 */
	public String getAddress() throws ImageResizerException {
		if (instance == null) {
			throw new ImageResizerException("Instance is not available.");
		}
		return instance.getPrivateIpAddress();
	}

	/**
	 * Updates the machine with new performance data.
	 *
	 * @param entry
	 *            LogEntry
	 */
	public void updatePerformance(LogEntry entry) {
		this.entry = entry;
	}
}
