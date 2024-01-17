package org.sgrewritten.stargate.thread;

import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadHelper {
    private static final BlockingQueue<Runnable> asyncQueue = new LinkedBlockingQueue<>();
    private static boolean asyncQueueThreadIsEnabled;


    public static void runAsyncTask(Runnable runnable) {
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
