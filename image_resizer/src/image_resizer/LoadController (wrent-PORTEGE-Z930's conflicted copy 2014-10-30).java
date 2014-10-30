package image_resizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Adam Kucera
 */
public class LoadController {

    //might be changed or parametrized TODO
    private static final String TMP_DIR = "tmp";
    private static final String OUT_DIR = "output";
    //how many images in a batch - maximum in memory
    private static final int BATCH_SIZE = 2;

    /**
     * If the file is bigger then certain treshold, we dont want to store all
     * images in memory, but we want to extract them and process them.
     *
     * TODO: similar load balancing on more machines
     */
    public void processZip(JCommanderParameters jcp) throws IOException {
        ZipHandler zip_handler = new ZipHandler(jcp.file);
        //files to be processed in memory
        if (zip_handler.getNumFiles() < BATCH_SIZE) {
            ArrayList<Image> list = zip_handler.getImagesFromZIP();
            processImages(list, jcp);
        } //files to be extracted and then read in batches
        else {
            zip_handler.extractFilesFromZIP(TMP_DIR);
            int num_files = zip_handler.getNumFiles();
            File[] files = new File(TMP_DIR).listFiles();

            ArrayList<Image> list = new ArrayList<Image>();
            for (int i = 0; i < num_files; i++) {
                Image image = new Image(files[i]);
                list.add(image);
                if (i > 0 && i - 1 % BATCH_SIZE == 0) {
                    System.out.println("Processing new batch.");
                    processImages(list, jcp);
                    list.clear();
                }
            }
            //and process the remaining- images
            System.out.println("Processing last batch.");
            processImages(list, jcp);
            Utils.delete(new File(TMP_DIR));
        }
        Utils.createZIP(OUT_DIR, true);
    }

    private void processImages(ArrayList<Image> list, JCommanderParameters jcp) throws IOException {
        ImageHandler image_handler = new ImageHandler(OUT_DIR);
        for (Image image : list) {
            image_handler.readImage(image);
            image_handler.writeResizedImage(jcp.twidth, jcp.theight, "t");
            image_handler.writeResizedImage(jcp.swidth, jcp.sheight, "s");
            image_handler.writeResizedImage(jcp.mwidth, jcp.mheight, "m");
            image_handler.writeResizedImage(jcp.lwidth, jcp.lheight, "l");
        }
    }
}
