package image_resizer_server;

import java.io.IOException;
import java.util.Timer;

/**
 *
 * @author Adam Kucera
 */
public class ImageResizerServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LoadController load_controller = new LoadController();
        try {
            Timer timer = new Timer();
            timer.schedule(new Monitor(), 0, 1000);
            Thread lc_thread = new Thread(load_controller);
            lc_thread.start();
            ServerConnection connection = new ServerConnection();
        } catch (IOException ex) {
            System.out.println("Error when performing I/O operations. Did you specify -file parameter?");
        }
    }

}
