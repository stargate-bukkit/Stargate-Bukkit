package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a stargate is deactivated
 *
 * <p>A deactivation is usually caused by no activity for a set amount of time.
 * This event can only be used to listen for de-activation events.</p>
 */
@SuppressWarnings("unused")
public class StargateDeactivateEvent extends StargateEvent {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Instantiates a new stargate deactivation event
     *
     * @param portal <p>The portal which was deactivated</p>
     */
    public StargateDeactivateEvent(@NotNull Portal portal) {
        super(portal);
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
