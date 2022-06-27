package net.TheDgtl.Stargate.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;


public class EntityInsideBlockEventListener implements Listener{
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onEntityInsideBlock(EntityInsideBlockEvent event) {
        Block block = event.getBlock();
        if ((block.getType() != Material.END_PORTAL && block.getType() != Material.NETHER_PORTAL)
                || Stargate.getRegistryStatic().getPortal(block.getLocation(), GateStructureType.IRIS) == null) {
            return;
        }
        event.setCancelled(true);
    }
}
