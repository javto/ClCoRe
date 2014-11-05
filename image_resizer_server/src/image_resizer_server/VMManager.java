package image_resizer_server;

import amazon.AmazonConnector;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Adam Kucera
 * @author Jaap
 */
class VMManager implements Runnable {

    AmazonEC2Client amazonEC2Client = null;
    AmazonConnector amazonConnector = null;

    /**
     * start the amazonEC2client and check if we need less or more machines.
     */
    @Override
    public void run() {
        //startup the connection to amazon
        amazonConnector = new AmazonConnector(new File(
                "amazonJaap.properties"), "amazon" + (int) (Math.random() * 1000));
        amazonEC2Client = amazonConnector.getAmazonEC2Client();

        //prints the number of running instances every 10 seconds
        Timer timer = new Timer();
        timer.schedule(new printNumberOfInstances(), 100, 10000);

        //for testing purposes:
        addInstances(4);
        while (true) {
            //TODO check if we need more or less instances

        }
    }

    private int getNumberOfRunningInstances() {
        return getInstances() != null ? getInstances().size() : -1;
    }

    private List<Instance> getInstances() {
        return amazonConnector.getInstances();
    }

    private void addInstances(int numberOfInstances) {
        amazonConnector.runInstances(numberOfInstances);
    }

    class printNumberOfInstances extends TimerTask {

        public void run() {
            System.out.println("There are " + getNumberOfRunningInstances() + " instances running");
        }
    }
    
    /**
     * Connects to the host via SSH (using identity key) and performs given command.
     * code from http://www.jcraft.com/jsch/examples/Shell.java.html
     * @param host host public IP or DNS
     * @param ssh_key identity key for SSH
     * @param command command to be performed
     */
    public void startApplicationViaSSH(String host, String ssh_key, String command) {
        JSch jsch = new JSch();
        String user = "ubuntu";

        try {
            //add identity key
            jsch.addIdentity(ssh_key);
            //create new session
            Session session = jsch.getSession(user, host);
            UserInfo ui = new MyUserInfo() {
                public void showMessage(String message) {
                    JOptionPane.showMessageDialog(null, message);
                }

                public boolean promptYesNo(String message) {
                    Object[] options = {"yes", "no"};
                    int foo = JOptionPane.showOptionDialog(null,
                            message,
                            "Warning",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return foo == 0;
                }
            };
            session.setUserInfo(ui);
            //dont check key footprints
            session.setConfig("StrictHostKeyChecking", "no");
            //connect to SSH
            session.connect();

            System.out.println("Connected to the server " + host);
            //perform the command
            startSlaveApplication(session, command);
        } catch (JSchException ex) {
            System.err.println("Unable to connect to SSH.");
        }
    }

    /**
     * Executes given command on SSH session.
     * code from http://www.jcraft.com/jsch/examples/Exec.java.html
     * @param session SSH session
     * @param command command to be performed
     * @throws JSchException 
     */
    private void startSlaveApplication(Session session, String command) throws JSchException {
        //perform the command
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);

        //get the result
        try {
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    System.out.print(new String(tmp, 0, i));
                }

                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ee) {
                }
            }
        } catch (IOException ex) {
            System.err.println("Error when reading from the server.");
        }
        //end the connection
        channel.disconnect();
        session.disconnect();
    }

    /**
     * Abstract class required for SSH communication
     * code from http://www.jcraft.com/jsch/examples/Shell.java.html
     */
    public static abstract class MyUserInfo
            implements UserInfo, UIKeyboardInteractive {

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public boolean promptYesNo(String str) {
            return false;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public void showMessage(String message) {
        }

        @Override
        public String[] promptKeyboardInteractive(String destination,
                String name,
                String instruction,
                String[] prompt,
                boolean[] echo) {
            return null;
        }
    }
}
