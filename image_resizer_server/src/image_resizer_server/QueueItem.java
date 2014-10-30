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

    public QueueItem(JCommanderParameters params, ConnectedClient client, String dir) {
        this.params = params;
        this.client = client;
        this.file = new File(dir, client.getId() + ".zip");
    }

}
