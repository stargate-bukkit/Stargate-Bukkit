package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.PortalHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * A listener that listens for any relevant events causing entities to spawn
 */
public class EntitySpawnListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        //Prevent Zombified Piglins and other creatures form spawning at stargates
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL) {
            if (PortalHandler.getByEntrance(event.getLocation()) != null) {
                event.setCancelled(true);
                Stargate.debug("EntitySpawnListener", "Prevented creature from spawning at Stargate");
            }
        }
    }

}
