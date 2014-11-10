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
 *
 * @author Adam Kucera
 */
public class ClientSlaveConnection {

    private Socket socket;
    private String server;
    //port has to be same.
    private final int port = 4020;
    private PrintStream so;
    private BufferedInputStream si;

    /**
     * Connects client to the server.
     */
    public ClientSlaveConnection(String host) {
        server = host;
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
     * Sends the file to the server. inspired from
     * http://stackoverflow.com/questions/4775617/file-uploading-downloading-between-server-client
     * almost the same as in the server application.
     *
     * @param file file to be sent
     * @throws IOException
     */
    public void sendFile(File file) throws IOException {
        System.out.println("Sending file "+file.getName());
        if (so != null) {
            byte[] bytearray = new byte[1024 * 1024];
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when reading file.");
            }
            BufferedInputStream bis = new BufferedInputStream(fis);
            int bytesRead = bis.read(bytearray);
            int bytesWritten = 0;
            try {
                do {
                    so.write(bytearray, 0, bytesRead);
                    bytesWritten += bytesRead;
                    bytesRead = bis.read(bytearray);
                } while (bytesRead != -1);

            } catch (IOException ex) {
                System.err.println("Error when sending file.");
            }

            so.flush();
            bis.close();
            socket.shutdownOutput();
            System.out.println("Done.");
        }
    }

    /**
     * Sends the parameters to the server. TODO to be done
     *
     * @param jcp parameters to be sent
     */
    public void sendParameters(JCommanderParameters jcp) {

    }

    /**
     * Closes the connection.
     *
     * @throws IOException
     */
    public void closeSocket() throws IOException {
        si.close();
        socket.close();
    }

    /**
     * Receives the file from the server. Almost the same as in the server part.
     *
     * @throws IOException
     */
    public void receiveFile(File file) throws IOException {
        System.out.println("Waiting for files to be processed and receiving file...");
        si = new BufferedInputStream(socket.getInputStream());
        if (si != null) {
            FileOutputStream fos = null;
            byte[] b = new byte[1024 * 1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when creating file.");
            }
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = si.read(b, 0, b.length);
            do {
                baos.write(b, 0, bytesRead);
                bytesRead = si.read(b);
                bos.write(baos.toByteArray());
                baos.reset();
            } while (bytesRead != -1);
            bos.flush();
            bos.close();
            System.out.println("File "+file.getName()+" received.");
        }
    }
}
