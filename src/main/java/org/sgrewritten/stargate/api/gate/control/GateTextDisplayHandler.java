package org.sgrewritten.stargate.api.gate.control;

import org.bukkit.DyeColor;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

public interface GateTextDisplayHandler extends ControlMechanism{

    public void displayText(String[] lines);

    void setSignColor(DyeColor color, RealPortal portal);
}
