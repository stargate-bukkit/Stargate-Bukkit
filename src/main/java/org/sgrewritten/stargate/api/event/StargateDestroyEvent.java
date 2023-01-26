package org.sgrewritten.stargate.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * This event should be called whenever a stargate is destroyed
 *
 * <p>This event can be used to deny or change the cost of a stargate destruction.</p>
 */
@SuppressWarnings("unused")
public class StargateDestroyEvent extends StargateExistenceEvent {

    /**
     * Instantiates a new Stargate Destroy Event
     *
     * @param portal     <p>The destroyed portal</p>
     * @param player     <p>The player destroying the portal</p>
     * @param deny       <p>Whether the event should be denied (cancelled)</p>
     * @param denyReason <p>The message to display if the event is denied</p>
     * @param cost       <p>The cost of destroying the portal</p>
     */
    public StargateDestroyEvent(@NotNull Portal portal, @NotNull Player player, boolean deny, String denyReason,
                                int cost) {
        super(portal, player, deny, denyReason, cost);

        //TODO: Perhaps alter, or add an event for a stargate destroyed by an explosion?
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

}