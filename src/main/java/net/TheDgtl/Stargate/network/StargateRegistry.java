package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.StorageAPI;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Register of all portals and networks
 *
 * @author Thorin (idea from EpicKnarvik)
 */
public class StargateRegistry implements StargateRegistryAPI{

    private final StorageAPI storageAPI;
    private final HashMap<String, Network> networkList = new HashMap<>();
    private final HashMap<String, InterServerNetwork> bungeeNetworkList = new HashMap<>();
    private final Map<GateStructureType, Map<BlockLocation, Portal>> portalFromStructureTypeMap = new EnumMap<>(GateStructureType.class);

    /**
     * Instantiates a new Stargate registry
     *
     * @param storageAPI <p>The database API to use for interfacing with the database</p>
     */
    public StargateRegistry(StorageAPI storageAPI) {
        this.storageAPI = storageAPI;
    }

    @Override
    public void loadPortals() {
        storageAPI.loadFromStorage();
        updateAllPortals();
    }

    @Override
    public void removePortal(Portal portal, PortalType portalType) {
        storageAPI.removePortalFromStorage(portal, portalType);
    }

    @Override
    public void savePortal(RealPortal portal, PortalType portalType) {
        storageAPI.savePortalToStorage(portal, portalType);
    }

    @Override
    public void createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException {
        storageAPI.createNetwork(networkName, flags);
    }

    @Override
    public void updateAllPortals() {
        updatePortals(getNetworkList());
        updatePortals(getBungeeNetworkList());
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
        return getNetworkMap(isBungee).get(name);
    }

    @Override
    public Portal getPortal(BlockLocation blockLocation, GateStructureType structureType) {
        if (!(portalFromStructureTypeMap.containsKey(structureType))) {
            return null;
        }
        return portalFromStructureTypeMap.get(structureType).get(blockLocation);
    }

    @Override
    public Portal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes) {
        for (GateStructureType key : structureTypes) {
            Portal portal = getPortal(blockLocation, key);
            if (portal != null) {
                return portal;
            }
        }
        return null;
    }

    @Override
    public Portal getPortal(Location location, GateStructureType structureType) {
        return getPortal(new BlockLocation(location), structureType);
    }

    @Override
    public Portal getPortal(Location location, GateStructureType[] structureTypes) {
        return getPortal(new BlockLocation(location), structureTypes);
    }
    
    @Override
    public Portal getPortal(Location location) {
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
        BlockVector adjacentVector = new BlockVector(1, 0, 0);
        for (int i = 0; i < 4; i++) {
            Location adjacentLocation = location.clone().add(adjacentVector);
            if (getPortal(adjacentLocation, structureType) != null) {
                return true;
            }
            adjacentVector.rotateAroundY(Math.PI / 2);
        }
        return false;
    }


    @Override
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, Portal> locationsMap) {
        if (!portalFromStructureTypeMap.containsKey(structureType)) {
            portalFromStructureTypeMap.put(structureType, new HashMap<>());
        }
        portalFromStructureTypeMap.get(structureType).putAll(locationsMap);
    }

    @Override
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation) {
        Map<BlockLocation, Portal> map = portalFromStructureTypeMap.get(structureType);
        if (map != null) {
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
    private Map<String, ? extends Network> getNetworkMap(boolean getBungee) {
        if (getBungee) {
            return getBungeeNetworkList();
        } else {
            return getNetworkList();
        }
    }

    @Override
    public HashMap<String, InterServerNetwork> getBungeeNetworkList() {
        return bungeeNetworkList;
    }

    @Override
    public HashMap<String, Network> getNetworkList() {
        return networkList;
    }

}
