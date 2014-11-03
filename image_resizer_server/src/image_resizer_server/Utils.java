package image_resizer_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class providing some useful functions, mainly working with files.
 * @author Adam Kucera
 */
public class Utils {

    /**
     * Deletes a file or a whole directory.
     * @param f file or dir to delete.
     * @throws IOException 
     */
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

    /**
     * Checks if a directory exists and otherwise it creates it.
     * @param dir directory to check 
     */
    public static void checkTmpDirectory(String dir) {
        File tmpDir = new File(dir);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
    }
    
    /**
     * Creates ZIP from specified directory.
     * using library from http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html
     * @param dir Directory to ZIP
     * @param cleanTmp Should the directory be deleted afterwards?
     * @throws java.io.FileNotFoundException
     */
    public static void createZIP(String dir, boolean cleanTmp) throws FileNotFoundException, IOException {
        zipdirectory.ZipDirectory.main(dir);
        if (cleanTmp) {
            Utils.delete(new File(dir));
        }
    }
}
