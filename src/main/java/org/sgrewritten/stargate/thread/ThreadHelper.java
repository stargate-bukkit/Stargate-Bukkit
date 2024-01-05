package org.sgrewritten.stargate.thread;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.SupplierAction;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadHelper {
    private static final BlockingQueue<Runnable> asyncQueue = new LinkedBlockingQueue<>();
    private static boolean asyncQueueThreadIsEnabled;

    /**
     * Makes sure that the runnable is always called syncronously.
     *
     * <p>Depending on whether this is in primary thread or not, this will directly run the runnable</p>
     *
     * @param runnable <p>The Runnable to be run</p>
     */
    public static void callSynchronously(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Stargate.addSynchronousTickAction(new SupplierAction(() -> {
                runnable.run();
                return true;
            }));
        }
    }

    /**
     * Run the task in this thread if not in primary thread, otherwise start a new async thread
     *
     * @param runnable <p>The runnable to run</p>
     */
    public static void callAsynchronously(Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            runAsyncTask(runnable);
        }
    }

    private static void runAsyncTask(Runnable runnable) {
        try {
            asyncQueue.put(runnable);
        } catch (InterruptedException e) {
            Stargate.log(e);
        }
    }

    public static void cycleThroughAsyncQueue() {
        try {
            while (asyncQueueThreadIsEnabled || !asyncQueue.isEmpty()) {
                try {
                    Runnable runnable = asyncQueue.take();
                    runnable.run();
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    Stargate.log(e);
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    public static void setAsyncQueueEnabled(boolean enable) {
        asyncQueueThreadIsEnabled = enable;
        if (!enable) {
            // escape the BlockedQueue#take() waiting thread
            runAsyncTask(() -> {
            });
        }
    }
}
