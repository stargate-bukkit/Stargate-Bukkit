package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.database.StorageAPI;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import net.md_5.bungee.api.ChatColor;
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
public class StargateRegistry implements RegistryAPI {

    private final StorageAPI storageAPI;
    private final HashMap<String, Network> networkMap = new HashMap<>();
    private final HashMap<String, Network> bungeeNetworkMap = new HashMap<>();
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
        if (this.networkExists(networkName, flags.contains(PortalFlag.FANCY_INTER_SERVER))) {
            throw new NameErrorException(null);
        }
        Network network = storageAPI.createNetwork(networkName, flags);
        network.assignToRegistry(this);
        getNetworkMap(flags.contains(PortalFlag.FANCY_INTER_SERVER)).put(network.getId(), network);
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
        String cleanName = name.trim().toLowerCase();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.DISABLE_CUSTOM_COLORED_NAMES)) {
            cleanName = ChatColor.stripColor(cleanName);
        }
        return getNetworkMap(isBungee).get(cleanName);
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
