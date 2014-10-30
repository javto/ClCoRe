package image_resizer_server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author Adam Kucera
 */
public class Image {
    BufferedImage data;
    String filename;

    public Image(InputStream is, String filename) throws IOException {
        this.data = ImageIO.read(is);
        this.filename = filename;
    }
    
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
