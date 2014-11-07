package image_resizer_server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adam Kucera
 */
public class ConnectedClientMaster implements Runnable {

    private int id;
    private Socket socket;
    private PrintWriter so;

    ConnectedClientMaster(int id, Socket clientSocket) {
        this.id = id;
        this.socket = clientSocket;

        //streams inicialization
        try {
            so = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Couldn't get I/O.");
            System.exit(1);
        }
        System.out.println("Client " + id + " accepted from: " + socket.getInetAddress()
                + ":" + socket.getPort());
    }

    @Override
    public void run() {
        try {
            String host;
            //host = VMManager.getInstance().getMachineWithLowestCPUUtilization().getHost();
            host = "localhost";
            System.out.println("Sending host info to client: " + host);
            sendHostInfo(host);
        } //catch (ImageResizerException ex) {
        //System.err.println(ex.getMessage());
        //} 
        finally {
            //end the connection
            so.close();
            try {
                socket.close();
            } catch (IOException ex) {
                System.err.println("Error when closing connection.");
            }
        }
    }

    private void sendHostInfo(String host) {
        so.print(host);
    }

}