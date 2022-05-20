package net.TheDgtl.Stargate.event;

import net.TheDgtl.Stargate.network.portal.Portal;
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
public class StargatePortalEvent extends StargateEvent {

    private static final HandlerList handlers = new HandlerList();
    final Entity travellingEntity;
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
    public StargatePortalEvent(@NotNull Entity travellingEntity, @NotNull Portal portal, Portal destination,
                               @NotNull Location exit) {
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
    public Location getExit() {
        return exit;
    }

    /**
     * Set the location of the entity's exit point
     *
     * @param location <p>The new location of the entity's exit point</p>
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

/*
@Override
    public List<Permission> getRelatedPerms() {
        String identifier = "sg.use";
        List<Permission> permList = new ArrayList<>();
        if (target instanceof Player) {
            if (!portal.isOpenFor(target)) {
                permList.add(Bukkit.getPluginManager().getPermission(identifier + ".follow"));
            }
            if (portal.hasFlag(PortalFlag.PRIVATE) && !portal.getOwnerUUID().equals(target.getUniqueId())) {
                permList.add(Bukkit.getPluginManager().getPermission("sg.admin.bypass.private"));
            }
        }

        return permList;
    }
 */