package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.Stargate;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityEventListener implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        for (Block b : event.blockList()) {
            Portal portal = Portal.getByBlock(b);
            if (portal == null) {
                continue;
            }
            if (Stargate.destroyedByExplosion()) {
                portal.unregister(true);
            } else {
                event.setCancelled(true);
                break;
            }
        }
    }
}
