package org.sgrewritten.stargate.gate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.format.SignLine;

import java.util.List;

public class GateMock implements GateAPI {
    @Override
    public void drawControlMechanisms(SignLine[] signLines, boolean drawButton) {

    }

    @Override
    public List<PortalPosition> getPortalPositions() {
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
        return null;
    }

    @Override
    public Vector getRelativeVector(Location location) {
        return new BlockVector(0,0,0);
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
    public void calculatePortalPositions(boolean alwaysOn) {

    }

    @Override
    public Location getTopLeft() {
        return null;
    }

    @Override
    public PortalPosition addPortalPosition(Location location, PositionType type, String pluginName) {
        return new PortalPosition(PositionType.BUTTON,new BlockVector(0,0,0),"Stargate");
    }

    @Override
    public void addPortalPosition(PortalPosition portalPosition) {

    }

    @Override
    public @Nullable PortalPosition removePortalPosition(Location location) {
        return null;
    }

    @Override
    public void removePortalPosition(PortalPosition portalPosition) {

    }

    @Override
    public void forceGenerateStructure() {

    }
}
