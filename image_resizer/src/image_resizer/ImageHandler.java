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

    public void readImage(String image) throws IOException {
        filename = image;
        original = ImageIO.read(new File(image));
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

    public void writeResizedImage(int width, int height) throws IOException {
        BufferedImage resized = resizeImage(width, height);
        ImageIO.write(resized, filename.substring(filename.length()-3), new File("resized.jpg")); 
    }
}
