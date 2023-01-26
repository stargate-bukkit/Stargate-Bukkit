package org.sgrewritten.stargate.api.event;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;

/**
 * An event run every time a Stargate's sign is formatted
 */
public class StargateSignFormatEvent extends StargateEvent {

    private LineFormatter formatter;
    private final DyeColor signColor;

    /**
     * Instantiates a new stargate sign format event
     *
     * @param portal    <p>The portal whose sign is about to be formatted</p>
     * @param formatter <p>The line formatter to use for formatting</p>
     * @param signColor <p>The current color of the sign</p>
     */
    public StargateSignFormatEvent(@NotNull RealPortal portal, LineFormatter formatter, DyeColor signColor) {
        super(portal);
        this.formatter = formatter;
        this.signColor = signColor;
    }

    /**
     * The line formatter to use for formatting the sign's lines
     *
     * @return <p>The line formatter to use</p>
     */
    public LineFormatter getLineFormatter() {
        return formatter;
    }

    /**
     * Sets the line formatter to use for formatting the sign's lines
     *
     * @param formatter <p>The formatter to be used</p>
     */
    public void setLineFormatter(LineFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Gets the color of the formatted sign
     *
     * @return <p>The color of the formatted sign</p>
     */
    public DyeColor getSignColor() {
        return signColor;
    }

}
