package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a player teleports through a stargate
 */
@SuppressWarnings("unused")
public class StargatePortalEvent extends StargatePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Portal destination;
    private Location exit;

    /**
     * Instantiates a new stargate portal event
     *
     * @param player      <p>The player teleporting</p>
     * @param portal      <p>The portal the player entered from</p>
     * @param destination <p>The destination the player should exit from</p>
     * @param exit        <p>The exit location of the destination portal the user will be teleported to</p>
     */
    public StargatePortalEvent(Player player, Portal portal, Portal destination, Location exit) {
        super("StargatePortalEvent", portal, player);

        this.destination = destination;
        this.exit = exit;
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
     * Return the destination gate
     *
     * @return destination gate
     */
    public Portal getDestination() {
        return destination;
    }

    /**
     * Return the location of the players exit point
     *
     * @return org.bukkit.Location Location of the exit point
     */
    public Location getExit() {
        return exit;
    }

    /**
     * Set the location of the players exit point
     */
    public void setExit(Location loc) {
        this.exit = loc;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}
