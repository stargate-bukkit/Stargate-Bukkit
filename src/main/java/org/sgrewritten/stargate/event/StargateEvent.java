package org.sgrewritten.stargate.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.network.portal.Portal;

/**
 * An abstract event describing any stargate event
 */
@SuppressWarnings("unused")
public abstract class StargateEvent extends Event {

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

}
