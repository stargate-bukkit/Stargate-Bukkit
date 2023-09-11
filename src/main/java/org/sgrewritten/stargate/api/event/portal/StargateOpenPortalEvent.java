package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * This event should be called whenever a player opens a stargate
 *
 * <p>This event can be used to overwrite whether the stargate should be forced to open, even if it's already open.</p>
 */
@SuppressWarnings({"unused"})
public class StargateOpenPortalEvent extends StargateEntityPortalEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean force;
    private boolean cancelled;

    /**
     * Instantiates a new stargate open event
     *
     * @param player <p>The player opening the stargate</p>
     * @param portal <p>The opened portal</p>
     * @param force  <p>Whether to force the portal open</p>
     */
    public StargateOpenPortalEvent(Player player, @NotNull Portal portal, boolean force) {
        super(portal, player);

        this.force = force;
        this.cancelled = false;
    }

    /**
     * Gets whether the portal should be forced open
     *
     * @return <p>Whether the portal should be forced open</p>
     */
    public boolean getForce() {
        return force;
    }

    /**
     * Sets whether the portal should be forced open
     *
     * @param force <p>Whether the portal should be forced open</p>
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

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
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