package org.sgrewritten.stargate.gate.control;

import java.util.Objects;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.gate.control.ControlMechanism;
import org.sgrewritten.stargate.api.gate.control.GateActivationHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.gate.Gate;

public class ButtonControlMechanism extends GatePosition implements GateActivationHandler {

    private boolean active;
    private @NotNull Gate gate;

    public ButtonControlMechanism(@NotNull BlockVector positionLocation, @NotNull Gate gate) {
        super(positionLocation);
        this.gate = Objects.requireNonNull(gate);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void onBlockClick(PlayerInteractEvent event) {
        
    }

    @Override
    public MechanismType getType() {
        return MechanismType.BUTTON;
    }

}
