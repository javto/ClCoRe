package image_resizer_server;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * inspiration from http://www.mkyong.com/java/how-to-resize-an-image-in-java/
 *
 * @author Adam Kucera
 */
public class ImageHandler {

    private BufferedImage original;
    private String filename;
    private String dir;

    public ImageHandler(String dir) {
        this.dir = dir;
    }

    public void readImage(Image image) throws IOException {
        filename = image.getFilename();
        original = image.getData();
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
        Utils.checkTmpDirectory(dir);
        String extension = filename.substring(filename.length() - 3);
        String resizedFilename = dir + "/" + filename.substring(0, filename.length() - 4) + "_" + descriptor + "." + extension;
        BufferedImage resized = resizeImage(width, height);
        ImageIO.write(resized, extension, new File(resizedFilename));
        System.out.println("Image " + width + "x" + height + " written into " + resizedFilename);
    }

}
