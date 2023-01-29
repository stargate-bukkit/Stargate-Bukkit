package org.sgrewritten.stargate.api.gate.control;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.FormattableObject;

public interface GateTextDisplayHandler extends ControlMechanism {

    /**
     * Set button and draw sign
     *
     * @param lines  <p>an array with 4 elements, representing each line of a sign</p>
     * @param portal <p>The portal that is related to this ControlMechanism</p>
     */
    public void displayText(@NotNull FormattableObject[] lines, @NotNull RealPortal portal);


    /**
     * Change the text color of the display. Note that this is provided by the
     * {@link DyeColor} as the main way of displaying text is by using signs
     *
     * @param color  <p>The color the text should display in</p>
     * @param portal <p>The portal that is related to this ControlMechanism</p>
     */
    void setTextColor(@NotNull DyeColor color, @NotNull RealPortal portal);
}
