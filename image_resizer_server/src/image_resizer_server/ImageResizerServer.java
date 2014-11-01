package image_resizer_server;

import com.beust.jcommander.JCommander;
import java.io.IOException;

/**
 *
 * @author Adam Kucera
 */
public class ImageResizerServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JCommanderParameters jcp = new JCommanderParameters();
        new JCommander(jcp, args);
        LoadController load_controller = new LoadController();
        try {
            Thread lc_thread = new Thread(load_controller);
            lc_thread.start();
            ServerConnection connection = new ServerConnection();
        } catch (IOException ex) {
            System.out.println("Error when performing I/O operations. Did you specify -file parameter?");
        }
    }

}
