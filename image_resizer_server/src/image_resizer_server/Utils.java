package image_resizer_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author wrent
 */
public class Utils {

    public static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    public static void checkTmpDirectory(String dir) {
        File tmpDir = new File(dir);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
    }
    
    //using library from http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html
    public static void createZIP(String dir, boolean cleanTmp) throws FileNotFoundException, IOException {
        zipdirectory.ZipDirectory.main(dir);
        if (cleanTmp) {
            Utils.delete(new File(dir));
        }
    }
}
