package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.manager.PermissionManager;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A helper class for creating a new network
 */
public final class NetworkCreationHelper {

    private NetworkCreationHelper() {

    }

    public static Set<String> getBannedNames() {
        Set<String> output = new HashSet<>();
        output.add(ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK).toLowerCase());
        return output;
    }

    public static Set<String> getDefaultNamesTaken() {
        Set<String> output = new HashSet<>();
        output.add(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK).toLowerCase());
        //output.add(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_TERMINAL_NAME).toLowerCase());
        return output;
    }

    /**
     * Check the name of a network, and insert the related flags into the flags collection
     *
     * @param networkName <p> The name of the network </p>
     * @return 
     */
    public static List<PortalFlag> getNameRelatedFlags(String networkName) {
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(networkName);
        List<PortalFlag> flags = new ArrayList<>();
        
        for(NetworkType type : NetworkType.values()) {
            if(type.getHighlightingStyle() == highlight) {
                flags.add(type.getRelatedFlag());
                break;
            }
        }
        return flags;
    }

    /**
     * Interprets a networkname and type, then selects it or creates it if it does not already exist
     * 
     * @param name              <p> Initial name of the network</p>
     * @param permissionManager <p> A permission manager of the player</p>
     * @param player            <p> The player selecting the network</p>
     * @param flags             <p> flags of a portal this selection or creation comes from</p>
     * @param registry          <p> Where named network is (or will be) registered</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public static Network selectNetwork(String name, PermissionManager permissionManager, Player player, Set<PortalFlag> flags, RegistryAPI registry) throws NameErrorException{

        Stargate.log(Level.FINER, "....Choosing network name....");
        Stargate.log(Level.FINER, "initial name is '" + name + "'");
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(name);
        String unHighlightedName = NameHelper.getTrimmedName(HighlightingStyle.getNameFromHighlightedText(name));
        TwoTuple<NetworkType,String> data;
        
        if(flags.contains(NetworkType.TERMINAL.getRelatedFlag())) {
            data = new TwoTuple<>(NetworkType.TERMINAL,unHighlightedName);
        }
        else if(unHighlightedName.trim().isEmpty()) {
            data  = getNetworkDataFromEmptyDefinition(player,permissionManager);
        }
        else if(NetworkType.styleGivesNetworkType(highlight)) {
            data = getNetworkDataFromExplicitDefinition(highlight,unHighlightedName,registry,flags.contains(PortalFlag.FANCY_INTER_SERVER));  
        }
        else {
            data = getNetworkDataFromImplicitDefinition(unHighlightedName, player, permissionManager,
                    flags.contains(PortalFlag.FANCY_INTER_SERVER), registry);
        }
        NetworkType type = data.getFirstValue();
        String finalNetworkName = data.getSecondValue();
        if (type == NetworkType.PERSONAL) {
            finalNetworkName = getPlayerUUID(finalNetworkName).toString();
        }
        if (type == NetworkType.DEFAULT
                && finalNetworkName.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            finalNetworkName = LocalNetwork.DEFAULT_NET_ID;
        }
        Stargate.log(Level.FINE,"Ended up with: " + type + ", " + finalNetworkName);
        
        return selectNetwork(finalNetworkName, type, flags.contains(PortalFlag.FANCY_INTER_SERVER),registry);
    }

    /**
     * Gets the network with the given name, and creates it if it doesn't already exist
     *
     * @param name  <p>The name of the network to get</p>
     * @param type <p>The type of network to get</p>
     * @param isInterserver <p>Whether or not the network works (or will work) across instances.
     * @param registry <p> Where the network is (or will be) registered</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public static Network selectNetwork(String name, NetworkType type, boolean isInterserver, RegistryAPI registry) throws NameErrorException {
        name = NameHelper.getTrimmedName(name);
        try {
            registry.createNetwork(name, type, isInterserver, false);
        } catch (NameErrorException nameErrorException) {
            TranslatableMessage translatableMessage = nameErrorException.getErrorMessage();
            if (translatableMessage != null) {
                throw nameErrorException;
            }
        }
        return registry.getNetwork(name, isInterserver);
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromImplicitDefinition(String name, Player player,
            PermissionManager permissionManager, boolean isInterserver, RegistryAPI registry) {
        if(name.equals(player.getName()) && permissionManager.canCreateInNetwork(name, NetworkType.PERSONAL)) {
            return new TwoTuple<>(NetworkType.PERSONAL,name);
        }
        if(name.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return new TwoTuple<>(NetworkType.DEFAULT,LocalNetwork.DEFAULT_NET_ID);
        }
        Network possibleNetwork = registry.getNetwork(name, isInterserver);
        if(possibleNetwork != null) {
            return new TwoTuple<>(possibleNetwork.getType(),name);
        }
        UUID playerUUID = getPlayerUUID(name);
        if(playerUUID != null && registry.getNetwork(playerUUID.toString(), isInterserver) != null) {
            return new TwoTuple<>(NetworkType.PERSONAL,name);
        }
        return new TwoTuple<>(NetworkType.CUSTOM,name);
    }
    
    private static TwoTuple<NetworkType,String> getNetworkDataFromExplicitDefinition(HighlightingStyle highlight,String name, RegistryAPI registry, boolean isInterserver) throws NameErrorException{
        String nameToTestFor = name;
        NetworkType type = NetworkType.getNetworkTypeFromHighlight(highlight);
        if (type == NetworkType.CUSTOM || type == NetworkType.TERMINAL) {
            UUID possiblePlayerUUID = getPlayerUUID(nameToTestFor);
            int i = 1;
            while (getDefaultNamesTaken().contains(nameToTestFor.toLowerCase())
                    || (type == NetworkType.TERMINAL && possiblePlayerUUID != null
                            && registry.getNetwork(possiblePlayerUUID.toString(), isInterserver) != null)) {
                nameToTestFor = name + i;
                i++;
            }
        }
        return new TwoTuple<>(type, nameToTestFor);
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromEmptyDefinition(Player player,
            PermissionManager permissionManager) {
        if(permissionManager.canCreateInNetwork("", NetworkType.DEFAULT)) {
            return new TwoTuple<>(NetworkType.DEFAULT, LocalNetwork.DEFAULT_NET_ID);
        }
        return new TwoTuple<>(NetworkType.PERSONAL, player.getName());
    }

    /**
     * Gets a player's UUID from the player's name
     *
     * @param playerName <p>The name of a player</p>
     * @return <p>The player's unique ID</p>
     */
    private static UUID getPlayerUUID(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).getUniqueId();
    }

}
