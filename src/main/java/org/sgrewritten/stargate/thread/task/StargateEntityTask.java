package org.sgrewritten.stargate.thread.task;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.Stargate;

public class StargateEntityTask extends StargateTask {

    private final Entity entity;
    private final Stargate plugin;

    public StargateEntityTask(Entity entity, Runnable runnable) {
        super(runnable);
        this.entity = entity;
        this.plugin = Stargate.getInstance();
    }

    @Override
    public void run() {
        if (USING_FOLIA) {
            entity.getScheduler().run(plugin, super::runTask, null);
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(super::runTask)).runTask(plugin);
        }
    }

    @Override
    public void runDelayed(long delay) {
        if (USING_FOLIA) {
            entity.getScheduler().runDelayed(plugin, super::runTask, null, delay);
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(super::runTask)).runTaskLater(plugin, delay);
        }
    }
}
