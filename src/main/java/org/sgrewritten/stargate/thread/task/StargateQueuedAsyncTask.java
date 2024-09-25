package org.sgrewritten.stargate.thread.task;

import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Runs asynchronous tasks in a queue (an attempt to avoid race conditions, and probably better than not doing this)
 */
public abstract class StargateQueuedAsyncTask extends StargateTask {
    public static final BlockingQueue<Runnable> asyncQueue = new LinkedBlockingQueue<>();

    protected StargateQueuedAsyncTask() {
    }

    public static void waitForEmptyQueue() {
        while (true) {
            if (asyncQueue.peek() == null) {
                return;
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
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

    public static void disableAsyncQueue(long id) {
        try {
            asyncQueue.put(new DisableQueueTask(id));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void enableAsyncQueue(long id) {
        new StargateAsyncTask() {
            @Override
            public void run() {
                StargateQueuedAsyncTask.cycleThroughAsyncQueue(id);
            }
        }.runNow();
    }

    private static void cycleThroughAsyncQueue(long id) {
        do {
            try {
                Runnable runnable = asyncQueue.take();
                if (runnable instanceof DisableQueueTask disableQueueTask && !disableQueueTask.hasId(id)) {
                    asyncQueue.put(runnable);
                    continue;
                }
                runnable.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                Stargate.log(e);
            }
        } while (!Thread.currentThread().isInterrupted());
    }

    private static class DisableQueueTask implements Runnable {
        private final long id;

        public DisableQueueTask(long id) {
            this.id = id;
        }

        @Override
        public void run() {
            Thread.currentThread().interrupt();
            asyncQueue.clear();
        }

        public boolean hasId(long id) {
            return this.id == id;
        }
    }
}
