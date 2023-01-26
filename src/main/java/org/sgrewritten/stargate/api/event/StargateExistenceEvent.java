package org.sgrewritten.stargate.api.event;

import org.bukkit.entity.Player;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * An event causing a Stargate to exist, or stop existing
 */
public abstract class StargateExistenceEvent extends DeniableStargateEvent {

    private int cost;

    /**
     * Instantiates a new stargate existence event
     *
     * @param portal     <p>The portal involved in this stargate event</p>
     * @param player     <p>The player trying to change the existence of a stargate</p>
     * @param deny       <p>Whether the stargate access should be denied</p>
     * @param denyReason <p>The reason stargate access was denied</p>
     * @param cost       <p>The cost of changing the Stargate's existence</p>
     */
    StargateExistenceEvent(Portal portal, Player player, boolean deny, String denyReason, int cost) {
        super(portal, player, deny, denyReason);
        this.cost = cost;
    }

    /**
     * Gets the cost of creating the stargate
     *
     * @return <p>The cost of creating the stargate</p>
     */
    public int getCost() {
        return cost;
    }

    /**
     * Sets the cost of creating the stargate
     *
     * @param cost <p>The new cost of creating the stargate</p>
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

}
