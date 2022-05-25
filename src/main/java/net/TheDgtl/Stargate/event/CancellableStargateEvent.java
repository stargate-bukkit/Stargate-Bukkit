package net.TheDgtl.Stargate.event;

import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * A Stargate event which can be cancelled
 */
public abstract class CancellableStargateEvent extends StargateEvent implements Cancellable {

    private boolean cancelled;

    /**
     * Instantiates a new cancellable stargate event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    CancellableStargateEvent(@NotNull Portal portal) {
        super(portal);

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
