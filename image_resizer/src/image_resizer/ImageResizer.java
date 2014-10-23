package image_resizer;

import com.beust.jcommander.JCommander;
import java.io.IOException;

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
        
        ImageHandler handler = new ImageHandler();
        try {
            handler.readImage(jcp.file);
            handler.writeResizedImage(jcp.twidth, jcp.theight, "t");
            handler.writeResizedImage(jcp.swidth, jcp.sheight, "s");
            handler.writeResizedImage(jcp.mwidth, jcp.mheight, "m");
            handler.writeResizedImage(jcp.lwidth, jcp.lheight, "l");
            
            handler.createZIP(true);
        } catch (IOException ex) {
            System.out.println("Error when performing I/O operations. Did you specify -file parameter?");
        }
    }
    
}
