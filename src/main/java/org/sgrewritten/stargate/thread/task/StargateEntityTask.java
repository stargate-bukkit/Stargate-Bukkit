package org.sgrewritten.stargate.thread.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.sgrewritten.stargate.Stargate;

public class StargateEntityTask extends StargateTask {

    private final Entity entity;
    private final Stargate plugin;

    public StargateEntityTask(Entity entity, Runnable runnable){
        super(runnable);
        this.entity = entity;
        this.plugin = Stargate.getInstance();
    }
    @Override
    public void run() {
        if(USING_FOLIA) {
            entity.getScheduler().run(plugin, super::registerFoliaTask, super::runTask);
        } else {
            super.registerBukkitTask(new StargatBukkitRunnable(super::runTask)).runTask(plugin);
        }
    }

    @Override
    public void runDelayed(long delay) {
        if(USING_FOLIA) {
            entity.getScheduler().runDelayed(plugin, super::registerFoliaTask, super::runTask, delay);
        } else {
            super.registerBukkitTask(new StargatBukkitRunnable(super::runTask)).runTaskLater(plugin,delay);
        }
    }
}
