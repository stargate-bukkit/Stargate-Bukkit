package org.sgrewritten.stargate.thread.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.sgrewritten.stargate.property.NonLegacyMethod;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class StargateTask implements Runnable {
    protected static final boolean USING_FOLIA = NonLegacyMethod.FOLIA.isImplemented();
    private static final int MAXIMUM_SHUTDOWN_CYCLES = 10;
    private final Runnable runnable;
    private volatile boolean cancelled;
    private volatile boolean running;
    private ScheduledTask scheduledTask;
    private BukkitRunnable scheduledBukkitTask;
    private static final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    abstract void runDelayed(long delay);

    protected StargateTask(Runnable runnable) {
        this.runnable = runnable;
    }

    public void cancel() {
        this.cancelled = true;
        cancelIfTaskHasBeenScheduled(!USING_FOLIA);
    }

    protected void registerTask() {
        tasks.add(runnable);
    }

    protected void registerFoliaTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
        if (cancelled) {
            cancelIfTaskHasBeenScheduled(false);
        } else {
            registerTask();
        }
    }

    protected BukkitRunnable registerBukkitTask(BukkitRunnable scheduledBukkitTask) {
        this.scheduledBukkitTask = scheduledBukkitTask;
        if (cancelled) {
            cancelIfTaskHasBeenScheduled(true);
        } else {
            registerTask();
        }
        return scheduledBukkitTask;
    }

    private void cancelIfTaskHasBeenScheduled(boolean bukkit) {
        if (bukkit) {
            scheduledBukkitTask.cancel();
        } else if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        tasks.remove(runnable);
    }

    /**
     * Runns all tasks
     */
    public static void forceRunAllTasks() {
        int counter = 0;
        while(!tasks.isEmpty() && counter > MAXIMUM_SHUTDOWN_CYCLES) {
            tasks.forEach(Runnable::run);
        }
    }

    /**
     * Run the task if not already been running (should be threadsafe)
     */
    protected void runTask() {
        if (running || cancelled) {
            return;
        }
        running = true;
        tasks.remove(runnable);
        runnable.run();
    }

    /**
     * Convenience method, does same as {@link StargateTask#runTask()}
     *
     * @param scheduledTask
     */
    protected void runTask(ScheduledTask scheduledTask) {
        if(this.scheduledTask == null){
            this.scheduledTask = scheduledTask;
        }
        this.runTask();
    }
}
