package image_resizer_client;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

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
        
        DateFormat format = DateFormat.getDateTimeInstance();
        String str = format.format(new Date());
        System.out.println(str+" Client "+jcp.output.getName()+" started.");
        //get host from the master server
        ClientMasterConnection masterConnection = new ClientMasterConnection();
        String host = "";
        try {
            host = masterConnection.getSlaveHost();
        } catch (IOException ex) {
            System.err.println("Error when receiving slave host from the master server.");
        }
        System.out.println("Received slave server host: "+host);
        //send file
        ClientSlaveConnection slaveConnection = new ClientSlaveConnection(host);
        try {
            slaveConnection.sendFile(jcp.file);
            slaveConnection.sendParameters(jcp);
        } catch (IOException ex) {
            System.err.println("Error when sending file to the slave server.");
            System.exit(1);
        }
        //and wait for receive
        try {
            slaveConnection.receiveFile(jcp.output);
        } catch (IOException ex) {
            System.err.println("Error when receiving file from the slave server"+ex.getMessage());
            System.exit(1);
        }
        //close socket
        try {
            slaveConnection.closeSocket();
        } catch (IOException ex) {
            System.err.println("Error when closing connection.");
        }
        str = format.format(new Date());
        System.out.println(str+" Client "+jcp.output.getName()+" ended.");
    }

}
