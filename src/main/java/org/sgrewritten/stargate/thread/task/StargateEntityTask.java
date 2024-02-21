package org.sgrewritten.stargate.thread.task;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.Stargate;

public abstract class StargateEntityTask extends StargateTask {

    private final Entity entity;
    private final Stargate plugin;

    protected StargateEntityTask(Entity entity) {
        this.entity = entity;
        this.plugin = Stargate.getInstance();
    }

    @Override
    public void runNow() {
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

    @Override
    public void runTaskTimer(long period, long delay) {
        super.setRepeatable(true);
        if (USING_FOLIA) {
            entity.getScheduler().runAtFixedRate(plugin, super::runTask, null, delay, period);
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(super::runTask)).runTaskTimer(plugin,delay,period);
        }
    }
}
