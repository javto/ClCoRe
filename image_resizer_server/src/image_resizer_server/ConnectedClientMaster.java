package image_resizer_server;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class representing a client connected to master server. Responsible for sending address of the host.
 * @author Adam Kucera
 */
public class ConnectedClientMaster implements Runnable {

    private int id;
    private Socket socket;
    private PrintWriter so;

    /**
     * Initializes new connection to client.
     * @param id id of the client
     * @param clientSocket client socket
     */
    ConnectedClientMaster(int id, Socket clientSocket) {
        this.id = id;
        this.socket = clientSocket;

        //stream inicialization
        try {
            so = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Couldn't get I/O.");
            System.exit(1);
        }
        System.out.println("Client " + id + " accepted from: " + socket.getInetAddress()
                + ":" + socket.getPort());
    }

    /**
     * Main loop responsible for sending information to the client.
     */
    @Override
    public void run() {
        try {
            String host;
            //get the machine with lowest CPU utilization
            host = VMManager.getInstance().getMachineWithLowestCPUUtilizationNoMaster().getHost();
            System.out.println("Sending host info to client: " + host);
            sendHostInfo(host);
        } catch (ImageResizerException ex) {
            System.err.println(ex.getMessage());
        } finally {
            //end the connection
            so.close();
            try {
                socket.close();
            } catch (IOException ex) {
                System.err.println("Error when closing connection.");
            }
        }
    }

    /**
     * Sends the host address to the client.
     * @param host slave address
     */
    private void sendHostInfo(String host) {
        so.print(host);
    }

}
