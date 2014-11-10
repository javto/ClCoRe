package image_resizer_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The class responsible for accepting clients on both master and slave
 * machines.
 * 
 * @author Adam Kucera
 */
public class ServerConnection {

	private ServerSocket socket;
	// has to be same port as on the client
	private final int slavePort = 4020;
	private final int masterPort = 4019;
	private int clientsCnt;

	/**
	 * Creates new socket connection.
	 * 
	 * @throws IOException
	 */
	public ServerConnection() {
		this.clientsCnt = 0;
	}

	/**
	 * Starts the connection as the slave server. It will accept clients and
	 * receive files from them.
	 * 
	 * @throws IOException
	 */
	public void runSlave() throws IOException {
		startServer(slavePort);

		Socket clientSocket = null;
		// endlessly accept clients
		while (true) {
			try {
				clientSocket = socket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.out.println("server ending");

				socket.close();
				System.exit(1);
			}
			// create object for each thread
			ConnectedClientSlave client = new ConnectedClientSlave(
					clientsCnt++, clientSocket);
			Thread t = new Thread(client);
			t.start();
		}
	}

	/**
	 * Starts the connection as the master. It is responsible for load balancing
	 * and sending the address of the slave machine back to the clients.
	 */
	public void runMaster() throws IOException {
		startServer(masterPort);

		Socket clientSocket = null;
		// endlessly accept clients
		while (true) {
			try {
				clientSocket = socket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.out.println("server ending");

				socket.close();
				System.exit(1);
			}
			// create object for each thread
			ConnectedClientMaster client = new ConnectedClientMaster(
					clientsCnt++, clientSocket);
			Thread t = new Thread(client);
			t.start();
		}
	}

	private void startServer(int port) throws IOException {
		// try to start server
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port);
			System.out.println("server ending");
			if (socket != null) {
				socket.close();
			}
			System.exit(1);
		}
	}
}
