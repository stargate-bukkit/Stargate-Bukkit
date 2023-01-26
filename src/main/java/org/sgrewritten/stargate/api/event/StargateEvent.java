package org.sgrewritten.stargate.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * An abstract event describing any stargate event
 */
@SuppressWarnings("unused")
public abstract class StargateEvent extends Event {

    protected static final HandlerList handlers = new HandlerList();
    private final Portal portal;

    /**
     * Instantiates a new stargate event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    StargateEvent(@NotNull Portal portal) {
        this.portal = portal;
    }

    /**
     * Gets the portal involved in this stargate event
     *
     * @return <p>The portal involved in this stargate event</p>
     */
    public Portal getPortal() {
        return portal;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
