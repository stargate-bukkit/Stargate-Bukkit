package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;

public class StargateSignDyeChangePortalEvent extends StargatePortalEvent {
    private static final HandlerList handlers = new HandlerList();
    private final DyeColor colorChange;
    private final Location location;
    private final PortalPosition portalPosition;

    /**
     * Instantiates a new stargate event
     *
     * @param portal <p>The portal involved in this stargate event</p>
     */
    public StargateSignDyeChangePortalEvent(@NotNull Portal portal, DyeColor colorChange, Location location, PortalPosition portalPosition) {
        super(portal, false);
        this.colorChange = colorChange;
        this.location = location;
        this.portalPosition = portalPosition;
    }

    /**
     * The location of the sign
     *
     * @return
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * @return <p>The new dye color of </p>
     */
    public DyeColor getColorChange() {
        return this.colorChange;
    }

    /**
     * @return <p>The portal position of the sign</p>
     */
    public PortalPosition getPortalPosition() {
        return this.portalPosition;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
