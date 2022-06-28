package net.TheDgtl.Stargate.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A listener for the PlayerAdvancementCriterionEvent event
 */
public class PlayerAdvancementListener implements Listener {

    /**
     * Listen to player advancement events, and cancel if the advancement came from touching a generated portal block
     *
     * <p>NOTE: This has to be in a separate listener, as it'll otherwise brick other listeners when not running on a
     * paper instance</p>
     *
     * @param event <p>The triggered event</p>
     */
    @EventHandler
    public void onPlayerAdvancementCriterionGrant(PlayerAdvancementCriterionGrantEvent event) {
        if (event.getCriterion().equals("entered_end_gateway") &&
                Stargate.getRegistryStatic().isNextToPortal(event.getPlayer().getLocation(), GateStructureType.IRIS)) {
            event.setCancelled(true);
        }
    }

}
