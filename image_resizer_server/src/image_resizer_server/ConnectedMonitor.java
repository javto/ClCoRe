package image_resizer_server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class representing connected slave monitor, it receives the log entries from slaves
 * and updates Virtual Machines performance.
 * @author Adam Kucera
 */
public class ConnectedMonitor implements Runnable {

    private final int id; //unique id of every thread
    private final Socket socket;
    private BufferedInputStream si;
    private LogEntry entry;

    /**
     * Initializes the connected monitor.
     * @param id client id 
     * @param socket client socket
     */
    public ConnectedMonitor(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
        this.entry = null;
        //streams inicialization
        try {
            si = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Couldn't get I/O.");
        }
    }

    /**
     * Main method, tries to receive the LogEntry and then update the machine 
     * and close connection.
     */
    @Override
    public void run() {

        try {
            //receive the file from the client
            receiveLogEntry();
        } catch (IOException ex) {
            System.err.println("Error when receiving LogEntry.");
        } catch (ClassNotFoundException ex) {
            System.err.println("Error when receiving LogEntry.");
        }

        try {
            //update virtual machine information
            updateVM(socket.getInetAddress());

            si.close();
            socket.close();
        } catch (IOException ex) {
            System.err.println("Error when closing connection.");
        }

    }

    /**
     * Receives the LogEntry from the input stream.
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private void receiveLogEntry() throws IOException, ClassNotFoundException {
        if (si != null) {
            try (ObjectInputStream ois = new ObjectInputStream(si)) {
                entry = (LogEntry) ois.readObject();
                ois.close();
            }
        }
    }

    /**
     * Updates the machine with the given address.
     * @param address IP address of the machine.
     */
    private void updateVM(InetAddress address) {
        try {
            VMManager.getInstance().updateMachinePerformance(address.getHostAddress(), entry);
        } catch (ImageResizerException ex) {
            System.err.println(ex.getMessage());
        }
    }

}
