package image_resizer_server;

import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            VMManager vmm = VMManager.getInstance();
            //this thread monitors our running VMs and starts/stops them, depending on a load
            Thread vmm_thread = new Thread(vmm);
            vmm_thread.start();
            //start monitor connection thread responsible for updating information about machines
            MonitorConnection mc;
            try {
                mc = new MonitorConnection();
                Thread mc_thread = new Thread(mc);
                mc_thread.start();
            } catch (IOException ex) {
                System.err.println("Error when starting Monitor Connection.");
            }
            try {
                //start socket connection and accept clients -> and point them to other VMs
                ServerConnection connection = new ServerConnection();
                connection.runMaster();
            } catch (IOException ex) {
                System.out.println("Error when performing I/O operations.");
            }
        } else {
            //start the performance monitoring on the machine
            Timer timer = new Timer();
            timer.schedule(Monitor.getInstance(), 0, 1000);
            //schedule sending of Monitor information to master
            MonitorClientConnection mcc = new MonitorClientConnection();
            Timer mcc_timer = new Timer();
            mcc_timer.schedule(mcc, 0, 5000);
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
