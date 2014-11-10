package image_resizer_server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.TimerTask;

/**
 * Class representing a connection of slave Monitor, is responsible for sending
 * LogEntries to master.
 * 
 * @author Adam Kucera
 */
public class MonitorClientConnection extends TimerTask {

	private Socket socket;
	// has to be set on Master server!
	private final String server = "ec2-54-148-17-73.us-west-2.compute.amazonaws.com";
	// port has to be same.
	private final int port = 4021;
	private PrintStream so;

	/**
	 * Sends the file to the server. inspired from
	 * http://stackoverflow.com/questions
	 * /4775617/file-uploading-downloading-between-server-client almost the same
	 * as in the server application.
	 *
	 * @param file
	 *            file to be sent
	 * @throws IOException
	 */
	public void sendFile(File file) throws IOException {
		if (so != null) {
			byte[] bytearray = new byte[(int) file.length()];
			FileInputStream fis = null;

			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException ex) {
				System.err.println("Error when reading file.");
			}
			BufferedInputStream bis = new BufferedInputStream(fis);

			try {
				bis.read(bytearray, 0, bytearray.length);
			} catch (IOException ex) {
				System.err.println("Error when sending file.");
			}
			so.write(bytearray, 0, bytearray.length);
			so.flush();
			bis.close();
		}
	}

	/**
	 * Main loop, it gets the serialized LogEntry object and sends it.
	 */
	@Override
	public void run() {
		// new connection every 5 seconds
		try {
			socket = new Socket(server, port);
			so = new PrintStream(socket.getOutputStream(), true);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + server);
		} catch (IOException ex) {
			System.err.println("Error when getting I/O.");
		}

		// if there is no serialized object, just create dummy one
		File file = new File("logentry.ser");
		if (!file.exists()) {
			LogEntry e = new LogEntry(new Date(), 0, 0, 0);
			try {
				FileOutputStream fileOut = new FileOutputStream("logentry.ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(e);
				out.close();
				fileOut.close();
			} catch (IOException i) {
				System.err.println("Error when serializing LogEntry.");
			}
		}
		try {
			this.sendFile(file);
		} catch (IOException ex) {
			System.err.println("Error when sending file.");
		}
		if (so != null) {
			so.close();
		}
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException ex) {
			System.err.println("Error when closing connection.");
		}
	}

}
