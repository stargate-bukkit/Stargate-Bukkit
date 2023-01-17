package org.sgrewritten.stargate.gate.control;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.BlockSetAction;
import org.sgrewritten.stargate.api.gate.control.GateTextDisplayHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.gate.Gate;

public class SignControlMechanism implements GateTextDisplayHandler{

    private @NotNull Gate gate;
    private @NotNull BlockVector positionLocation;

    public SignControlMechanism(@NotNull BlockVector positionLocation, @NotNull Gate gate) {
        this.gate = Objects.requireNonNull(gate);
        this.positionLocation = Objects.requireNonNull(positionLocation);
    }

    @Override
    public void displayText(String[] lines) {
        Location signLocation = gate.getLocation(this.positionLocation);
        BlockState signState = signLocation.getBlock().getState();
        if (!(signState instanceof Sign)) {
            Stargate.log(Level.FINE, "Could not find sign at position " + signLocation);
            return;
        }
        Sign sign = (Sign) signState;
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, lines[i]);
        }
        Stargate.addSynchronousTickAction(new BlockSetAction(sign, true));
    }

    @Override
    public MechanismType getType() {
        return MechanismType.SIGN;
    }
}
