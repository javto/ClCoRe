package image_resizer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * inspiration from http://www.mkyong.com/java/how-to-resize-an-image-in-java/
 *
 * @author wrent
 */
public class ImageHandler {

    private BufferedImage original;
    private String filename;

    public void readImage(File image) throws IOException {
        filename = image.getName();
        original = ImageIO.read(image);
    }

    private BufferedImage resizeImage(int width, int height) {
        if (original != null) {
            BufferedImage resized = new BufferedImage(width, height, original.getType());
            Graphics2D g = resized.createGraphics();
            g.drawImage(original, 0, 0, width, height, null);
            g.dispose();

            return resized;
        } else {
            return null;
        }
    }

    //TODO: should the scaling keep the aspect ratio?
    public void writeResizedImage(int width, int height, String descriptor) throws IOException {
        String extension = filename.substring(filename.length()-3);
        String resizedFilename = filename.substring(0, filename.length()-4)+"_"+descriptor+"."+extension;
        BufferedImage resized = resizeImage(width, height);
        ImageIO.write(resized, extension, new File(resizedFilename)); 
        System.out.println("Image "+width+"x"+height+" written into "+resizedFilename);
    }
}
