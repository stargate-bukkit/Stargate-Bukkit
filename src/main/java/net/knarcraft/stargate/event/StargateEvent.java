package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * An abstract event describing any stargate event
 */
@SuppressWarnings("unused")
public abstract class StargateEvent extends Event implements Cancellable {

    protected final Portal portal;
    protected boolean cancelled;

    public StargateEvent(String event, Portal portal) {
        this.portal = portal;
        this.cancelled = false;
    }


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
