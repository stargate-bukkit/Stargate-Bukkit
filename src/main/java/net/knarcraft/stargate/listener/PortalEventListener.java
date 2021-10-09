package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.portal.PortalHandler;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

/**
 * Listens for and cancels relevant portal events
 */
public class PortalEventListener implements Listener {

    @EventHandler
    public void onPortalCreation(PortalCreateEvent event) {
        if (event.isCancelled()) {
            return;
        }
        //Cancel nether portal creation when the portal is a StarGate portal
        for (BlockState block : event.getBlocks()) {
            if (PortalHandler.getByBlock(block.getBlock()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
