package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a player opens a stargate
 */
public class StargateOpenEvent extends StargatePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean force;

    /**
     * Instantiates a new stargate open event
     *
     * @param player <p>The player opening the stargate</p>
     * @param portal <p>The portal opened</p>
     * @param force  <p>Whether to force the portal open</p>
     */
    public StargateOpenEvent(Player player, Portal portal, boolean force) {
        super("StargateOpenEvent", portal, player);

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

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}
