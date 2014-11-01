package image_resizer_server;

import java.io.File;

/**
 *
 * @author Adam Kucera
 */
public class QueueItem {

    private File file;
    private JCommanderParameters params;
    private ConnectedClient client;
    private Object lock;

    public QueueItem(JCommanderParameters params, ConnectedClient client, String dir, Object lock) {
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

    public ConnectedClient getClient() {
        return client;
    }

    public Object getLock() {
        return lock;
    }
}
