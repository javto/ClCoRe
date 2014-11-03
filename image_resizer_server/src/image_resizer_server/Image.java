package image_resizer_server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Object representing each image to be resized.
 * @author Adam Kucera
 */
public class Image {
    BufferedImage data;
    String filename;

    /**
     * Creates new Image object from InputStream.
     * @param is InputStream to create the image from.
     * @param filename desired filename
     * @throws IOException 
     */
    public Image(InputStream is, String filename) throws IOException {
        this.data = ImageIO.read(is);
        this.filename = filename;
    }
    
    /**
     * Creates new Image object from file
     * @param file file to create the image from
     * @throws IOException 
     */
    public Image(File file) throws IOException {
        this.data = ImageIO.read(file);
        this.filename = file.getName();
    }

    public BufferedImage getData() {
        return data;
    }

    public String getFilename() {
        return filename;
    }
    
}
