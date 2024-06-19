package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLine;

public class StargateSignFormatPortalEvent extends StargatePortalEvent {
    private static final HandlerList handlers = new HandlerList();
    private final PortalPosition portalPosition;
    private final Location location;
    private final SignLine[] lines;

    public StargateSignFormatPortalEvent(@NotNull RealPortal portal, SignLine[] signLines, PortalPosition portalPosition, Location location) {
        super(portal, false);
        this.lines = signLines;
        this.portalPosition = portalPosition;
        this.location = location;
    }

    /**
     * @return <p> The sign lines to format</p>
     */
    public SignLine[] getLines() {
        return this.lines;
    }

    /**
     * @return The sign that is being formatted
     * @throws IllegalStateException <p>If no sign was found at the current location</p>
     */
    public Sign getSign() {
        Block block = location.getBlock();
        if (block.getState() instanceof Sign sign) {
            return sign;
        }
        throw new IllegalStateException("Could not find any sign at " + location + ", found " + block.getType());
    }

    /**
     * @return <p>The portal position of the sign that will be formatted</p>
     */
    public PortalPosition getPortalPosition() {
        return this.portalPosition;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
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
