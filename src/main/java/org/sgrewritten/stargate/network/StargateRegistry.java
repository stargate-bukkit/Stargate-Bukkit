package org.sgrewritten.stargate.network;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.api.structure.GateStructureType;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;
import org.sgrewritten.stargate.vectorlogic.VectorUtils;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Register of all portals and networks
 *
 * @author Thorin (idea from EpicKnarvik)
 */
public class StargateRegistry implements RegistryAPI {

    private final StorageAPI storageAPI;
    private final HashMap<String, Network> networkMap = new HashMap<>();
    private final HashMap<String, Network> bungeeNetworkMap = new HashMap<>();
    private final Map<GateStructureType, Map<BlockLocation, RealPortal>> portalFromStructureTypeMap = new EnumMap<>(GateStructureType.class);

    /**
     * Instantiates a new Stargate registry
     *
     * @param storageAPI <p>The database API to use for interfacing with the database</p>
     */
    public StargateRegistry(StorageAPI storageAPI) {
        this.storageAPI = storageAPI;
    }

    @Override
    public void loadPortals(StargateEconomyAPI economyManager) {
        try {
            storageAPI.loadFromStorage(this, economyManager);
        } catch (StorageReadException e) {
            Stargate.log(e);
            return;
        }
        Stargate.addSynchronousTickAction(new SupplierAction(() -> {
            updateAllPortals();
            return true;
        }));
    }

    @Override
    public void removePortal(Portal portal, StorageType portalType) {
        try {
            storageAPI.removePortalFromStorage(portal, portalType);
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }

        if (portal instanceof RealPortal realPortal) {
            for (GateStructureType formatType : GateStructureType.values()) {
                for (BlockLocation loc : realPortal.getGate().getLocations(formatType)) {
                    Stargate.log(Level.FINEST, "Unregistering type: " + formatType + " location, at: " + loc);
                    this.unRegisterLocation(formatType, loc);
                }
            }
        }
    }

    @Override
    public void savePortal(RealPortal portal, StorageType portalType) {
        try {
            storageAPI.savePortalToStorage(portal, portalType);
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public Network createNetwork(String networkName, NetworkType type, boolean isInterserver, boolean isForced)
            throws InvalidNameException, NameLengthException, NameConflictException, UnimplementedFlagException {
        if (this.networkExists(networkName, isInterserver)
                || this.networkExists(NetworkCreationHelper.getPlayerUUID(networkName).toString(), isInterserver)) {
            if (isForced && type == NetworkType.DEFAULT) {
                Network network = this.getNetwork(networkName, isInterserver);
                if (network != null && network.getType() != type) {
                    this.rename(network);
                }
            }
            throw new NameConflictException("network of id '" + networkName + "' already exists", true);
        }
        Network network = storageAPI.createNetwork(networkName, type, isInterserver);
        network.assignToRegistry(this);
        getNetworkMap(isInterserver).put(network.getId(), network);
        Stargate.log(
                Level.FINEST, String.format("Adding networkid %s to interServer = %b", network.getId(), isInterserver));
        return network;
    }

    @Override
    public Network createNetwork(String targetNetwork, Set<PortalFlag> flags, boolean isForced) throws InvalidNameException, NameLengthException, NameConflictException, UnimplementedFlagException {
        return this.createNetwork(targetNetwork, NetworkType.getNetworkTypeFromFlags(flags), flags.contains(PortalFlag.FANCY_INTER_SERVER), isForced);
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
        for (BlockVector adjacentVector : VectorUtils.getAdjacentRelativePositions()) {
            Location adjacentLocation = location.clone().add(adjacentVector);
            if (getPortal(adjacentLocation, structureType) != null) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, RealPortal> locationsMap) {
        if (!portalFromStructureTypeMap.containsKey(structureType)) {
            portalFromStructureTypeMap.put(structureType, new HashMap<>());
        }
        portalFromStructureTypeMap.get(structureType).putAll(locationsMap);
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
    public HashMap<String, Network> getBungeeNetworkMap() {
        return bungeeNetworkMap;
    }

    @Override
    public HashMap<String, Network> getNetworkMap() {
        return networkMap;
    }

    public void load(StargateEconomyAPI economyManager) {
        networkMap.clear();
        bungeeNetworkMap.clear();
        portalFromStructureTypeMap.clear();
        this.loadPortals(economyManager);
    }

    @Override
    public void rename(Network network, String newName) throws InvalidNameException, NameLengthException, UnimplementedFlagException {
        //noinspection ResultOfMethodCallIgnored
        if (ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> UUID.fromString(newName))) {
            throw new InvalidNameException("Can not rename the network to an UUID.");
        }

        Bukkit.getScheduler().runTaskAsynchronously(Stargate.getInstance(), () -> {
            try {
                storageAPI.updateNetworkName(newName, newName, network.getStorageType());
            } catch (StorageWriteException e) {
                Stargate.log(e);
            }
        });

        Stargate.log(Level.FINE, String.format("Renaming network %s to %s", network.getName(), newName));
        network.setID(newName);
        network.updatePortals();
    }

    @Override
    public void rename(Portal portal, String newName) {
        try {
            storageAPI.updatePortalName(newName, portal.getGlobalId(), portal.getStorageType());
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
        portal.setName(newName);
        portal.getNetwork().updatePortals();
    }

    @Override
    public void rename(Network network) throws InvalidNameException {
        //noinspection ResultOfMethodCallIgnored
        if (ExceptionHelper.doesNotThrow(IllegalArgumentException.class, () -> UUID.fromString(network.getId()))) {
            throw new InvalidNameException("Can not rename the network as it's name is an UUID.");
        }
        String newName = network.getId();
        int i = 1;
        try {
            boolean isInterServer = (network instanceof InterServerNetwork);
            while (networkExists(newName, isInterServer) || storageAPI.netWorkExists(newName, network.getStorageType())) {
                newName = network.getId() + i;
                i++;
            }
            try {
                rename(network, newName);
            } catch (InvalidNameException | NameLengthException | UnimplementedFlagException e) {
                String annoyinglyOverThoughtName = network.getId();
                int n = 1;
                while (networkExists(annoyinglyOverThoughtName, isInterServer) || storageAPI.netWorkExists(newName, network.getStorageType())) {
                    String number = String.valueOf(n);
                    annoyinglyOverThoughtName = network.getId().substring(0, network.getId().length() - number.length())
                            + number;
                }
                try {
                    rename(network, annoyinglyOverThoughtName);
                } catch (InvalidNameException | NameLengthException | UnimplementedFlagException impossible) {
                    Stargate.log(Level.SEVERE,
                            "Could not rename the network, do /sg trace and show the data in an new issue in sgrewritten.org/report");
                }
            }
        } catch (StorageReadException e) {
            Stargate.log(e);
        }
    }
}
