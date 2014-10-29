package image_resizer;

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
    private String dir = "output";

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

    private void checkTmpDirectory(String dir) {
        File tmpDir = new File(dir);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
    }

    //TODO: should the scaling keep the aspect ratio?
    public void writeResizedImage(int width, int height, String descriptor) throws IOException {
        checkTmpDirectory(dir);
        String extension = filename.substring(filename.length() - 3);
        String resizedFilename = dir + "/" + filename.substring(0, filename.length() - 4) + "_" + descriptor + "." + extension;
        BufferedImage resized = resizeImage(width, height);
        ImageIO.write(resized, extension, new File(resizedFilename));
        System.out.println("Image " + width + "x" + height + " written into " + resizedFilename);
    }

    //using library from http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html
    public void createZIP(boolean cleanTmp) throws FileNotFoundException, IOException {
        zipdirectory.ZipDirectory.main(dir);
        if (cleanTmp) {
            delete(new File(dir));
        }
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }
}
