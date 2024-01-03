package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * This event should be called whenever a stargate is deactivated
 *
 * <p>A deactivation is usually caused by no activity for a set amount of time.
 * This event can only be used to listen for de-activation events.</p>
 */
@SuppressWarnings("unused")
public class StargateDeactivatePortalEvent extends StargatePortalEvent {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Instantiates a new stargate deactivation event
     *
     * @param portal <p>The portal which was deactivated</p>
     */
    public StargateDeactivatePortalEvent(Portal portal) {
        super(portal, false);
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