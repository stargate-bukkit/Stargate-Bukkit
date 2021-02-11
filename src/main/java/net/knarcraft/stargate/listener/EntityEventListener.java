package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.PortalHandler;
import net.knarcraft.stargate.Stargate;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * This listener listens for any relevant events on portal entities
 */
@SuppressWarnings("unused")
public class EntityEventListener implements Listener {

    /**
     * This method catches any explosion events
     *
     * <p>If destroyed by explosions is enabled, any portals destroyed by the explosion will be unregistered. If not,
     * the explosion will be cancelled.</p>
     *
     * @param event <p>The triggered explosion event</p>
     */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        for (Block block : event.blockList()) {
            Portal portal = PortalHandler.getByBlock(block);
            if (portal == null) {
                continue;
            }
            if (Stargate.destroyedByExplosion()) {
                PortalHandler.unregister(portal, true);
            } else {
                event.setCancelled(true);
                break;
            }
        }
    }
}
