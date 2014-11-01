package image_resizer_server;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author wrent
 */
public class QueueManager {

    private static QueueManager instance;
    private ConcurrentLinkedQueue<QueueItem> pending_queue;

    public static QueueManager GetInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }

    public QueueManager() {
        pending_queue = new ConcurrentLinkedQueue<QueueItem>();
    }

    public void enqueue(QueueItem item) {
        pending_queue.add(item);
    }
    
    public boolean hasItems() {
        return !pending_queue.isEmpty();
    }
    
    public QueueItem dequeue() {
        return pending_queue.poll();
    }
}
