package org.sgrewritten.stargate.thread.task;

import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Runs asynchronous tasks in a queue (an attempt to avoid race conditions, and probably better than not doing this)
 */
public abstract class StargateQueuedAsyncTask extends StargateTask {
    private static final BlockingQueue<Runnable> asyncQueue = new LinkedBlockingQueue<>();
    private static boolean asyncQueueThreadIsEnabled = false;

    protected StargateQueuedAsyncTask() {
    }

    @Override
    public void runDelayed(long delay) {
        StargateQueuedAsyncTask task = this;
        new StargateAsyncTask() {
            @Override
            public void run() {
                task.run();
            }
        }.runDelayed(delay);
    }

    @Override
    public void runNow() {
        try {
            super.registerTask();
            asyncQueue.put(super::runTask);
        } catch (InterruptedException e) {
            Stargate.log(e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void runTaskTimer(long period, long delay) {
        super.setRepeatable(true);
        StargateQueuedAsyncTask task = this;
        new StargateAsyncTask() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskTimer(period, delay);
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
        new StargateAsyncTask() {
            @Override
            public void run() {
                StargateQueuedAsyncTask.cycleThroughAsyncQueue();
            }
        }.runNow();
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
