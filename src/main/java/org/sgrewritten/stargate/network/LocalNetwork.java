package org.sgrewritten.stargate.network;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.StorageType;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.property.BypassPermission;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A network of portals
 */
public class LocalNetwork implements Network {

    public static final String DEFAULT_NET_ID = "<@default@>";
    
    protected Map<String, Portal> nameToPortalMap;
    protected SQLDatabaseAPI database;
    protected String name;
    protected String id;
    protected RegistryAPI registry;

    private NetworkType networkType;

    /**
     * Instantiates a new network
     *
     * @param name <p>The name of the new network</p>
     * @param flags <p>The flags attached to this network</p>
     * @throws InvalidNameException <p>If the network name is invalid</p>
     * @throws NameLengthException 
     */
    public LocalNetwork(String name, Set<PortalFlag> flags) throws InvalidNameException, NameLengthException {
        this(name, NetworkType.getNetworkTypeFromFlags(flags));
    }
    
    public LocalNetwork(String name, NetworkType type) throws InvalidNameException, NameLengthException {
        load(name,type);
    }
    
    private void load(String name, NetworkType type) throws InvalidNameException, NameLengthException {
        Objects.requireNonNull(name);
        this.networkType = Objects.requireNonNull(type);
        switch(type) {
        case DEFAULT:
            loadAsDefault(name);
            break;
        case PERSONAL:
            loadAsPersonalNetwork(name);
            break;
        case CUSTOM:
            loadAsCustomNetwork(name);
        default:
            break;
        }
        nameToPortalMap = new HashMap<>();
    }
    
    private void loadAsDefault(String name) throws InvalidNameException {
        this.name = ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK);
        if(!DEFAULT_NET_ID.equals(name)) {
            throw new InvalidNameException("Invalid name '"+ name + "' can not be default network, expected name '" + DEFAULT_NET_ID + "'");//TODO refactor NameErrorException to multimple errors
        }
        id = DEFAULT_NET_ID;
    }
    
    private void loadAsCustomNetwork(String networkName) throws NameLengthException {
        networkName = NameHelper.getTrimmedName(networkName);
        if (!NameHelper.isValidName(networkName)) {
            throw new NameLengthException("Name '" + networkName + "' is to short or to long, expected length over 0 and under " + Stargate.getMaxTextLength());
        }
        this.name = networkName.trim();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.DISABLE_CUSTOM_COLORED_NAMES)) {
            this.name = ChatColor.stripColor(this.name);
        }
        id = NameHelper.getNormalizedName(this.name);
    }
    
    private void loadAsPersonalNetwork(String uuidString) {
        Stargate.log(Level.FINER, "Initialized personal network with UUID " + uuidString);
        String possiblePlayerName = Bukkit.getOfflinePlayer(UUID.fromString(uuidString)).getName();
        Stargate.log(Level.FINER, "Matching player name: " + possiblePlayerName);
        if (possiblePlayerName != null
                && (NetworkCreationHelper.getDefaultNamesTaken().contains(possiblePlayerName.toLowerCase())
                || NetworkCreationHelper.getBannedNames().contains(possiblePlayerName.toLowerCase()))) {
            possiblePlayerName = uuidString.split("-")[0];
        }
        name = possiblePlayerName;
        id = uuidString;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<Portal> getAllPortals() {
        return nameToPortalMap.values();
    }

    @Override
    public Portal getPortal(String name) {
        if (name == null) {
            return null;
        }
        return nameToPortalMap.get(NameHelper.getNormalizedName(name));
    }

    @Override
    public void removePortal(Portal portal, boolean removeFromDatabase) {
        nameToPortalMap.remove(portal.getID());
        if (!removeFromDatabase) {
            return;
        }
        registry.removePortal(portal, StorageType.LOCAL);
    }

    @Override
    public void addPortal(Portal portal, boolean saveToDatabase) throws NameConflictException {
        if (isPortalNameTaken(portal.getName())) {
            throw new NameConflictException("portal of name '" + portal.getName() + "' already exist in network '" + this.getId() + "'");
        }
        if (portal instanceof RealPortal) {
            RealPortal realPortal = (RealPortal) portal;
            for (GateStructureType key : GateStructureType.values()) {
                List<BlockLocation> locations = realPortal.getGate().getLocations(key);
                if (locations == null) {
                    continue;
                }
                registry.registerLocations(key, generateLocationMap(locations, (RealPortal) portal));
            }
            if (saveToDatabase) {
                savePortal((RealPortal) portal);
            }
        }
        nameToPortalMap.put(portal.getID(), portal);
    }

    @Override
    public boolean isPortalNameTaken(String name) {
        return nameToPortalMap.containsKey(NameHelper.getNormalizedName(name));
    }

    @Override
    public void updatePortals() {
        for (String portal : nameToPortalMap.keySet()) {
            getPortal(portal).updateState();
        }
    }

    @Override
    public Set<String> getAvailablePortals(Player player, Portal requester) {
        Set<String> tempPortalList = new HashSet<>(nameToPortalMap.keySet());
        tempPortalList.remove(requester.getID());
        if (!requester.hasFlag(PortalFlag.FORCE_SHOW)) {
            Set<String> removeList = new HashSet<>();
            for (String portalName : tempPortalList) {
                Portal target = getPortal(portalName);
                if (target.hasFlag(PortalFlag.HIDDEN) && !playerCanSeeHiddenPortal(target, player)) {
                    removeList.add(portalName);
                }
                if (target.hasFlag(PortalFlag.PRIVATE) && !playerCanSeePrivatePortal(target, player)) {
                    removeList.add(portalName);
                }
            }
            tempPortalList.removeAll(removeList);
        }
        return tempPortalList;
    }

    private boolean playerCanSeeHiddenPortal(Portal portalToSee, Player player) {
        return player != null && (player.hasPermission(BypassPermission.HIDDEN.getPermissionString())
                || portalToSee.getOwnerUUID().equals(player.getUniqueId()));
    }

    private boolean playerCanSeePrivatePortal(Portal portalToSee, Player player) {
        return player != null && (player.hasPermission(BypassPermission.PRIVATE.getPermissionString())
                || player.getUniqueId().equals(portalToSee.getOwnerUUID()));
    }

    @Override
    public HighlightingStyle getHighlightingStyle() {
        return networkType.getHighlightingStyle();
    }

    @Override
    public void destroy() {
        for (String portalName : nameToPortalMap.keySet()) {
            Portal portal = nameToPortalMap.get(portalName);
            portal.destroy();
        }
        nameToPortalMap.clear();
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int size() {
        return this.getAllPortals().size();
    }

    /**
     * Saves the given portal to the database
     *
     * @param portal <p>The portal to save</p>
     */
    protected void savePortal(RealPortal portal) {
        registry.savePortal(portal, StorageType.LOCAL);
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
    public void assignToRegistry(RegistryAPI registry) {
        this.registry = registry;
    }

    @Override
    public NetworkType getType() {
        return networkType;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }

    @Override
    public void setID(String newName) throws InvalidNameException, NameLengthException {
        load(newName,this.getType());
    }

}
