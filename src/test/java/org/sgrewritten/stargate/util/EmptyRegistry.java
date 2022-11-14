package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.sgrewritten.stargate.gate.structure.GateStructureType;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.PortalType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.RealPortal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmptyRegistry implements RegistryAPI {

    @Override
    public void loadPortals() {
    }

    @Override
    public void removePortal(Portal portal, PortalType portalType) {
    }

    @Override
    public void savePortal(RealPortal portal, PortalType portalType) {
    }

    @Override
    public void updateAllPortals() {
    }

    @Override
    public void updatePortals(Map<String, ? extends Network> networkMap) {
    }

    @Override
    public RealPortal getPortal(BlockLocation blockLocation, GateStructureType structureType) {
        return null;
    }

    @Override
    public RealPortal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes) {
        return null;
    }

    @Override
    public RealPortal getPortal(Location location, GateStructureType structureType) {
        return null;
    }

    @Override
    public RealPortal getPortal(Location location, GateStructureType[] structureTypes) {
        return null;
    }

    @Override
    public RealPortal getPortal(Location location) {
        return null;
    }

    @Override
    public boolean isPartOfPortal(List<Block> blocks) {
        return false;
    }

    @Override
    public boolean isNextToPortal(Location location, GateStructureType structureType) {
        return false;
    }

    @Override
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, RealPortal> locationsMap) {

    }

    @Override
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation) {
    }

    @Override
    public void createNetwork(String networkName, Set<PortalFlag> flags) {
    }

    @Override
    public boolean networkExists(String networkName, boolean isBungee) {
        return false;
    }

    @Override
    public Network getNetwork(String name, boolean isBungee) {
        return null;
    }

    @Override
    public HashMap<String, Network> getBungeeNetworkMap() {
        return null;
    }

    @Override
    public HashMap<String, Network> getNetworkMap() {
        return null;
    }

}
