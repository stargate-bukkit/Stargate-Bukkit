package org.sgrewritten.stargate.api.event.portal;

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
public class StargatePlayerDestroyPortalEvent extends DeniableStargatePortalEvent {

    private static final HandlerList handlers = new HandlerList();
    private double cost;

    /**
     * Instantiates a new Stargate Destroy Event
     *
     * @param portal     <p>The destroyed portal</p>
     * @param player     <p>The player destroying the portal</p>
     * @param deny       <p>Whether the event should be denied (cancelled)</p>
     * @param denyReason <p>The message to display if the event is denied</p>
     * @param cost       <p>The cost of destroying the portal</p>
     */
    public StargatePlayerDestroyPortalEvent(@NotNull Portal portal, @NotNull Player player, boolean deny, String denyReason,
                                            double cost) {
        super(portal, player, deny, denyReason, false);

        this.cost = cost;
    }

    /**
     * Gets the cost of destroying the portal
     *
     * @return <p>The cost of destroying the portal</p>
     */
    public double getCost() {
        return cost;
    }

    /**
     * Sets the cost of destroying the portal
     *
     * @param cost <p>The cost of destroying the portal</p>
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}