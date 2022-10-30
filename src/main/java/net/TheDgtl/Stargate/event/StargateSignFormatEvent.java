package net.TheDgtl.Stargate.event;

import org.bukkit.DyeColor;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.TheDgtl.Stargate.network.portal.formatting.LineFormatter;

public class StargateSignFormatEvent extends StargateEvent{
    private static final HandlerList handlers = new HandlerList();
    private LineFormatter formatter;
    private final DyeColor signColor;

    public StargateSignFormatEvent(@NotNull RealPortal portal, LineFormatter formatter, DyeColor signColor) {
        super(portal);
        this.formatter = formatter;
        this.signColor = signColor;
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
    
    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}
