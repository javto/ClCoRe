package image_resizer_server;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Class responsible for resizing Image objects.
 * inspiration from http://www.mkyong.com/java/how-to-resize-an-image-in-java/
 *
 * @author Adam Kucera
 */
public class ImageHandler {

    private BufferedImage original;
    private String filename;
    private String dir;//output dir to save the resized images

    /**
     * Creates the new Handler for an client
     * @param dir output dir for images
     */
    public ImageHandler(String dir) {
        this.dir = dir;
    }

    /**
     * Reads an image into Handler
     * @param image Image to be resized.
     * @throws IOException 
     */
    public void readImage(Image image) throws IOException {
        filename = image.getFilename();
        original = image.getData();
    }

    /**
     * Resizes the image to given width and height.
     * @param width given width
     * @param height given height
     * @return resized BufferedImage
     */
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

    /**
     * Writes the resized image into output dir with proper descriptor.
     * @param width desired width
     * @param height desired height
     * @param descriptor desired descriptor
     * @throws IOException 
     */
    //TODO: should the scaling keep the aspect ratio?
    public void writeResizedImage(int width, int height, String descriptor) throws IOException {
        //does directory exist
        Utils.checkTmpDirectory(dir);
        //get the extension of the original file
        String extension = filename.substring(filename.length() - 3);
        String resizedFilename = dir + "/" + filename.substring(0, filename.length() - 4) + "_" + descriptor + "." + extension;
        //get resized image
        BufferedImage resized = resizeImage(width, height);
        //write new image
        ImageIO.write(resized, extension, new File(resizedFilename));
        System.out.println("Image " + width + "x" + height + " written into " + resizedFilename);
    }

}
