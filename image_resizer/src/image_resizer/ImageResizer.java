package image_resizer;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Adam Kucera
 */
public class ImageResizer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JCommanderParameters jcp = new JCommanderParameters();
        new JCommander(jcp, args);

        ImageHandler image_handler = new ImageHandler();
        ZipHandler zip_handler = new ZipHandler();
        try {
            ArrayList<Image> list = zip_handler.getImagesFromZIP(jcp.file);
            for (Image image : list) {
                image_handler.readImage(image);
                image_handler.writeResizedImage(jcp.twidth, jcp.theight, "t");
                image_handler.writeResizedImage(jcp.swidth, jcp.sheight, "s");
                image_handler.writeResizedImage(jcp.mwidth, jcp.mheight, "m");
                image_handler.writeResizedImage(jcp.lwidth, jcp.lheight, "l");
            }
            image_handler.createZIP(true);
        } catch (IOException ex) {
            System.out.println("Error when performing I/O operations. Did you specify -file parameter?");
        }
    }

}
