package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * An abstract event describing any stargate event
 */
@SuppressWarnings("unused")
public abstract class StargatePortalEvent extends Event {

    private final Portal portal;

    /**
     * Instantiates a new stargate event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    StargatePortalEvent(@NotNull Portal portal, boolean async) {
        super(async);
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

}
