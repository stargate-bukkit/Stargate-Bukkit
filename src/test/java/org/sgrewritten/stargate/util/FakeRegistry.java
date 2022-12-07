package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.gate.structure.GateStructureType;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
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
import java.util.logging.Level;

public class FakeRegistry implements RegistryAPI {

    private HashMap<String, Network> bungeeNetworkMap = new HashMap<>();
    private HashMap<String, Network> networkMap = new HashMap<>();

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
    public Network createNetwork(String networkName, NetworkType type, boolean isInterserver) throws NameErrorException {
        networkName = NameHelper.getTrimmedName(networkName);
        if (this.networkExists(networkName, isInterserver)) {
            throw new NameErrorException(null);
        }
        Network network = isInterserver ? new InterServerNetwork(networkName,type) : new LocalNetwork(networkName, type); 
        Stargate.log(Level.FINEST, String.format("Adding networkid %s to interServer = %b", network.getId(), isInterserver));
        getNetworkMap(isInterserver).put(network.getId(), network);
        return network;
    }
    
    @Override
    public Network createNetwork(String targetNetwork, Set<PortalFlag> flags) throws NameErrorException {
        return this.createNetwork(targetNetwork, NetworkType.getNetworkTypeFromFlags(flags),flags.contains(PortalFlag.FANCY_INTER_SERVER));
    }

    @Override
    public boolean networkExists(String networkName, boolean isBungee) {
        return getNetwork(networkName, isBungee) != null;
    }

    @Override
    public Network getNetwork(String name, boolean isBungee) {
        return getNetworkMap(isBungee).get(name);
    }

    /**
     * Gets the map storing all networks of the given type
     *
     * @param getBungee <p>Whether to get BungeeCord networks</p>
     * @return <p>A network name -> network map</p>
     */
    private Map<String, Network> getNetworkMap(boolean getBungee) {
        if (getBungee) {
            return getBungeeNetworkMap();
        } else {
            return getNetworkMap();
        }
    }

    @Override
    public HashMap<String, Network> getBungeeNetworkMap() {
        return bungeeNetworkMap;
    }

    @Override
    public HashMap<String, Network> getNetworkMap() {
        return networkMap;
    }

}
