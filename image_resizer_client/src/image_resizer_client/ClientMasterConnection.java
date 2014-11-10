package image_resizer_client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Adam Kucera
 */
public class ClientMasterConnection {

    private Socket socket;
    //has to be set to MASTER address!!
    private final String server = "ec2-54-148-17-73.us-west-2.compute.amazonaws.com";
    //port has to be same.
    private final int port = 4019;
    BufferedReader si;

    /**
     * Connects client to the server.
     */
    public ClientMasterConnection() {
        try {
            socket = new Socket(server, port);
            //initialize stream
            si = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + server);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + server);
            System.exit(1);
        }
    }

    public String getSlaveHost() throws IOException {
        String host = "";
        String str;
        while ((str = si.readLine()) != null) {
            host = host.concat(str);
        }
        return host;
    }
}
