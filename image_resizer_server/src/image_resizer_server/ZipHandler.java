package image_resizer_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Class dealing with ZIP extracting.
 * @author Adam Kucera
 */
public class ZipHandler {

    private ZipInputStream zis;
    private ZipFile zip;

    /**
     * Initializes new ZipHandler.
     * @param file Zip to take care of.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public ZipHandler(File file) throws FileNotFoundException, IOException {
        zis = new ZipInputStream(new FileInputStream(file));
        zip = new ZipFile(file);
    }

    /**
     * Returns a list of all images in a ZIP.
     * @return Array of images
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public ArrayList<Image> getImagesFromZIP() throws FileNotFoundException, IOException {
        ZipEntry entry = zis.getNextEntry();
        ArrayList<Image> list = new ArrayList<Image>();

        while (entry != null) {
            Image image = new Image(zis, entry.getName());
            list.add(image);
            entry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        return list;
    }

    /**
     * Extracts the files from the ZIP to disk into specified output dir.
     * from http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
     * @param output_dir dir to extract the images
     * @throws java.io.FileNotFoundException
     */
    public void extractFilesFromZIP(String output_dir) throws FileNotFoundException, IOException {
        ZipEntry entry = zis.getNextEntry();

        byte[] buffer = new byte[1024];

        while (entry != null) {

            String fileName = entry.getName();
            File newFile = new File(output_dir + File.separator + fileName);

            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            entry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }
    
    /**
     * Returns the number of files in the ZIP.
     * @return number of files.
     */
    public int getNumFiles() {
        return zip.size();
    }
}
