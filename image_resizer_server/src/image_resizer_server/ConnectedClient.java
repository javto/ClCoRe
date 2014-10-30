package image_resizer_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Adam Kucera
 */
public class ConnectedClient implements Runnable {

    private final String RECEIVED_FILES_DIR = "received";
    private final int id; //unique id of every thread
    private final Socket socket;
    private BufferedInputStream si;

    public ConnectedClient(int id, Socket socket) throws IOException {
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
            receiveFile();
            JCommanderParameters jcp = receiveJcp();
            QueueItem item = new QueueItem(jcp, this, RECEIVED_FILES_DIR);
            QueueManager.GetInstance().enqueue(item);
            this.wait();
            sendFile();
            si.close();
            socket.close();
            return;
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
        }
    }

    private void sendFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getId() {
        return id;
    }

    private JCommanderParameters receiveJcp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
