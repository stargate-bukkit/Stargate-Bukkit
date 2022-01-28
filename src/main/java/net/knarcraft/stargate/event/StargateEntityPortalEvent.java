package net.knarcraft.stargate.event;

import net.knarcraft.stargate.portal.Portal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a non-player teleports through a stargate
 *
 * <p>This event can be used to overwrite the location the entity is teleported to.</p>
 */
@SuppressWarnings("unused")
public class StargateEntityPortalEvent extends StargateEvent implements StargateTeleportEvent {

    private static final HandlerList handlers = new HandlerList();
    final Entity travellingEntity;
    private final Portal destination;
    private Location exit;

    /**
     * Instantiates a new stargate portal event
     *
     * @param travellingEntity <p>The entity travelling through this portal</p>
     * @param portal           <p>The portal the entity entered from</p>
     * @param destination      <p>The destination the entity should exit from</p>
     * @param exit             <p>The exit location of the destination portal the entity will be teleported to</p>
     */
    public StargateEntityPortalEvent(Entity travellingEntity, Portal portal, Portal destination, Location exit) {
        super(portal);

        this.travellingEntity = travellingEntity;
        this.destination = destination;
        this.exit = exit;
    }

    /**
     * Return the non-player entity teleporting
     *
     * @return <p>The non-player teleporting</p>
     */
    public Entity getEntity() {
        return travellingEntity;
    }

    /**
     * Return the destination portal
     *
     * @return <p>The destination portal</p>
     */
    public Portal getDestination() {
        return destination;
    }

    /**
     * Return the location of the players exit point
     *
     * @return <p>Location of the exit point</p>
     */
    @Override
    public Location getExit() {
        return exit;
    }

    /**
     * Set the location of the entity's exit point
     *
     * @param location <p>The new location of the entity's exit point</p>
     */
    public void setExit(Location location) {
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
