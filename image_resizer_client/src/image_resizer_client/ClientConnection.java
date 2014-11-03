package image_resizer_client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Basic class for client connection. Handles sending and receiving.
 * @author Adam Kucera
 */
public class ClientConnection {

    private Socket socket;
    //address has to be set to the MASTER server!
    private final String server = "localhost";
    //port has to be same.
    private final int port = 4020;
    private PrintStream so;
    private BufferedInputStream si;
    //what is the filename of received file.
    private final String RECEIVED_FILE = "output";

    /**
     * Connects client to the server.
     */
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
     * Sends the file to the server.
     * inspired from http://stackoverflow.com/questions/4775617/file-uploading-downloading-between-server-client
     * almost the same as in the server application.
     * 
     * @param file file to be sent
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
            bis.close();
            socket.shutdownOutput();
            System.out.println("Done.");
        }
    }

    /**
     * Sends the parameters to the server.
     * TODO to be done
     * @param jcp parameters to be sent
     */
    public void sendParameters(JCommanderParameters jcp) {

    }

    /**
     * Closes the connection.
     * @throws IOException 
     */
    public void closeSocket() throws IOException {
        si.close();
        socket.close();
    }

    /**
     * Receives the file from the server.
     * Almost the same as in the server part.
     * @throws IOException 
     */
    public void receiveFile() throws IOException {
        System.out.println("Receiving file...");
        si = new BufferedInputStream(socket.getInputStream());
        File file = new File(RECEIVED_FILE + ".zip");
        if (si != null) {
            FileOutputStream fos = null;
            byte[] b = new byte[1];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when creating file.");
            }
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = 0;
            try {
                bytesRead = si.read(b, 0, b.length);
            } catch (IOException ex) {
                System.err.println("Error when reading first byte.");
            }

            do {
                try {
                    baos.write(b);
                } catch (IOException ex) {
                    System.err.println("Error when writing bytes.");
                }
                try {
                    bytesRead = si.read(b);
                } catch (IOException ex) {
                    System.err.println("Error when reading next bytes.");
                }
            } while (bytesRead != -1);

            try {
                bos.write(baos.toByteArray());
            } catch (IOException ex) {
                System.err.println("Error when writing buffer.");
            }
            bos.flush();
            bos.close();
        }
    }
}
