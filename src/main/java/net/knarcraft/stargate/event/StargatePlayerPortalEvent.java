package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a player teleports through a stargate
 *
 * <p>This event can be used to overwrite the location the player is teleported to.</p>
 */
@SuppressWarnings("unused")
public class StargatePlayerPortalEvent extends StargatePlayerEvent implements StargateTeleportEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Portal destination;
    private Location exit;

    /**
     * Instantiates a new stargate player portal event
     *
     * @param player      <p>The player teleporting</p>
     * @param portal      <p>The portal the player entered from</p>
     * @param destination <p>The destination the player should exit from</p>
     * @param exit        <p>The exit location of the destination portal the user will be teleported to</p>
     */
    public StargatePlayerPortalEvent(@NotNull Player player, @NotNull Portal portal, @NotNull Portal destination,
                                     @NotNull Location exit) {
        super(portal, player);

        this.destination = destination;
        this.exit = exit;
    }

    /**
     * Return the destination portal
     *
     * @return <p>The destination portal</p>
     */
    @NotNull
    public Portal getDestination() {
        return destination;
    }

    /**
     * Return the location of the players exit point
     *
     * @return <p>Location of the exit point</p>
     */
    @Override
    @NotNull
    public Location getExit() {
        return exit;
    }

    /**
     * Set the location of the player's exit point
     *
     * @param location <p>The new location of the player's exit point</p>
     */
    public void setExit(@NotNull Location location) {
        this.exit = location;
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
