package org.sgrewritten.stargate.network;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.gate.structure.GateStructureType;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.property.BypassPermission;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A network of portals
 */
public class LocalNetwork implements Network {

    private static final String DEFAULT_NET_ID = "";
    
    protected Map<String, Portal> nameToPortalMap;
    protected SQLDatabaseAPI database;
    protected String name;
    protected String id;
    private RegistryAPI registry;

    private final NetworkType networkType;

    /**
     * Instantiates a new network
     *
     * @param name <p>The name of the new network</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public LocalNetwork(String name, Set<PortalFlag> flags) throws NameErrorException {
        this(name, getNetworkTypeFromFlags(flags));
    }
    
    public LocalNetwork(String name, NetworkType type) throws NameErrorException {
        this.networkType = type;
        
        switch(type) {
        case DEFAULT:
            loadAsDefault();
            break;
        case PERSONAL:
            loadAsPersonalNetwork(name);
            break;
        case CUSTOM:
            loadAsCustomNetwork(name);
            break;
        }
        
    }
    
    private static NetworkType getNetworkTypeFromFlags(Set<PortalFlag> flags) {
        if(flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            return NetworkType.PERSONAL;
        }
        if(flags.contains(PortalFlag.DEFAULT_NETWORK)) {
            return NetworkType.DEFAULT;
        }
        return NetworkType.CUSTOM;
    }
    
    private void loadAsDefault() {
        name = ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK);
        id = DEFAULT_NET_ID;
    }
    
    private void loadAsCustomNetwork(String networkName) throws NameErrorException {
        if (name.trim().isEmpty() || (name.length() >= Stargate.getMaxTextLength())) {
            throw new NameErrorException(TranslatableMessage.INVALID_NAME);
        }
        this.name = name.trim();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.DISABLE_CUSTOM_COLORED_NAMES)) {
            this.name = ChatColor.stripColor(this.name);
        }
        nameToPortalMap = new HashMap<>();
        id = NameHelper.getNormalizedName(this.name);
    }
    
    private void loadAsPersonalNetwork(String uuidString) {
        Stargate.log(Level.FINER, "Initialized personal network with UUID " + name);
        String possiblePlayerName = Bukkit.getOfflinePlayer(UUID.fromString(name)).getName();
        Stargate.log(Level.FINER, "Matching player name: " + possiblePlayerName);
        if (possiblePlayerName != null
                && (NetworkCreationHelper.getDefaultNamesTaken().contains(possiblePlayerName.toLowerCase())
                || NetworkCreationHelper.getBannedNames().contains(possiblePlayerName.toLowerCase()))) {
            possiblePlayerName = name.split("-")[0];
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
        Stargate.getRegistryStatic().removePortal(portal, PortalType.LOCAL);
    }

    @Override
    public void addPortal(Portal portal, boolean saveToDatabase) throws NameErrorException {
        if (isPortalNameTaken(portal.getName())) {
            throw new NameErrorException(TranslatableMessage.ALREADY_EXIST);
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
        registry.savePortal(portal, PortalType.LOCAL);
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

}
