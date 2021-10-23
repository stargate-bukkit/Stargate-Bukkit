package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.StargateConfig;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.utility.PortalFileHelper;
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
     * This listener listens for the loading of a world and loads all gates from the world if not already loaded
     *
     * @param event <p>The triggered world load event</p>
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        StargateConfig config = Stargate.getStargateConfig();
        if (!config.getManagedWorlds().contains(event.getWorld().getName()) &&
                PortalFileHelper.loadAllPortals(event.getWorld())) {
            config.addManagedWorld(event.getWorld().getName());
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
        String worldName = world.getName();
        StargateConfig config = Stargate.getStargateConfig();
        if (config.getManagedWorlds().contains(worldName)) {
            config.removeManagedWorld(worldName);
            PortalRegistry.clearPortals(world);
        }
    }
}
