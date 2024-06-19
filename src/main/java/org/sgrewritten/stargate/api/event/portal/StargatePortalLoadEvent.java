package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class StargatePortalLoadEvent extends StargatePortalEvent{

    private static final HandlerList handlerList = new HandlerList();

    /**
     * Instantiates a new portal load event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    public StargatePortalLoadEvent(@NotNull Portal portal) {
        super(portal, false);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
