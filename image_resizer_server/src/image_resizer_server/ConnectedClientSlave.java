package image_resizer_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class is responsible for handling connected clients to slave machines. It
 * receives the ZIP from the user and puts it to the queue, it also sends the
 * files back.
 *
 * @author Adam Kucera
 */
public class ConnectedClientSlave implements Runnable {

    private final String RECEIVED_FILES_DIR = "received";//Where are received files stored
    private final int id; //unique id of every thread
    private final Socket socket;
    private BufferedInputStream si;
    private PrintStream so;
    private File fileToSend = null;

    /**
     * Inicializes the client connection streams.
     *
     * @param id id of the client
     * @param socket client connection socket
     * @throws IOException
     */
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

    /**
     * Main running method of each thread. At first it receives files and
     * parameters, then it puts it to the queue and waits. When the queue is
     * processed, then it sends the file back to the user.
     */
    @Override
    public void run() {
        try {
            //receive the file from the client
            System.out.println("Receiving file.");
            receiveFile();
            //System.exit(0);
            //receive the parameters from the client
            System.out.println("Receiving parameters.");
            JCommanderParameters jcp = receiveJcp();
            //put the item in the queue
            Object lock = new Object();
            QueueItem item = new QueueItem(jcp, this, RECEIVED_FILES_DIR, lock);
            QueueManager.GetInstance().enqueue(item);
            //wait until it is processed
            synchronized (lock) {
                System.out.println("Waiting...");
                //TODO im not sure if this solution is really working right now!!!
                lock.wait();
            }
            //send the file back to the client
            sendFile();
            //end the connection
            socket.close();
        } catch (IOException ex) {
            System.err.println("Error when ending connection.");
        } catch (InterruptedException ex) {
            System.err.println("Error when invoking wait operation.");
        }
    }

    /**
     * Main method responsible for receiving the file from the client. inspired
     * from
     * http://stackoverflow.com/questions/4775617/file-uploading-downloading-between-server-client
     */
    private void receiveFile() throws IOException {
        //create new file
        File file = new File(RECEIVED_FILES_DIR, id + ".zip");
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("0"); // Configured to write to myFile
        }
        if (si != null) {
            //initialize streams
            FileOutputStream fos = null;
            byte[] b = new byte[1024 * 1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when creating file " + file.getAbsolutePath());
                return;
            }
            //read the file from the socket connection
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = si.read(b, 0, b.length);
            do {
                baos.write(b, 0, bytesRead);
                bytesRead = si.read(b);
                bos.write(baos.toByteArray());
                baos.reset();
            } while (bytesRead != -1);
//            //write to the file
//            bos.write(baos.toByteArray(), bytesWritten, baos.size());
//            //close the streams
//            bos.flush();
            bos.flush();
            bos.close();
            socket.shutdownInput();
        }
    }

    /**
     * Method responsible for sending the file back to the client.
     */
    private void sendFile() {
        //initialize stream
        try {
            so = new PrintStream(socket.getOutputStream(), true);
        } catch (IOException ex) {
            System.err.println("Error when creating stream.");
        }
        System.out.println("Sending file back to the client");
        if (so != null) {
            //initialize streams
            byte[] bytearray = new byte[1024 * 1024];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fileToSend);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when reading file.");
            }
            BufferedInputStream bis = new BufferedInputStream(fis);
            try {
                int bytesRead = bis.read(bytearray);
                int bytesWritten = 0;
                do {
                    so.write(bytearray, 0, bytesRead);
                    bytesWritten += bytesRead;
                    bytesRead = bis.read(bytearray);
                } while (bytesRead != -1);

            } catch (IOException ex) {
                System.err.println("Error when sending file.");
            }
            System.out.println("Sending back.");
            //close the streams
            so.flush();
            System.out.println("Done.");
            try {
                bis.close();
            } catch (IOException ex) {
                System.err.println("Error when closing stream.");
            }
        }
        so.close();
        try {
            Utils.delete(fileToSend);
        } catch (IOException ex) {
            System.err.println("Error when deleting file.");
        }
    }

    /**
     * Get the id of the client.
     *
     * @return id of the client
     */
    public int getId() {
        return id;
    }

    /**
     * Method which receives user parameters from the client.
     *
     * @return
     */
    private JCommanderParameters receiveJcp() {
        //TODO this has to be implemented!!!
        return new JCommanderParameters();
    }

    /**
     * Sets the file to be send to the user.
     *
     * @param file file to be send
     */
    public void setFileToSend(File file) {
        this.fileToSend = file;
    }
}
