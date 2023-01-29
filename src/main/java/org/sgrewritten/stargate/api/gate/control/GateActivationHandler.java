package org.sgrewritten.stargate.api.gate.control;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public interface GateActivationHandler extends ControlMechanism {

    public boolean isActive();

    public void drawButton(Material buttonMaterial, BlockFace facing);
}
