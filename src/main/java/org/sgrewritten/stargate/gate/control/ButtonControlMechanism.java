package org.sgrewritten.stargate.gate.control;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.gate.control.ControlMechanism;
import org.sgrewritten.stargate.api.gate.control.GateActivationHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.util.ButtonHelper;

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
    public boolean onBlockClick(PlayerInteractEvent event, RealPortal portal) {
        portal.onButtonClick(event);
        event.setUseInteractedBlock(Event.Result.DENY);
        return true;
    }

    @Override
    public MechanismType getType() {
        return MechanismType.BUTTON;
    }

    @Override
    public void drawButton(Material buttonMaterial, BlockFace facing) {
        Block button = gate.getLocation(positionLocation).getBlock();
        if (ButtonHelper.isButton(button.getType())) {
            return;
        }
        Stargate.log(Level.FINEST, "buttonMaterial: " + buttonMaterial);
        Directional buttonData = (Directional) Bukkit.createBlockData(buttonMaterial);
        buttonData.setFacing(facing);
        
        button.setBlockData(buttonData);
    }

    
}
