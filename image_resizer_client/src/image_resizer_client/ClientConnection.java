package image_resizer_client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Adam Kucera
 */
public class ClientConnection {

    private Socket socket;
    private final String server = "localhost";
    private final int port = 4020;
    private PrintStream so;
    private BufferedInputStream si;

    public ClientConnection() {
        try {
            socket = new Socket(server, port);
            //initialize streams
            so = new PrintStream(socket.getOutputStream(), true);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + server);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + server);
            System.exit(1);
        }
    }

    /**
     * From
     * http://stackoverflow.com/questions/4775617/file-uploading-downloading-between-server-client
     *
     * @param file
     * @throws IOException
     */
    public void sendFile(File file) throws IOException {
        if (so != null) {
            byte[] bytearray = new byte[(int) file.length()];
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when reading file.");
            }
            BufferedInputStream bis = new BufferedInputStream(fis);

            try {
                bis.read(bytearray, 0, bytearray.length);
            } catch (IOException ex) {
                System.err.println("Error when sending file.");
            }
            System.out.println("Sending (" + bytearray.length + " bytes)");
            so.write(bytearray, 0, bytearray.length);
            so.flush();
            System.out.println("Done.");
            bis.close();
        }
    }

    public void sendParameters(JCommanderParameters jcp) {

    }

    public void closeSocket() throws IOException {
        so.close();
        socket.close();
    }
}
