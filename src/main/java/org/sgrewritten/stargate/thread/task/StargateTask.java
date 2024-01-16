package org.sgrewritten.stargate.thread.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.sgrewritten.stargate.property.NonLegacyMethod;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class StargateTask implements Runnable {
    protected static final boolean USING_FOLIA = NonLegacyMethod.FOLIA.isImplemented();
    private final Runnable runnable;
    private volatile boolean cancelled;
    private volatile boolean running;
    private ScheduledTask scheduledTask;
    private BukkitRunnable scheduledBukkitTask;
    private static Set<Runnable> tasks = new ConcurrentSkipListSet<>();

    abstract void runDelayed(long delay);

    protected StargateTask(Runnable runnable){
        this.runnable = runnable;
    }
    public void cancel() {
        this.cancelled = true;
        cancelIfTaskHasBeenScheduled(!USING_FOLIA);
    }

    protected void registerTask(){
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
        if(cancelled){
            return;
        }
        cancelled = true;
        if (bukkit) {
            scheduledBukkitTask.cancel();
        } else if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        tasks.remove(runnable);
    }

    public static void forceRunAllTasks(){
        tasks.forEach(Runnable::run);
    }

    protected void runTask() {
        if(running){
            return;
        }
        running = true;
        tasks.remove(runnable);
        runnable.run();
    }

    public void runTask(ScheduledTask scheduledTask) {
        this.runTask();
    }
}
