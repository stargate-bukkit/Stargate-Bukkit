package org.sgrewritten.stargate.api.event.gate;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.format.SignLine;

public class StargateSignFormatGateEvent extends StargateGateEvent {
    private static final HandlerList handlers = new HandlerList();
    private final PortalPosition portalPosition;
    private final Location location;
    private final SignLine[] lines;

    public StargateSignFormatGateEvent(@NotNull GateAPI gate, SignLine[] signLines, PortalPosition portalPosition, Location location) {
        super(gate);
        this.lines = signLines;
        this.portalPosition = portalPosition;
        this.location = location;
    }

    /**
     * @return <p> The that will be formatted</p>
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
