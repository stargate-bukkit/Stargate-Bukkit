package org.sgrewritten.stargate.thread.task;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Convenience class
 */
public class StargateBukkitRunnable extends BukkitRunnable {
    private final Runnable runnable;

    StargateBukkitRunnable(Runnable runnable){
        this.runnable = runnable;
    }
    @Override
    public void run() {
        runnable.run();
    }
}
