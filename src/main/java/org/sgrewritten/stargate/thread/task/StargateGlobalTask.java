package org.sgrewritten.stargate.thread.task;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.Stargate;

public class StargateGlobalTask extends StargateTask {
    private final Stargate plugin;

    public StargateGlobalTask(Runnable runnable){
        super(runnable);
        this.plugin = Stargate.getInstance();
    }

    public void run(boolean bungee) {
        // if no players are online, then no bungee messages can be sent (wait 10 second until a player joins)
        if(bungee && !Bukkit.getServer().getOnlinePlayers().isEmpty()){
            runDelayed(200, () -> run(true));
            return;
        }

        if(USING_FOLIA){
            super.registerFoliaTask(Bukkit.getServer().getGlobalRegionScheduler().run(plugin, this::runTask));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(this::runTask)).runTask(plugin);
        }
    }

    private void runDelayed(long delay, Runnable runnable) {
        if(USING_FOLIA){
            super.registerFoliaTask(Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> runnable.run(), delay));
        } else {
            super.registerBukkitTask(new StargateBukkitRunnable(runnable)).runTaskLater(plugin, delay);
        }
    }

    @Override
    public void runDelayed(long delay) {
        runDelayed(delay, super::runTask);
    }

    @Override
    public void run() {
        run(false);
    }
}
