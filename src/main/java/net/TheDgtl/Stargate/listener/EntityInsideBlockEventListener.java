package net.TheDgtl.Stargate.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * A listener specifically for the Paper-only EntityInsideBlockEvent event
 */
public class EntityInsideBlockEventListener implements Listener {

    /**
     * Listens for the entity inside block event and cancels it if it's caused by a player entering a Stargate
     *
     * <p>NOTE: This has to be in a separate listener, as it'll otherwise brick other listeners when not running on a
     * paper instance</p>
     *
     * @param event <p>The triggered event</p>
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onEntityInsideBlock(EntityInsideBlockEvent event) {
        Block block = event.getBlock();
        //Block any results of entering a default portal block
        if ((block.getType() == Material.END_PORTAL || block.getType() == Material.NETHER_PORTAL) &&
                Stargate.getRegistryStatic().getPortal(block.getLocation(), GateStructureType.IRIS) != null) {
            event.setCancelled(true);
        }
    }

}
