package org.sgrewritten.stargate.thread.task;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.TimeUnit;

public class StargateAsyncTask extends StargateTask{

    private final Stargate plugin;

    StargateAsyncTask(Runnable runnable){
        super(runnable);
        this.plugin = Stargate.getInstance();
    }
    @Override
    public void run() {
        if(USING_FOLIA){
            super.registerFoliaTask(Bukkit.getServer().getAsyncScheduler().runNow(plugin, super::runTask));
        } else {
            super.registerBukkitTask(new StargatBukkitRunnable(super::runTask)).runTask(plugin);
        }
    }

    @Override
    public void runDelayed(long delay) {
        if(USING_FOLIA){
            super.registerFoliaTask(Bukkit.getServer().getAsyncScheduler().runDelayed(plugin, super::runTask,delay, TimeUnit.MILLISECONDS));
        } else {
            super.registerBukkitTask(new StargatBukkitRunnable(super::runTask)).runTaskLaterAsynchronously(plugin, delay);
        }
    }
}
