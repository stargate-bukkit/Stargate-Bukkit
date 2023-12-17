package org.sgrewritten.stargate.network;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.VectorUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Register of all portals and networks
 *
 * @author Thorin (idea from EpicKnarvik)
 */
public class StargateRegistry implements RegistryAPI {

    private final StorageAPI storageAPI;
    private final BlockHandlerResolver blockHandlerResolver;
    private final Map<String, Network> networkMap = new HashMap<>();
    private final Map<String, Network> bungeeNetworkMap = new HashMap<>();
    private final Map<GateStructureType, Map<BlockLocation, RealPortal>> portalFromStructureTypeMap = new EnumMap<>(GateStructureType.class);
    private final Map<BlockLocation, PortalPosition> portalPositionMap = new HashMap<>();
    private final Map<String, Map<BlockLocation, PortalPosition>> portalPositionPluginNameMap = new HashMap<>();
    private final Map<PortalPosition, RealPortal> portalPositionPortalRelation = new HashMap<>();

    /**
     * Instantiates a new Stargate registry
     *
     * @param storageAPI <p>The database API to use for interfacing with the database</p>
     */
    public StargateRegistry(StorageAPI storageAPI, BlockHandlerResolver blockHandlerResolver) {
        this.storageAPI = storageAPI;
        this.blockHandlerResolver = blockHandlerResolver;
    }

    @Override
    public void unregisterPortal(Portal portal) {
        if (portal instanceof RealPortal realPortal) {
            for (GateStructureType formatType : GateStructureType.values()) {
                for (BlockLocation loc : realPortal.getGate().getLocations(formatType)) {
                    Stargate.log(Level.FINEST, "Unregistering type: " + formatType + " location, at: " + loc);
                    this.unRegisterLocation(formatType, loc);
                }
            }
            GateAPI gate = realPortal.getGate();
            List<PortalPosition> portalPositions = gate.getPortalPositions();
            for (PortalPosition portalPosition : portalPositions) {
                Location location = gate.getLocation(portalPosition.getRelativePositionLocation());
                if (!portalPosition.getPluginName().equals("Stargate")) {
                    Stargate.log(Level.FINEST, "Unregistering non-Stargate portal position on location " + location.toString());
                    blockHandlerResolver.registerRemoval(this, location, realPortal);
                }
                Stargate.log(Level.FINEST, "Unregistering portal position on location " + location.toString());
                this.removePortalPosition(location);
            }
        }
    }

    @Override
    public void registerPortal(RealPortal portal) {
        GateAPI gate = portal.getGate();
        for (GateStructureType key : GateStructureType.values()) {
            List<BlockLocation> locations = gate.getLocations(key);
            if (locations == null) {
                continue;
            }
            this.registerLocations(key, generateLocationMap(locations, portal));
        }
        for (PortalPosition portalPosition : gate.getPortalPositions()) {
            Location location = gate.getLocation(portalPosition.getRelativePositionLocation());
            this.registerPortalPosition(portalPosition, location, portal);
        }
    }

    /**
     * Gets a map between the given block locations and the given portal
     *
     * @param locations <p>The locations related to the portal</p>
     * @param portal    <p>The portal with blocks at the given locations</p>
     * @return <p>The resulting location to portal mapping</p>
     */
    private Map<BlockLocation, RealPortal> generateLocationMap(List<BlockLocation> locations, RealPortal portal) {
        Map<BlockLocation, RealPortal> output = new HashMap<>();
        for (BlockLocation location : locations) {
            output.put(location, portal);
        }
        return output;
    }

    @Override
    public void updateAllPortals() {
        updatePortals(getNetworkMap());
        updatePortals(getBungeeNetworkMap());
    }

    @Override
    public void updatePortals(Map<String, ? extends Network> networkMap) {
        for (Network network : networkMap.values()) {
            network.updatePortals();
        }
    }

    @Override
    public boolean networkExists(String networkName, boolean isBungee) {
        return getNetwork(networkName, isBungee) != null;
    }

    @Override
    public Network getNetwork(String name, boolean isBungee) {
        String cleanName = NameHelper.getNormalizedName(NameHelper.getTrimmedName(name));
        return getNetworkMap(isBungee).get(cleanName);
    }

    @Override
    public RealPortal getPortal(BlockLocation blockLocation, GateStructureType structureType) {
        if (!(portalFromStructureTypeMap.containsKey(structureType))) {
            return null;
        }
        return portalFromStructureTypeMap.get(structureType).get(blockLocation);
    }

    @Override
    public RealPortal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes) {
        for (GateStructureType key : structureTypes) {
            RealPortal portal = getPortal(blockLocation, key);
            if (portal != null) {
                return portal;
            }
        }
        return null;
    }

    @Override
    public RealPortal getPortal(Location location, GateStructureType structureType) {
        return getPortal(new BlockLocation(location), structureType);
    }

    @Override
    public RealPortal getPortal(Location location, GateStructureType[] structureTypes) {
        return getPortal(new BlockLocation(location), structureTypes);
    }

    @Override
    public RealPortal getPortal(Location location) {
        PortalPosition portalPosition = this.getPortalPosition(location);
        if (portalPosition != null) {
            return this.getPortalFromPortalPosition(portalPosition);
        }
        return getPortal(location, GateStructureType.values());
    }


    @Override
    public boolean isPartOfPortal(List<Block> blocks) {
        for (Block block : blocks) {
            if (getPortal(block.getLocation()) != null) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isNextToPortal(Location location, GateStructureType structureType) {
        return !getPortalsFromTouchingBlock(location, structureType).isEmpty();
    }

    @Override
    public List<RealPortal> getPortalsFromTouchingBlock(Location location, GateStructureType structureType) {
        List<RealPortal> portals = new ArrayList<>();
        for (BlockVector adjacentVector : VectorUtils.getAdjacentRelativePositions()) {
            Location adjacentLocation = location.clone().add(adjacentVector);
            RealPortal portal = getPortal(adjacentLocation, structureType);
            if (portal != null) {
                portals.add(portal);
            }
        }
        return portals;
    }


    @Override
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, RealPortal> locationsMap) {
        if (!portalFromStructureTypeMap.containsKey(structureType)) {
            portalFromStructureTypeMap.put(structureType, new HashMap<>());
        }
        portalFromStructureTypeMap.get(structureType).putAll(locationsMap);
    }

    @Override
    public void registerLocation(GateStructureType structureType, BlockLocation location, RealPortal portal) {
        if (!portalFromStructureTypeMap.containsKey(structureType)) {
            portalFromStructureTypeMap.put(structureType, new HashMap<>());
        }
        portalFromStructureTypeMap.get(structureType).put(location, portal);
    }

    @Override
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation) {
        Map<BlockLocation, RealPortal> map = portalFromStructureTypeMap.get(structureType);
        if (map != null && map.get(blockLocation) != null) {
            Stargate.log(Level.FINER, "Unregistering portal " + map.get(blockLocation).getName() +
                    " with structType " + structureType + " at location " + blockLocation.toString());
            map.remove(blockLocation);
        }
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
    public Map<String, Network> getBungeeNetworkMap() {
        return bungeeNetworkMap;
    }

    @Override
    public Map<String, Network> getNetworkMap() {
        return networkMap;
    }

    public void clear(StargateAPI stargateAPI) {
        networkMap.clear();
        bungeeNetworkMap.clear();
        portalFromStructureTypeMap.clear();
        portalPositionMap.clear();
        portalPositionPluginNameMap.clear();
        portalFromStructureTypeMap.clear();
    }

    @Override
    public void updateName(Network network, String newId) throws InvalidNameException, NameLengthException, UnimplementedFlagException {
        if (ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> UUID.fromString(newId))) {
            throw new InvalidNameException("Can not rename the network to an UUID.");
        }
        Stargate.log(Level.FINE, String.format("Renaming network %s to %s", network.getName(), newId));
        // The network is stored in a map with its name as a key; this key needs to be updated properly.
        Map<String, Network> map = (network.getStorageType() == StorageType.INTER_SERVER) ? this.bungeeNetworkMap : this.networkMap;
        map.remove(network.getId());
        network.setID(newId);
        map.put(network.getId(), network);
        network.updatePortals();
    }

    @Override
    public String getValidNewName(Network network) throws InvalidNameException {
        //noinspection ResultOfMethodCallIgnored
        if (ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> UUID.fromString(network.getId()))) {
            throw new InvalidNameException("Can not rename the network as it's name is an UUID.");
        }
        String newName = network.getId();
        int i = 1;
        try {
            boolean isInterServer = (network.getStorageType() == StorageType.INTER_SERVER);
            while (networkExists(newName, isInterServer) || storageAPI.netWorkExists(newName, network.getStorageType())) {
                newName = network.getId() + i;
                i++;
            }
            if (newName.length() < Stargate.getMaxTextLength()) {
                return newName;
            }
            String annoyinglyOverThoughtName = network.getId();
            int n = 1;
            while (networkExists(annoyinglyOverThoughtName, isInterServer) || storageAPI.netWorkExists(newName, network.getStorageType())) {
                String number = String.valueOf(n);
                annoyinglyOverThoughtName = network.getId().substring(0, network.getId().length() - number.length())
                        + number;
            }
            return annoyinglyOverThoughtName;
        } catch (StorageReadException e) {
            Stargate.log(e);
        }
        throw new InvalidNameException("Unable to find a valid name");
    }

    @Override
    public Map<BlockLocation, PortalPosition> getPortalPositions() {
        return this.portalPositionMap;
    }

    @Override
    public Map<BlockLocation, PortalPosition> getPortalPositionsOwnedByPlugin(Plugin plugin) {
        this.portalPositionPluginNameMap.putIfAbsent(plugin.getName(), new HashMap<>());
        return this.portalPositionPluginNameMap.get(plugin.getName());
    }

    @Override
    public PortalPosition savePortalPosition(RealPortal portal, Location location, PositionType type, Plugin plugin) {
        BlockVector relativeVector = portal.getGate().getRelativeVector(location).toBlockVector();
        PortalPosition portalPosition = new PortalPosition(type, relativeVector, plugin.getName());
        try {
            storageAPI.addPortalPosition(portal, portal.getStorageType(), portalPosition);
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
        return portalPosition;
    }

    @Override
    public void removePortalPosition(Location location) {
        BlockLocation blockLocation = new BlockLocation(location);
        PortalPosition portalPosition = portalPositionMap.get(blockLocation);
        if (portalPosition == null) {
            return;
        }
        portalPositionMap.remove(blockLocation);
        portalPositionPluginNameMap.get(portalPosition.getPluginName()).remove(blockLocation);
        RealPortal portal = portalPositionPortalRelation.remove(portalPosition);
        portal.getGate().removePortalPosition(portalPosition);
        try {
            storageAPI.removePortalPosition(portal, portal.getStorageType(), portalPosition);
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public void registerPortalPosition(PortalPosition portalPosition, Location location, RealPortal portal) {
        Stargate.log(Level.FINEST, String.format("Registering portal position at %s for portal %s", location.toString(), portal.getName()));
        BlockLocation blockLocation = new BlockLocation(location);
        portalPositionMap.put(blockLocation, portalPosition);
        portalPositionPluginNameMap.putIfAbsent(portalPosition.getPluginName(), new HashMap<>());
        portalPositionPluginNameMap.get(portalPosition.getPluginName()).put(blockLocation, portalPosition);
        portalPositionPortalRelation.put(portalPosition, portal);
        portal.getGate().addPortalPosition(portalPosition);
    }

    @Override
    public PortalPosition getPortalPosition(Location location) {
        return portalPositionMap.get(new BlockLocation(location));
    }

    @Override
    public @Nullable RealPortal getPortalFromPortalPosition(PortalPosition portalPosition) {
        return portalPositionPortalRelation.get(portalPosition);
    }

    @Override
    public void registerNetwork(Network network) {
        network.assignToRegistry(this);
        getNetworkMap(network.getStorageType() == StorageType.INTER_SERVER).put(network.getId(), network);
    }

    @Override
    public void renameNetwork(String newId, String oldId, boolean isInterServer) throws InvalidNameException, UnimplementedFlagException, NameLengthException {
        Map<String,Network> networks = getNetworkMap(isInterServer);
        Network network = networks.remove(oldId);
        if(network == null){
            throw new InvalidNameException("Name does not exist, can not rename: " + oldId);
        }
        network.setID(NameHelper.getNormalizedName(newId));
        networks.put(network.getId(), network);
    }

}
