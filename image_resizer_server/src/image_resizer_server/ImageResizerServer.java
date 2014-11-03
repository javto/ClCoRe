package image_resizer_server;

import java.io.IOException;
import java.util.Timer;

/**
 * The main class for the server, it starts all the main threads for
 * connections, schedulling and VM management. It starts Timer for monitoring.
 *
 * @author Adam Kucera
 */
public class ImageResizerServer {

    /**
     * Main server method responsible for starting threads.
     *
     * @param args If the argument is 1, it is started as master, otherwise as
     * slave
     */
    public static void main(String[] args) {
        //run as master server
        if (args.length != 0 && "1".equals(args[0])) {
            VMManager vmm = new VMManager();
            //this thread monitors our running VMs and starts/stops them, depending on a load
            Thread vmm_thread = new Thread(vmm);
            vmm_thread.run();
            try {
                //start socket connection and accept clients -> and point them to other VMs
                ServerConnection connection = new ServerConnection();
                connection.runMaster();
            } catch (IOException ex) {
                System.out.println("Error when performing I/O operations.");
            }
        } //run as slave server = receive files, process them and return to the user
        else {
            //start the performance monitoring on the machine
            Timer timer = new Timer();
            timer.schedule(new Monitor(), 0, 1000);
            //start the main thread processing items in the queue
            JobProcessor jp = new JobProcessor();
            Thread jp_thread = new Thread(jp);
            jp_thread.start();
            //start the server connection and accept clients and files
            try {
                ServerConnection connection = new ServerConnection();
                connection.runSlave();
            } catch (IOException ex) {
                System.out.println("Error when performing I/O operations.");
            }
        }
    }

}
