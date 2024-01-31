package org.sgrewritten.stargate.thread;

import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadHelper {

    private ThreadHelper(){
        throw new IllegalStateException("Utility class");
    }
    private static final BlockingQueue<Runnable> asyncQueue = new LinkedBlockingQueue<>();
    private static volatile boolean asyncQueueThreadIsEnabled = false;


    public static void runAsyncTask(Runnable runnable) {
        try {
            asyncQueue.put(runnable);
        } catch (InterruptedException e) {
            Stargate.log(e);
            Thread.currentThread().interrupt();
        }
    }

    public static void cycleThroughAsyncQueue() {
        if(asyncQueueThreadIsEnabled){
            return;
        }
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
            Thread.currentThread().interrupt();
        }
    }

    public static void setAsyncQueueEnabled(boolean enable) {
        runAsyncTask(() -> asyncQueueThreadIsEnabled = enable);
    }

    public static boolean getAsyncQueueEnabled() {
        return asyncQueueThreadIsEnabled;
    }
}
