package image_resizer_server;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Object responsible for maintaining the job queue instance.
 *
 * @author wrent
 */
public class QueueManager {

    private static QueueManager instance;
    private ConcurrentLinkedQueue<QueueItem> pending_queue = null;

    /**
     * Returns the running instance of the queue. If it does not exist, it
     * creates it.
     *
     * @return instance of queue
     */
    public static QueueManager GetInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }

    /**
     * Private constructor.
     */
    private QueueManager() {
        this.pending_queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds item to the queue.
     *
     * @param item Item to add to the queue.
     */
    public void enqueue(QueueItem item) {
        pending_queue.add(item);
    }

    /**
     * Finds out, if the queue has items.
     *
     * @return has some items?
     */
    public boolean hasItems() {
        if (pending_queue != null) {
            return !pending_queue.isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Dequeues item from the queue
     *
     * @return item on front of the queue
     */
    public QueueItem dequeue() {
        return pending_queue.poll();
    }
}
