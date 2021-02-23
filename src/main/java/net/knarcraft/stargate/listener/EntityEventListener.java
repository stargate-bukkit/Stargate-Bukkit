package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.utility.EntityHelper;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;

/**
 * This listener listens for any relevant events on portal entities
 */
@SuppressWarnings("unused")
public class EntityEventListener implements Listener {

    /**
     * This event handler prevents sending entities to the normal nether instead of the stargate target
     *
     * @param event <p>The event to check and possibly cancel</p>
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPortalEvent(EntityPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        if (PortalHandler.getByAdjacentEntrance(event.getFrom(), (int) EntityHelper.getEntityMaxSize(entity)) != null) {
            event.setCancelled(true);
        }
    }

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
                PortalHandler.unregisterPortal(portal, true);
            } else {
                event.setCancelled(true);
                break;
            }
        }
    }
}
