package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event should be called whenever a player opens a stargate
 *
 * <p>This event can be used to overwrite whether the stargate should be forced to open, even if it's already open.</p>
 */
@SuppressWarnings({"unused"})
public class StargateOpenEvent extends StargatePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean force;

    /**
     * Instantiates a new stargate open event
     *
     * @param player <p>The player opening the stargate</p>
     * @param portal <p>The opened portal</p>
     * @param force  <p>Whether to force the portal open</p>
     */
    public StargateOpenEvent(@Nullable Player player, @NotNull Portal portal, boolean force) {
        super(portal, player);

        this.force = force;
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
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}
