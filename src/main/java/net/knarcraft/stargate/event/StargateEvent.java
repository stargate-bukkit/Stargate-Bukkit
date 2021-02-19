package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * An abstract event describing any stargate event
 */
@SuppressWarnings("unused")
public abstract class StargateEvent extends Event implements Cancellable {

    private final Portal portal;
    private boolean cancelled;

    /**
     * Instantiates a new stargate event
     * @param event <p>UNUSED</p>
     * @param portal <p>The portal involved in this stargate event</p>
     */
    StargateEvent(String event, Portal portal) {
        this.portal = portal;
        this.cancelled = false;
    }

    /**
     * Gets the portal involved in this stargate event
     * @return <p>The portal involved in this stargate event</p>
     */
    public Portal getPortal() {
        return portal;
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
