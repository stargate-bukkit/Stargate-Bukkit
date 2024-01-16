package org.sgrewritten.stargate.thread.task;

import org.bukkit.scheduler.BukkitRunnable;

public class StargatBukkitRunnable extends BukkitRunnable {
    private final Runnable runnable;

    StargatBukkitRunnable(Runnable runnable){
        this.runnable = runnable;
    }
    @Override
    public void run() {
        runnable.run();
    }
}
