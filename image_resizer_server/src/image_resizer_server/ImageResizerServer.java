package image_resizer_server;

import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adam Kucera
 */
public class ImageResizerServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //run as master server
        if (args.length != 0 && "1".equals(args[0])) {
            VMManager vmm = new VMManager();
            //this thread monitors our running VMs and starts/stops them, depending on a load
            Thread vmm_thread = new Thread(vmm);
            vmm_thread.run();
            try {
                //start socket connection and accept clients
                ServerConnection connection = new ServerConnection();
                connection.runMaster();
            } catch (IOException ex) {
                System.out.println("Error when performing I/O operations.");
            }
        } 
        //run as slave server
        else {
            JobProcessor jp = new JobProcessor();
            try {
                Timer timer = new Timer();
                timer.schedule(new Monitor(), 0, 1000);
                Thread jp_thread = new Thread(jp);
                jp_thread.start();
                ServerConnection connection = new ServerConnection();
                connection.runSlave();
            } catch (IOException ex) {
                System.out.println("Error when performing I/O operations.");
            }
        }
    }

}
