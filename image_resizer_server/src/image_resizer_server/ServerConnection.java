package image_resizer_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Adam Kucera
 */
public class ServerConnection {

    private ServerSocket socket;
    private final int port = 4020;
    private int clientsCnt;

    public ServerConnection() throws IOException {
        this.clientsCnt = 0;

        //try to start server
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.out.println("server ending");

            socket.close();
            System.exit(1);
        }

    }

    void runSlave() throws IOException {
        Socket clientSocket = null;
        //endlessly accept clients
        while (true) {
            try {
                clientSocket = socket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.out.println("server ending");

                socket.close();
                System.exit(1);
            }
            //create object for each thread
            ConnectedClientSlave client = new ConnectedClientSlave(clientsCnt++, clientSocket);
            Thread t = new Thread(client);
            t.start();
        }
    }

    void runMaster() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
