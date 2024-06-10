package org.sgrewritten.stargate.network;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.event.portal.StargateListPortalEvent;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.proxy.PluginMessageSender;
import org.sgrewritten.stargate.api.permission.BypassPermission;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.network.proxy.InterServerMessageSender;
import org.sgrewritten.stargate.network.proxy.LocalNetworkMessageSender;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A network of portals
 */
public class StargateNetwork implements Network {
    private final PluginMessageSender messageSender;
    private final StorageType storageType;

    private Map<String, Portal> nameToPortalMap;
    private String name;
    private String id;
    private RegistryAPI registry;
    private NetworkType networkType;

    /**
     * Instantiates a new network
     *
     * @param name  <p>The name of the new network</p>
     * @param flags <p>The flags attached to this network</p>
     * @throws InvalidNameException       <p>If the network name is invalid</p>
     * @throws NameLengthException
     * @throws UnimplementedFlagException
     */
    public StargateNetwork(String name, Set<PortalFlag> flags, StorageType storageType) throws InvalidNameException,
            NameLengthException, UnimplementedFlagException {
        this(name, NetworkType.getNetworkTypeFromFlags(flags), storageType);
    }

    public StargateNetwork(String name, NetworkType type, StorageType storageType) throws InvalidNameException,
            NameLengthException, UnimplementedFlagException {
        load(name, type);
        this.storageType = storageType;
        this.messageSender = storageType == StorageType.INTER_SERVER ? new InterServerMessageSender() :
                new LocalNetworkMessageSender();
    }

    private void load(String name, NetworkType type) throws InvalidNameException, NameLengthException,
            UnimplementedFlagException {
        Objects.requireNonNull(name);
        this.networkType = Objects.requireNonNull(type);
        setID(name, type);
        nameToPortalMap = new HashMap<>();
    }

    private void setID(String name, NetworkType type) throws InvalidNameException, NameLengthException,
            UnimplementedFlagException {
        switch (type) {
            case DEFAULT -> loadAsDefault(name);
            case PERSONAL -> loadAsPersonalNetwork(name);
            case CUSTOM -> loadAsCustomNetwork(name);
            case TERMINAL ->
                    throw new UnimplementedFlagException("Terminal networks are not implemented yet", type.getRelatedFlag());
        }
    }

    private void loadAsDefault(String name) throws InvalidNameException {
        this.name = ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK);
        if (!StargateConstant.DEFAULT_NETWORK_ID.equals(name)) {
            throw new InvalidNameException("Invalid name '" + name + "' can not be default network, expected name '" +
                    StargateConstant.DEFAULT_NETWORK_ID + "'");
        }
        id = StargateConstant.DEFAULT_NETWORK_ID;
    }

    private void loadAsCustomNetwork(String networkName) throws NameLengthException {
        networkName = NameHelper.getTrimmedName(networkName);
        if (NameHelper.isInvalidName(networkName)) {
            throw new NameLengthException("Name '" + networkName + "' is to short or to long, expected length over 0 " +
                    "and under " + StargateConstant.MAX_TEXT_LENGTH);
        }
        this.name = networkName.trim();
        if (ConfigurationHelper.getBoolean(ConfigurationOption.DISABLE_CUSTOM_COLORED_NAMES)) {
            this.name = ChatColor.stripColor(this.name);
        }
        id = NameHelper.getNormalizedName(this.name);
    }

    private void loadAsPersonalNetwork(String uuidString) throws InvalidNameException {
        Stargate.log(Level.FINER, "Initialized personal network with UUID " + uuidString);
        String possiblePlayerName = Bukkit.getOfflinePlayer(UUID.fromString(uuidString)).getName();
        if (possiblePlayerName == null) {
            throw new InvalidNameException("The personal network of the uuid '" + uuidString + "' has no valid player name.");
        }
        Stargate.log(Level.FINER, "Matching player name: " + possiblePlayerName);
        if (NetworkCreationHelper.getDefaultNamesTaken().contains(possiblePlayerName.toLowerCase()) ||
                NetworkCreationHelper.getBannedNames().contains(possiblePlayerName.toLowerCase())) {
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
    public void removePortal(Portal portal) {
        nameToPortalMap.remove(portal.getId());
    }

    @Override
    public void addPortal(Portal portal) throws NameConflictException {
        if (isPortalNameTaken(portal.getName())) {
            throw new NameConflictException("portal of name '" + portal.getName() + "' already exist in network '" +
                    this.getId() + "'", false);
        }
        if (portal instanceof RealPortal realPortal) {
            registry.registerPortal(realPortal);
        }
        nameToPortalMap.put(portal.getId(), portal);
    }

    @Override
    public boolean isPortalNameTaken(String name) {
        return nameToPortalMap.containsKey(NameHelper.getNormalizedName(name));
    }

    @Override
    public void updatePortals() {
        for (Portal portal : nameToPortalMap.values()) {
            portal.updateState();
        }
    }

    @Override
    public Set<String> getAvailablePortals(Player player, Portal requester) {
        Set<String> output = new HashSet<>(nameToPortalMap.keySet());
        output.remove(requester.getId());
        Set<String> removeList = new HashSet<>();
        for (String portalName : output) {
            Portal target = getPortal(portalName);
            if (!canSeePortal(target, requester, player)) {
                removeList.add(portalName);
            }
        }
        output.removeAll(removeList);
        return output;
    }

    private boolean playerCanSeePrivatePortal(Portal portalToSee, Player player) {
        return player != null && (player.hasPermission(BypassPermission.PRIVATE.getPermissionString())
                || player.getUniqueId().equals(portalToSee.getOwnerUUID()));
    }

    @Override
    public boolean canSeePortal(Portal portalToSee, Portal origin, Player player){
        boolean deny = (portalToSee.hasFlag(StargateFlag.PRIVATE) && !playerCanSeePrivatePortal(portalToSee, player));
        StargateListPortalEvent event = new StargateListPortalEvent(origin, player, portalToSee, deny);
        Bukkit.getPluginManager().callEvent(event);
        return !event.getDeny();
    }

    @Override
    public HighlightingStyle getHighlightingStyle() {
        return networkType.getHighlightingStyle();
    }

    @Override
    public void destroy() {
        for (Portal portal : nameToPortalMap.values()) {
            portal.destroy();
        }
        nameToPortalMap.clear();
    }

    @Override
    public String getName() {
        if (getType() == NetworkType.PERSONAL && registry != null &&
                (registry.networkExists(NameHelper.getNormalizedName(name), this.getStorageType()))) {
            return id.split("-")[0];
        }
        return name;
    }

    @Override
    public int size() {
        return this.getAllPortals().size();
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
        return this.storageType;
    }

    @Override
    public void setID(String newName) throws InvalidNameException, NameLengthException, UnimplementedFlagException {
        setID(newName, this.getType());
    }

    @Override
    public PluginMessageSender getPluginMessageSender() {
        return this.messageSender;
    }

    @Override
    public void renamePortal(String newName, String oldName) throws InvalidNameException {
        Portal portal = nameToPortalMap.remove(oldName);
        if (portal == null) {
            throw new InvalidNameException("Name does not exist, can not rename: " + oldName);
        }
        portal.setName(NameHelper.getNormalizedName(newName));
        nameToPortalMap.put(portal.getName(), portal);
    }


}
