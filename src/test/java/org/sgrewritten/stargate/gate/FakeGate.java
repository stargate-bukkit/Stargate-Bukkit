package org.sgrewritten.stargate.gate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.gate.control.ControlMechanism;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;

import java.util.List;

public class FakeGate implements GateAPI {
    @Override
    public List<GatePosition> getPortalPositions() {
        return null;
    }

    @Override
    public List<BlockLocation> getLocations(GateStructureType structureType) {
        return null;
    }

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }

    @Override
    public Location getExit() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public GateFormatAPI getFormat() {
        return null;
    }

    @Override
    public BlockFace getFacing() {
        return BlockFace.SOUTH;
    }

    @Override
    public Vector getRelativeVector(Location location) {
        return new Vector();
    }

    @Override
    public boolean getFlipZ() {
        return false;
    }

    @Override
    public Location getLocation(@NotNull Vector vector) {
        return null;
    }

    @Override
    public Location getTopLeft() {
        return null;
    }

    @Override
    public void addPortalPosition(GatePosition pos) {

    }

    @Override
    public void setPortalControlMechanism(@NotNull ControlMechanism mechanism) {

    }

    @Override
    public @Nullable ControlMechanism getPortalControlMechanism(@NotNull MechanismType type) {
        return null;
    }

    @Override
    public GatePosition getPortalPosition(@NotNull Location location) {
        return null;
    }
}
