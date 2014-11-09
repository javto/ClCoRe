package image_resizer_server;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Adam Kucera
 */
public class Job implements Runnable {

    private QueueItem item;
    private String IMAGES_DIR;
    
    public Job(QueueItem item, String dir) {
        this.item = item;
        this.IMAGES_DIR = dir;
    }

    @Override
    public void run() {
        ConnectedClientSlave client;
        client = item.getClient();
        //and process it
        try {
            JobProcessor.processZip(item.getFile(), item.getParams(), client.getId());
        } catch (IOException ex) {
            System.err.println("Error when processing job for client " + client.getId());
        }
        try {
            //delete the original file
            Utils.delete(item.getFile());
            //and set which file to send to the client
            client.setFileToSend(new File(IMAGES_DIR + client.getId() + ".zip"));
        } catch (IOException ex) {
            System.err.println("Error when deleting temporary files.");
        }
        Object lock = item.getLock();

        //notify ClientConnection to continue with work
        //TODO not really sure if this works for multiple clients!!!!! have to test
        synchronized (lock) {
            lock.notify();
        }
    }

}
