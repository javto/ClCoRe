package image_resizer_server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class responsible for processing jobs in the queue on the slave machine.
 *
 * @author Adam Kucera
 */
public class JobProcessor implements Runnable {

    //might be changed or parametrized TODO
    private static final String TMP_DIR = "tmp";
    private static final String IMAGES_DIR = "images";
    //how many images in a batch - maximum in memory
    private static final int BATCH_SIZE = 10;

    /**
     * If the file is bigger then certain treshold, we dont want to store all
     * images in memory, but we want to extract them and process them. This
     * method decides, how will the ZIP be processed - directly in memory or
     * from disk.
     *
     * @param file file to process
     * @param jcp parameters to apply
     * @param client_id ID of the client to process
     * @throws java.io.IOException
     */
    public static void processZip(File file, JCommanderParameters jcp, int client_id) throws IOException {
        System.out.println("Processing zip "+ file.getName());
        ZipHandler zip_handler = new ZipHandler(file);
        //files to be processed in memory
        if (zip_handler.getNumFiles() < BATCH_SIZE) {
            //put all images to memory
            ArrayList<Image> list = zip_handler.getImagesFromZIP();
            //and process them
            processImages(list, jcp, client_id);
            //and create ZIP
            Utils.createZIP(IMAGES_DIR + client_id, true);
        } //files to be extracted and then read in batches
        else {
            //extract files
            zip_handler.extractFilesFromZIP(TMP_DIR + client_id);

            int num_files = zip_handler.getNumFiles();
            File[] files = new File(TMP_DIR + client_id).listFiles();
            ArrayList<Image> list = new ArrayList<>();
            //put files in the memory in batches and process these batches
            for (int i = 0; i < num_files; i++) {
                Image image = new Image(files[i]);
                list.add(image);
                if (i > 0 && i % BATCH_SIZE == 0) {
                    System.out.println("Processing new batch "+i / BATCH_SIZE);
                    processImages(list, jcp, client_id);
                    list.clear();
                }
            }
            //and process the remaining- images
            System.out.println("Processing last batch.");
            processImages(list, jcp, client_id);
            //and create ZIP
            Utils.createZIP(IMAGES_DIR + client_id, true);
            Utils.delete(new File(TMP_DIR + client_id));
        }
    }

    /**
     * The method will create 4 different images from the origininal images in
     * list
     *
     * @param list list of images to process
     * @param jcp parameters to use
     * @param client_id client ID to use
     * @throws IOException
     */
    private static void processImages(ArrayList<Image> list, JCommanderParameters jcp, int client_id) throws IOException {
        ImageHandler image_handler = new ImageHandler(IMAGES_DIR + client_id);
        for (Image image : list) {
            image_handler.readImage(image);
            image_handler.writeResizedImage(jcp.twidth, jcp.theight, "t");
            image_handler.writeResizedImage(jcp.swidth, jcp.sheight, "s");
            image_handler.writeResizedImage(jcp.mwidth, jcp.mheight, "m");
            image_handler.writeResizedImage(jcp.lwidth, jcp.lheight, "l");
        }
    }

    /**
     * Method gets the ZIP file from the queue and processes it.
     */
    @Override
    public void run() {
        QueueManager queue = QueueManager.GetInstance();
        while (true) {
            if (queue.hasItems()) {
                QueueItem item;
                //get the item from the queue
                synchronized (this) {
                    item = queue.dequeue();
                }
                Thread job_thread = new Thread(new Job(item, IMAGES_DIR));
                job_thread.start();
            }
        }

    }
}
