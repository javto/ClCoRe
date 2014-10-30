package image_resizer_server;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author wrent
 */
public class QueueManager {

    private static QueueManager instance;
    private ConcurrentLinkedQueue<QueueItem> queue;

    public static QueueManager GetInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }

    public QueueManager() {
        queue = new ConcurrentLinkedQueue<QueueItem>();
    }

    public void enqueue(QueueItem item) {
        queue.add(item);
    }

}
