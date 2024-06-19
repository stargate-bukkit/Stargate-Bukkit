package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.property.BlockEventType;

public class StargateDestroyPortalEvent extends CancellableStargatePortalEvent {
    private final BlockEventType cause;
    private static final HandlerList handlerList = new HandlerList();

    /**
     * Instantiates a new cancellable stargate event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    public StargateDestroyPortalEvent(@NotNull Portal portal, BlockEventType cause) {
        super(portal, false);
        this.cause = cause;
    }

    /**
     * @return <p>The event that caused this portal destruction</p>
     */
    public BlockEventType getCause() {
        return this.cause;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
