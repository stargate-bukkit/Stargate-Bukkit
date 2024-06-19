package org.sgrewritten.stargate.thread.task;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.util.BungeeHelper;

/**
 * Runs task on the global thread (Folia) or on the main thread (paper)
 */
public abstract class StargateGlobalTask extends StargateTask {
    private final Stargate plugin;
    private boolean bungee = false;

    protected StargateGlobalTask() {
        this.plugin = Stargate.getInstance();
    }

    protected StargateGlobalTask(boolean bungee) {
        this.plugin = Stargate.getInstance();
        this.bungee = bungee;
    }

    @Override
    public void runDelayed(long delay) {
        if (USING_FOLIA) {
            super.registerFoliaTask(Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, super::runTask, delay));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(super::runTask)).runTaskLater(plugin, delay);
        }
    }

    @Override
    public void runNow() {
        super.setRepeatable(false);
        if (bungee && !BungeeHelper.canSendBungeeMessages()) {
            runTaskTimer(20, 20, () -> {
                if (BungeeHelper.canSendBungeeMessages()) {
                    this.runNow();
                }
            });
            return;
        }
        if (USING_FOLIA) {
            super.registerFoliaTask(Bukkit.getServer().getGlobalRegionScheduler().run(plugin, this::runTask));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(this::runTask)).runTask(plugin);
        }
    }

    public void runTaskTimer(long period, long delay, Runnable runnable) {
        super.setRepeatable(true);
        if (USING_FOLIA) {
            super.registerFoliaTask(Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, ignored -> runnable.run(), delay, period));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(runnable)).runTaskTimer(plugin, delay, period);
        }
    }

    @Override
    public void runTaskTimer(long period, long delay) {
        runTaskTimer(period, delay, super::runTask);
    }
}
