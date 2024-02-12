package org.sgrewritten.stargate.thread.task;

import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StargateQueuedAsyncTask extends StargateTask {
    private static final BlockingQueue<Runnable> asyncQueue = new LinkedBlockingQueue<>();
    private static boolean asyncQueueThreadIsEnabled = false;

    public StargateQueuedAsyncTask(Runnable runnable) {
        super(runnable);
    }

    @Override
    void runDelayed(long delay) {
        new StargateAsyncTask(this).runDelayed(delay);
    }

    @Override
    public void run() {
        try {
            super.registerTask();
            asyncQueue.put(super::runTask);
        } catch (InterruptedException e) {
            Stargate.log(e);
            Thread.currentThread().interrupt();
        }
    }

    public static void disableAsyncQueue() {
        try {
            asyncQueue.put(new DisableQueueTask());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void enableAsyncQueue() {
        try {
            asyncQueue.put(new EnableQueueTask());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        new StargateAsyncTask(StargateQueuedAsyncTask::cycleThroughAsyncQueue).run();
    }

    private static void cycleThroughAsyncQueue() {
        do {
            try {
                Runnable runnable = asyncQueue.take();
                runnable.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Stargate.log(e);
            }
        } while (asyncQueueThreadIsEnabled || !asyncQueue.isEmpty());
    }

    private static class DisableQueueTask implements Runnable {
        @Override
        public void run() {
            asyncQueueThreadIsEnabled = false;
        }
    }

    private static class EnableQueueTask implements Runnable {

        @Override
        public void run() {
            asyncQueueThreadIsEnabled = true;
        }
    }
}
