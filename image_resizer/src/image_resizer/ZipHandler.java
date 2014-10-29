package image_resizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Adam Kucera
 */
public class ZipHandler {
    private ZipInputStream zis;
    
    public ArrayList<Image> getImagesFromZIP(File zip) throws FileNotFoundException, IOException {
        readZIP(zip);
        ZipEntry entry = zis.getNextEntry();
        ArrayList<Image> list = new ArrayList<Image>();
        
        while(entry != null) {
           Image image = new Image(zis, entry.getName());
           list.add(image);
           entry = zis.getNextEntry();
        }
        return list;
    }
    
    public void extractFilesFromZIP(File zip, String output_dir) {
        
    }
    
    private void readZIP(File zip) throws FileNotFoundException {
        zis = new ZipInputStream(new FileInputStream(zip));
    }
}
