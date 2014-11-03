package image_resizer_server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Adam Kucera
 */
public class JobProcessor implements Runnable {

    //might be changed or parametrized TODO
    private static final String TMP_DIR = "tmp";
    private static final String IMAGES_DIR = "images";
    //how many images in a batch - maximum in memory
    private static final int BATCH_SIZE = 50;

    /**
     * If the file is bigger then certain treshold, we dont want to store all
     * images in memory, but we want to extract them and process them.
     *
     * TODO: similar load balancing on more machines
     */
    public void processZip(File file, JCommanderParameters jcp, int client_id) throws IOException {
        ZipHandler zip_handler = new ZipHandler(file);
        //files to be processed in memory
        if (zip_handler.getNumFiles() < BATCH_SIZE) {
            ArrayList<Image> list = zip_handler.getImagesFromZIP();
            processImages(list, jcp, client_id);
        } //files to be extracted and then read in batches
        else {
            zip_handler.extractFilesFromZIP(TMP_DIR + client_id);
            int num_files = zip_handler.getNumFiles();
            File[] files = new File(TMP_DIR + client_id).listFiles();

            ArrayList<Image> list = new ArrayList<Image>();
            for (int i = 0; i < num_files; i++) {
                Image image = new Image(files[i]);
                list.add(image);
                if (i > 0 && i - 1 % BATCH_SIZE == 0) {
                    System.out.println("Processing new batch.");
                    processImages(list, jcp, client_id);
                    list.clear();
                }
            }
            //and process the remaining- images
            System.out.println("Processing last batch.");
            processImages(list, jcp, client_id);
            Utils.delete(new File(TMP_DIR + client_id));
        }
    }

    private void processImages(ArrayList<Image> list, JCommanderParameters jcp, int client_id) throws IOException {
        ImageHandler image_handler = new ImageHandler(IMAGES_DIR + client_id);
        for (Image image : list) {
            image_handler.readImage(image);
            image_handler.writeResizedImage(jcp.twidth, jcp.theight, "t");
            image_handler.writeResizedImage(jcp.swidth, jcp.sheight, "s");
            image_handler.writeResizedImage(jcp.mwidth, jcp.mheight, "m");
            image_handler.writeResizedImage(jcp.lwidth, jcp.lheight, "l");
        }
        Utils.createZIP(IMAGES_DIR + client_id, true);
    }

    @Override
    public void run() {
        QueueManager queue = QueueManager.GetInstance();
        //check the queue and process (schedule? load balance?) items
        while (true) {
            if (queue.hasItems()) {
                QueueItem item;
                ConnectedClientSlave client;
                synchronized (this) {
                    item = queue.dequeue();
                }
                client = item.getClient();

                //TODO the assigning to proper VMs should be done here?
                //or maybe it should be in the connection and the connection will be started with the proper VM
                try {
                    processZip(item.getFile(), item.getParams(), client.getId());
                } catch (IOException ex) {
                    System.err.println("Error when processing job for client " + client.getId());
                }

                try {
                    Utils.delete(item.getFile());
                    client.setFileToSend(new File(IMAGES_DIR + client.getId()+".zip"));
                } catch (IOException ex) {
                    System.err.println("Error when deleting temporary files.");
                }
                Object lock = item.getLock();
                
                synchronized (lock) {
                   lock.notify();
                }
            }
        }

    }
}
