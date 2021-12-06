package net.TheDgtl.Stargate.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * TODO: This class does literally nothing
 */
public class WorldEventListener implements Listener {
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Load all portals in world
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {

    }
}
