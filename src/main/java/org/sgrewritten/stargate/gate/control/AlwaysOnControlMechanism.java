package org.sgrewritten.stargate.gate.control;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.api.gate.control.GateActivationHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;

public class AlwaysOnControlMechanism implements GateActivationHandler {

    @Override
    public MechanismType getType() {
        return MechanismType.BUTTON;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void drawButton(Material buttonMaterial, BlockFace facing) {
        // Do nothing
    }

}
