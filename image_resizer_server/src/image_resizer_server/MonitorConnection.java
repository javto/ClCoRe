package image_resizer_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class running on a master server, it runs server socket for receiving
 * information from slave Monitors and updates Virtual Machines performance.
 * 
 * @author Adam Kucera
 */
public class MonitorConnection implements Runnable {

    private ServerSocket socket;
    //has to be same port as on the client
    private final int port = 4021;
    private int clientsCnt;

    /**
     * Creates new socket connection.
     */
    public MonitorConnection() throws IOException {
        this.clientsCnt = 0;
        //try to start server
        try {
            socket = new ServerSocket(port, 100);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.out.println("server ending");

            socket.close();
        }
    }

    /**
     * Main thread loop, endlessly accepts clients.
     */
    @Override
    public void run() {
        
        //endlessly accept clients
        while (true) {
            Socket clientSocket = null;
            try {
                
                clientSocket = socket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.out.println("server ending");

                try {
                    socket.close();
                } catch (IOException ex) {
                    System.err.println("Error when closing socket.");
                }
            }
            //create object for each thread
            ConnectedMonitor client = new ConnectedMonitor(clientsCnt++, clientSocket);
            Thread t = new Thread(client);
            t.start();
        }
    }

}
