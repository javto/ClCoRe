package image_resizer_client;

import com.beust.jcommander.JCommander;
import java.io.IOException;

/**
 *
 * @author Adam Kucera
 */
public class ImageResizerClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JCommanderParameters jcp = new JCommanderParameters();
        new JCommander(jcp, args);

        ClientConnection connection = new ClientConnection();
        try {
            connection.sendFile(jcp.file);
            connection.sendParameters(jcp);
            connection.closeSocket();
        } catch (IOException ex) {
            System.err.println("Error when sending file to the server.");
        }
    }

}
