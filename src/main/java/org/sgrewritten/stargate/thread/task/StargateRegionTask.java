package org.sgrewritten.stargate.thread.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.thread.SynchronousPopulator;

public abstract class StargateRegionTask extends StargateTask {

    private final Location location;
    private final Stargate plugin;
    private static final SynchronousPopulator populator = new SynchronousPopulator();

    private final boolean bungee;

    protected StargateRegionTask(Location location, boolean bungee) {
        this.location = location;
        this.plugin = Stargate.getInstance();
        this.bungee = bungee;
    }

    protected StargateRegionTask(Location location) {
        this(location, false);
    }

    @Override
    public void runNow() {
        if (USING_FOLIA) {
            ScheduledTask theTask = Bukkit.getServer().getRegionScheduler().run(plugin, location, super::runTask);
            super.registerFoliaTask(theTask);
        } else {
            runPopulatorTask();
        }
    }

    @Override
    public void runDelayed(long delay) {
        if (USING_FOLIA) {
            ScheduledTask theTask = Bukkit.getServer().getRegionScheduler().runDelayed(plugin, location, super::runTask, delay);
            super.registerFoliaTask(theTask);
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(this::runPopulatorTask)).runTaskLater(plugin, delay);
        }
    }

    @Override
    public void runTaskTimer(long period, long delay) {
        super.setRepeatable();
        if (USING_FOLIA) {
            ScheduledTask theTask = Bukkit.getServer().getRegionScheduler().runAtFixedRate(plugin, location, super::runTask, delay, period);
            super.registerFoliaTask(theTask);
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(this::runPopulatorTask)).runTaskTimer(plugin, delay, period);
        }
    }

    private void runPopulatorTask() {
        populator.addAction(super::runTask, bungee);
        super.registerTask();
    }

    public static void startPopulator(Plugin plugin) {
        if (USING_FOLIA) {
            return;
        }
        new StargateBukkitRunnable(populator).runTaskTimer(plugin, 0, 1);
    }

    public static void clearPopulator() {
        populator.clear();
    }
}
