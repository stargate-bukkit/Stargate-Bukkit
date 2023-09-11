package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * This event should be called whenever a non-player teleports through a stargate
 *
 * <p>This event can be used to overwrite the location the entity is teleported to.</p>
 */
@SuppressWarnings("unused")
public class StargateTeleportPortalEvent extends StargateEntityPortalEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Portal destination;
    private Location exit;

    /**
     * Instantiates a new stargate portal event
     *
     * @param travellingEntity <p>The entity travelling through a portal</p>
     * @param portal           <p>The portal the entity entered from</p>
     * @param destination      <p>The destination the entity should exit from</p>
     * @param exit             <p>The exit location of the destination portal the entity will be teleported to</p>
     */
    public StargateTeleportPortalEvent(@NotNull Entity travellingEntity, @NotNull Portal portal, Portal destination,
                                       @NotNull Location exit) {
        super(portal,travellingEntity);
        this.destination = destination;
        this.exit = exit;
    }

    /**
     * Gets the destination portal
     *
     * @return <p>The destination portal</p>
     */
    public Portal getDestination() {
        return destination;
    }

    /**
     * Gets the location the entity is about to be teleported to
     *
     * @return <p>The location the entity should exit from the portal</p>
     */
    public Location getExit() {
        return exit;
    }

    /**
     * Set the location the entity should exit from
     *
     * @param location <p>The new location the entity should exit from.</p>
     */
    public void setExit(@NotNull Location location) {
        this.exit = location;
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

}