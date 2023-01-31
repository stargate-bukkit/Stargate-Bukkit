package org.sgrewritten.stargate.gate.control;

import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.gate.control.GateActivationHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;

import java.util.UUID;

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
    public @Nullable UUID getActivator() {
        return null;
    }

}
