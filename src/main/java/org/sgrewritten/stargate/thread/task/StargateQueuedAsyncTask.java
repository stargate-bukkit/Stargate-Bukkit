package org.sgrewritten.stargate.thread.task;

import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StargateQueuedAsyncTask extends StargateTask {
    private static final BlockingQueue<Runnable> asyncQueue = new LinkedBlockingQueue<>();
    private static boolean asyncQueueThreadIsEnabled = false;
    private static boolean isCyclingThroughQueue = false;

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
            if (!asyncQueueThreadIsEnabled) {
                enableAsyncQueue();
            }
            super.registerTask();
            asyncQueue.put(super::runTask);
        } catch (InterruptedException e) {
            Stargate.log(e);
            Thread.currentThread().interrupt();
        }
    }

    public static void disableAsyncQueue() {
        try {
            asyncQueue.put(() -> {
                asyncQueueThreadIsEnabled = false;
                isCyclingThroughQueue = false;
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void enableAsyncQueue() {
        if (isCyclingThroughQueue) {
            return;
        }
        isCyclingThroughQueue = true;
        try {
            asyncQueue.put(() -> asyncQueueThreadIsEnabled = true);
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
        } while (asyncQueueThreadIsEnabled);
    }
}
