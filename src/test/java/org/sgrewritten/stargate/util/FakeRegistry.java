package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.structure.GateStructureType;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
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
    public void removePortal(Portal portal, StorageType portalType) {
    }

    @Override
    public void savePortal(RealPortal portal, StorageType portalType) {
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
    public Network createNetwork(String networkName, NetworkType type, boolean isInterserver, boolean isForced)
            throws InvalidNameException, NameLengthException {
        networkName = NameHelper.getTrimmedName(networkName);
        if (this.networkExists(networkName, isInterserver)) {
            if (isForced && type == NetworkType.DEFAULT) {
                Network network = this.getNetwork(networkName, isInterserver);
                if(network.getType() != type) {
                    this.rename(network);
                }
            }
            throw new InvalidNameException(null);
        }
        Network network = isInterserver ? new InterServerNetwork(networkName,type) : new LocalNetwork(networkName, type);
        getNetworkMap(isInterserver).put(network.getId(), network);
        Stargate.log(Level.FINEST, String.format("Adding networkid %s to interServer = %b", network.getId(), isInterserver));
        return network;
    }
    
    @Override
    public Network createNetwork(String targetNetwork, Set<PortalFlag> flags, boolean isForced) throws InvalidNameException, NameLengthException {
        return this.createNetwork(targetNetwork, NetworkType.getNetworkTypeFromFlags(flags),flags.contains(PortalFlag.FANCY_INTER_SERVER),isForced);
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

    @Override
    public void rename(Network network, String newName) throws InvalidNameException {
    }

    @Override
    public void rename(Portal portal, String newName) throws InvalidNameException {
    }

    @Override
    public void rename(Network network) {
    }

}
