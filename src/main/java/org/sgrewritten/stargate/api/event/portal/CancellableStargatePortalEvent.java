package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * A Stargate event which can be cancelled
 */
public abstract class CancellableStargatePortalEvent extends StargatePortalEvent implements Cancellable {

    private boolean cancelled;

    /**
     * Instantiates a new cancellable stargate event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     * @param async <p>Whether the event is asynchronous</p>
     */
    protected CancellableStargatePortalEvent(@NotNull Portal portal, boolean async) {
        super(portal, async);

        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
