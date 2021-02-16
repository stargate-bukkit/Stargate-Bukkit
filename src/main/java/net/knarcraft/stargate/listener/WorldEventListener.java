package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.PortalHandler;
import net.knarcraft.stargate.Stargate;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldEventListener implements Listener {
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!Stargate.managedWorlds.contains(event.getWorld().getName())
                && PortalHandler.loadAllGates(event.getWorld())) {
            Stargate.managedWorlds.add(event.getWorld().getName());
        }
    }

    // We need to reload all gates on world unload, boo
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Stargate.debug("onWorldUnload", "Reloading all Stargates");
        World w = event.getWorld();
        if (Stargate.managedWorlds.contains(w.getName())) {
            Stargate.managedWorlds.remove(w.getName());
            PortalHandler.clearGates();
            for (World world : Stargate.server.getWorlds()) {
                if (Stargate.managedWorlds.contains(world.getName())) {
                    PortalHandler.loadAllGates(world);
                }
            }
        }
    }
}
