package image_resizer_client;

import com.beust.jcommander.JCommander;
import java.io.IOException;

/**
 * Main class for client, it starts the connection.
 * @author Adam Kucera
 */
public class ImageResizerClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //get the parameters
        JCommanderParameters jcp = new JCommanderParameters();
        new JCommander(jcp, args);

        //send file
        ClientConnection connection = new ClientConnection();
        try {
            connection.sendFile(jcp.file);
            connection.sendParameters(jcp);
        } catch (IOException ex) {
            System.err.println("Error when sending file to the server.");
            System.exit(1);
        }
        //and wait for receive
        try {
            connection.receiveFile();
        } catch (IOException ex) {
            System.err.println("Error when receiving file from the server"+ex.getMessage());
            System.exit(1);
        }
        //close socket
        try {
            connection.closeSocket();
        } catch (IOException ex) {
            System.err.println("Error when closing connection.");
        }
    }

}
