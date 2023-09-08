package org.sgrewritten.stargate.api.event;

import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.network.portal.formatting.LineFormatter;

public class StargateSignFormatEvent extends StargateEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Sign sign;
    private LineFormatter formatter;
    private final DyeColor signColor;

    public StargateSignFormatEvent(@NotNull RealPortal portal, LineFormatter formatter, DyeColor signColor, Sign sign) {
        super(portal);
        this.formatter = formatter;
        this.signColor = signColor;
        this.sign = sign;
    }

    public LineFormatter getLineFormatter() {
        return formatter;
    }

    public void setLineFormatter(LineFormatter formatter) {
        this.formatter = formatter;
    }

    public DyeColor getSignColor() {
        return signColor;
    }

    public Sign getSign(){
        return sign;
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
