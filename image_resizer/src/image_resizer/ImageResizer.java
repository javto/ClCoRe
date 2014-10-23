package image_resizer;

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
        ImageHandler handler = new ImageHandler();
        try {
            handler.readImage(args[0]);
            handler.writeResizedImage(200, 200);
        } catch (IOException ex) {
            System.out.println("Error when doing I/O operations.");
        }
    }
    
}
