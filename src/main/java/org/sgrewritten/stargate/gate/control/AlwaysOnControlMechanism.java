package org.sgrewritten.stargate.gate.control;

import org.bukkit.event.player.PlayerInteractEvent;
import org.sgrewritten.stargate.api.gate.control.GateActivationHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;

public class AlwaysOnControlMechanism implements GateActivationHandler{

    @Override
    public MechanismType getType() {
        return MechanismType.BUTTON;
    }

    @Override
    public boolean isActive() {
        return true;
    }
    
}
