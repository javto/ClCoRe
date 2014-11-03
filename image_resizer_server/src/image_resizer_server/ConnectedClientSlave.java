package image_resizer_server;

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

/**
 *
 * @author Adam Kucera
 */
public class ConnectedClientSlave implements Runnable {

    private final String RECEIVED_FILES_DIR = "received";
    private final int id; //unique id of every thread
    private final Socket socket;
    private BufferedInputStream si;
    private PrintStream so;
    private File fileToSend = null;

    public ConnectedClientSlave(int id, Socket socket) throws IOException {
        this.id = id;
        this.socket = socket;

        //streams inicialization
        try {
            si = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Couldn't get I/O.");
            System.exit(1);
        }
        System.out.println("Client " + id + " accepted from: " + socket.getInetAddress()
                + ":" + socket.getPort());
    }

    //main running method of each thread
    @Override
    public void run() {
        try {
            System.out.println("Receiving file.");
            receiveFile();
            System.out.println("Receiving parameters.");
            JCommanderParameters jcp = receiveJcp();
            Object lock = new Object();
            QueueItem item = new QueueItem(jcp, this, RECEIVED_FILES_DIR, lock);
            QueueManager.GetInstance().enqueue(item);
            synchronized (lock) {
                System.out.println("Waiting...");
                lock.wait();
            }
            sendFile();
            socket.close();
        } catch (IOException ex) {
            System.err.println("Error when ending connection.");
        } catch (InterruptedException ex) {
            System.err.println("Error when invoking wait operation.");
        }
    }

    /**
     * from
     * http://stackoverflow.com/questions/4775617/file-uploading-downloading-between-server-client
     */
    private void receiveFile() throws IOException {
        File file = new File(RECEIVED_FILES_DIR, id + ".zip");
        if (si != null) {
            FileOutputStream fos = null;
            byte[] b = new byte[1];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when creating file.");
                return;
            }
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = si.read(b, 0, b.length);

            do {
                baos.write(b);
                bytesRead = si.read(b);
            } while (bytesRead != -1);
            bos.write(baos.toByteArray());
            bos.flush();
            bos.close();

            socket.shutdownInput();
        }
    }

    private void sendFile() {
        try {
            so = new PrintStream(socket.getOutputStream(), true);
        } catch (IOException ex) {
            System.err.println("Error when creating stream.");
        }
        System.out.println("Sending file back to the client");
        if (so != null) {
            byte[] bytearray = new byte[(int) fileToSend.length()];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fileToSend);
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
            try {
                bis.close();
            } catch (IOException ex) {
                System.err.println("Error when closing stream.");
            }
        }
        so.close();
    }

    public int getId() {
        return id;
    }

    private JCommanderParameters receiveJcp() {
        //TODO this has to be implemented!!!
        return new JCommanderParameters();
    }

    void setFileToSend(File file) {
        this.fileToSend = file;
    }
}
