package org.sgrewritten.stargate.thread.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.property.NonLegacyClass;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class StargateTask implements Runnable {
    protected static final boolean USING_FOLIA = NonLegacyClass.REGIONIZED_SERVER.isImplemented();
    private static final int MAXIMUM_SHUTDOWN_CYCLES = 10;
    private boolean taskIsRegistered = false;
    private volatile boolean cancelled = false;
    private volatile boolean running = false;
    private ScheduledTask scheduledTask;
    private BukkitRunnable scheduledBukkitTask;
    private static final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private boolean repeatable = false;

    public abstract void runDelayed(long delay);

    public abstract void runNow();

    public abstract void runTaskTimer(long period, long delay);


    /**
     * Cancel this task
     */
    public void cancel() {
        this.cancelled = true;
        cancelIfTaskHasBeenScheduled(!USING_FOLIA);
    }

    /**
     * Register the task to all tasks that are currently run
     */
    protected void registerTask() {
        if (taskIsRegistered) {
            return;
        }
        taskIsRegistered = true;
        tasks.add(this);
    }

    /**
     * Register a Folia task
     * @param scheduledTask <p>The id of the task</p>
     */
    protected void registerFoliaTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
        if (cancelled) {
            cancelIfTaskHasBeenScheduled(false);
        } else {
            registerTask();
        }
    }

    /**
     * Register a bukkit task
     * @param scheduledBukkitTask <p>The ID of the task</p>
     * @return <p>A bukkit runnable</p>
     */
    protected BukkitRunnable registerBukkitTask(BukkitRunnable scheduledBukkitTask) {
        this.scheduledBukkitTask = scheduledBukkitTask;
        if (!cancelled) {
            registerTask();
        }
        return scheduledBukkitTask;
    }

    /**
     * Cancel this task if it has been scheduled
     * @param bukkit <p>Whether it's a bukkit or Folia task</p>
     */
    private void cancelIfTaskHasBeenScheduled(boolean bukkit) {
        if (bukkit) {
            scheduledBukkitTask.cancel();
        } else if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        tasks.remove(this);
    }

    /**
     * Runs all tasks
     */
    public static void forceRunAllTasks() {
        int counter = 0;
        while (!tasks.isEmpty() && counter < MAXIMUM_SHUTDOWN_CYCLES) {
            Queue<Runnable> scheduledTasks = new LinkedList<>(tasks);
            scheduledTasks.forEach(task -> {
                try {
                    task.run();
                } catch (Exception e) {
                    Stargate.log(e);
                }
            });
            tasks.removeAll(scheduledTasks);
            counter++;
        }
    }

    /**
     * Run the task if not already been running (should be threadsafe)
     */
    protected void runTask() {
        if (cancelled) {
            cancelIfTaskHasBeenScheduled(true);
            return;
        }
        if (running && !repeatable) {
            return;
        }
        running = true;
        tasks.remove(this);
        this.run();
    }

    /**
     * Convenience method, does same as {@link StargateTask#runTask()}
     *
     * @param scheduledTask
     */
    protected void runTask(ScheduledTask scheduledTask) {
        if (this.scheduledTask == null) {
            this.scheduledTask = scheduledTask;
        }
        this.runTask();
    }

    /**
     * Should the task repeat itself?
     * @param repeatable <p>Change the task to repeat it self, or to stop repeating itself</p>
     */
    protected void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
