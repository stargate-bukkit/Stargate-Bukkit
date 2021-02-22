package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.Stargate;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * This listener listens for the loading and unloading of worlds to load and unload stargates
 */
@SuppressWarnings("unused")
public class WorldEventListener implements Listener {

    /**
     * This listener listens for the loading of a world and loads all all gates from the world if not already loaded
     *
     * @param event <p>The triggered world load event</p>
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!Stargate.managedWorlds.contains(event.getWorld().getName()) &&
                PortalHandler.loadAllGates(event.getWorld())) {
            Stargate.managedWorlds.add(event.getWorld().getName());
        }
    }

    /**
     * This listener listens for the unloading of a world
     *
     * @param event <p>The triggered world unload event</p>
     */
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Stargate.debug("onWorldUnload", "Reloading all Stargates");
        World world = event.getWorld();
        if (Stargate.managedWorlds.contains(world.getName())) {
            Stargate.managedWorlds.remove(world.getName());
            PortalHandler.clearGates(world);
        }
    }
}
