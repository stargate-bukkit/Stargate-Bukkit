package org.sgrewritten.stargate.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * This event should be called whenever a stargate is closed
 *
 * <p>This event can be used to overwrite whether the stargate should be forced to close, even if it's set as
 * always-on.</p>
 */
@SuppressWarnings("unused")
public class StargateCloseEvent extends CancellableStargateEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean force;

    /**
     * Instantiates a new stargate closing event
     *
     * @param portal <p>The portal to close</p>
     * @param force  <p>Whether to force the gate to close, even if set as always-on</p>
     */
    public StargateCloseEvent(@NotNull Portal portal, boolean force) {
        super(portal);

        this.force = force;
    }

    /**
     * Gets whether to force the stargate to close
     *
     * @return <p>Whether to force the stargate to close</p>
     */
    public boolean getForce() {
        return force;
    }

    /**
     * Sets whether the stargate should be forced to close
     *
     * @param force <p>Whether the stargate should be forced to close</p>
     */
    public void setForce(boolean force) {
        this.force = force;
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
