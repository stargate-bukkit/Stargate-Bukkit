package org.sgrewritten.stargate.thread.task;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;

import java.util.concurrent.TimeUnit;

/**
 * Runs tasks asynchronously
 */
public abstract class StargateAsyncTask extends StargateTask {

    private final Stargate plugin;

    protected StargateAsyncTask() {
        this.plugin = Stargate.getInstance();
    }

    @Override
    public void runNow() {
        if (USING_FOLIA) {
            super.registerFoliaTask(Bukkit.getServer().getAsyncScheduler().runNow(plugin, super::runTask));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(super::runTask)).runTaskAsynchronously(plugin);
        }
    }

    @Override
    public void runDelayed(long delay) {
        if (USING_FOLIA) {
            super.registerFoliaTask(Bukkit.getServer().getAsyncScheduler().runDelayed(plugin, super::runTask, delay, TimeUnit.MILLISECONDS));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(super::runTask)).runTaskLaterAsynchronously(plugin, delay);
        }
    }

    @Override
    public void runTaskTimer(long period, long delay) {
        super.setRepeatable(true);
        if (USING_FOLIA) {
            super.registerFoliaTask(Bukkit.getServer().getAsyncScheduler().runAtFixedRate(plugin, super::runTask, delay, period, TimeUnit.MILLISECONDS));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(super::runTask)).runTaskTimer(plugin, delay, period);
        }
    }
}
