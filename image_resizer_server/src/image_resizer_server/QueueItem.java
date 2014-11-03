package image_resizer_server;

import java.io.File;

/**
 * Object representing an item in the queue
 * @author Adam Kucera
 */
public class QueueItem {

    private File file;
    private JCommanderParameters params;
    private ConnectedClientSlave client;
    private Object lock;

    public QueueItem(JCommanderParameters params, ConnectedClientSlave client, String dir, Object lock) {
        this.params = params;
        this.client = client;
        this.file = new File(dir, client.getId() + ".zip");
        this.lock = lock;
    }

    public File getFile() {
        return file;
    }

    public JCommanderParameters getParams() {
        return params;
    }

    public ConnectedClientSlave getClient() {
        return client;
    }

    public Object getLock() {
        return lock;
    }
}
