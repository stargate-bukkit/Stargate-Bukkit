package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StargatePortalEvent extends StargateEvent {

    private final Player player;
    private final Portal destination;
    private Location exit;

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public StargatePortalEvent(Player player, Portal portal, Portal dest, Location exit) {
        super("StargatePortalEvent", portal);

        this.player = player;
        this.destination = dest;
        this.exit = exit;
    }

    /**
     * Return the player that went through the gate.
     *
     * @return player that went through the gate
     */
    public Player getPlayer() {
        return player;
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

}
