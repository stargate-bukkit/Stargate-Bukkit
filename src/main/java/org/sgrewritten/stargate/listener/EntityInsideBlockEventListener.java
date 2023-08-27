package org.sgrewritten.stargate.listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;
import org.sgrewritten.stargate.api.network.RegistryAPI;

/**
 * A listener specifically for the Paper-only EntityInsideBlockEvent event
 */
public class EntityInsideBlockEventListener implements Listener {

    private final RegistryAPI registry;

    public EntityInsideBlockEventListener(RegistryAPI registry) {
        this.registry = registry;
    }

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
                registry.getPortal(block.getLocation(), GateStructureType.IRIS) != null) {
            event.setCancelled(true);
        }
    }

}
