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
 * The class is responsible for handling connected clients to slave machines.
 * It receives the ZIP from the user and puts it to the queue, it also sends the files back.
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
     * Main running method of each thread. At first it receives files and parameters,
     * then it puts it to the queue and waits. When the queue is processed, then it
     * sends the file back to the user.
     */
    @Override
    public void run() {
        try {
            //receive the file from the client
            System.out.println("Receiving file.");
            receiveFile();
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
     * Main method responsible for receiving the file from the client.
     * inspired from http://stackoverflow.com/questions/4775617/file-uploading-downloading-between-server-client
     */
    private void receiveFile() throws IOException {
        //create new file
        File file = new File(RECEIVED_FILES_DIR, id + ".zip");
        if (si != null) {
            //initialize streams
            FileOutputStream fos = null;
            byte[] b = new byte[1];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when creating file.");
                return;
            }
            //read the file from the socket connection
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = si.read(b, 0, b.length);
            do {
                baos.write(b);
                bytesRead = si.read(b);
            } while (bytesRead != -1);
            //write to the file
            bos.write(baos.toByteArray());
            //close the streams
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
            byte[] bytearray = new byte[(int) fileToSend.length()];
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fileToSend);
            } catch (FileNotFoundException ex) {
                System.err.println("Error when reading file.");
            }
            BufferedInputStream bis = new BufferedInputStream(fis);

            //read the file
            try {
                bis.read(bytearray, 0, bytearray.length);
            } catch (IOException ex) {
                System.err.println("Error when sending file.");
            }
            System.out.println("Sending (" + bytearray.length + " bytes)");
            //and send it to the client
            so.write(bytearray, 0, bytearray.length);
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
    }

    /**
     * Get the id of the client.
     * @return id of the client
     */
    public int getId() {
        return id;
    }

    /**
     * Method which receives user parameters from the client.
     * @return 
     */
    private JCommanderParameters receiveJcp() {
        //TODO this has to be implemented!!!
        return new JCommanderParameters();
    }

    /**
     * Sets the file to be send to the user.
     * @param file file to be send
     */
    public void setFileToSend(File file) {
        this.fileToSend = file;
    }
}
