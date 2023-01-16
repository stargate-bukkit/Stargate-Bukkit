package org.sgrewritten.stargate.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.api.network.RegistryAPI;

/**
 * A listener for the PlayerAdvancementCriterionEvent event
 */
public class PlayerAdvancementListener implements Listener {

    private RegistryAPI registry;

    public PlayerAdvancementListener(RegistryAPI registry) {
        this.registry = registry;
    }
    
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
                registry.isNextToPortal(event.getPlayer().getLocation(), GateStructureType.IRIS)) {
            event.setCancelled(true);
        }
    }

}
