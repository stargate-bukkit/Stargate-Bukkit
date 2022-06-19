package net.TheDgtl.Stargate.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerAdvancementListener implements Listener {
    /**
     * Listen to player advancement events, and cancel if the advancement came from touching a generated portal block
     * <p>
     * NOTE: This have to be in a separate listener, as it's brick other listeners when not running on a paper instance
     *
     * @param event
     */
    @EventHandler
    public void onPlayerAdvancementCriterionGrant(PlayerAdvancementCriterionGrantEvent event) {
        if (!event.getCriterion().equals("entered_end_gateway") || !Stargate.getRegistryStatic()
                .isNextToPortal(event.getPlayer().getLocation(), GateStructureType.IRIS)) {
            return;
        }
        event.setCancelled(true);

    }
}
