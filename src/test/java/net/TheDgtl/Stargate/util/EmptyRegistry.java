package net.TheDgtl.Stargate.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;

public class EmptyRegistry implements RegistryAPI{

    @Override
    public void loadPortals() {}

    @Override
    public void removePortal(Portal portal, PortalType portalType) { }

    @Override
    public void savePortal(RealPortal portal, PortalType portalType) {}

    @Override
    public void updateAllPortals() {}

    @Override
    public void updatePortals(Map<String, ? extends Network> networkMap) {}

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
    public void createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException {
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
