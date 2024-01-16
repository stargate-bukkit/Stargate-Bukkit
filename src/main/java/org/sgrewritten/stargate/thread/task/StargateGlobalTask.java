package org.sgrewritten.stargate.thread.task;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;

public class StargateGlobalTask extends StargateTask {
    private final Stargate plugin;

    public StargateGlobalTask(Runnable runnable){
        super(runnable);
        this.plugin = Stargate.getInstance();
    }
    @Override
    public void run() {
        if(USING_FOLIA){
            super.registerFoliaTask(Bukkit.getServer().getGlobalRegionScheduler().run(plugin, super::runTask));
        } else {
            super.registerBukkitTask(new StargatBukkitRunnable(super::runTask)).runTask(plugin);
        }
    }

    @Override
    public void runDelayed(long delay) {
        if(USING_FOLIA){
            super.registerFoliaTask(Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, super::runTask, delay));
        } else {
            super.registerBukkitTask(new StargatBukkitRunnable(super::runTask)).runTaskLater(plugin, delay);
        }
    }
}
