package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.exception.PermissionException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.manager.PermissionManager;
import org.sgrewritten.stargate.manager.StargatePermissionManager;
import org.sgrewritten.stargate.network.CreationAuthority;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.ArrayList;
import java.util.EnumSet;
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
        output.add(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_TERMINAL_NAME).toLowerCase());
        return output;
    }

    /**
     * Check the name of a network, and insert the related flags into the flags collection
     *
     * @param networkName <p> The name of the network </p>
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
     * Remove notations from the network name and make it ready for use
     */
    public static String parseNetworkNameName(String initialName) throws NameErrorException {
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(initialName);
        String unHighlightedName = HighlightingStyle.getNameFromHighlightedText(initialName);
        if (highlight == HighlightingStyle.CURLY_BRACKETS) {
            try {
                return getPlayerUUID(unHighlightedName).toString();
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new NameErrorException(TranslatableMessage.INVALID_NAME);
            }
        }
        return unHighlightedName;
    }

    /**
     * Interprets a networkname and type, then selects it or creates it if it does not already exist
     * 
     * @param name              <p> Initial name of the network</p>
     * @param permissionManager <p> A permission manager of the player</p>
     * @param player            <p> The player selecting the network</p>
     * @param flags             <p> flags of a portal this selection or creation comes from</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public static Network selectNetwork(String name, PermissionManager permissionManager, Player player, Set<PortalFlag> flags) throws NameErrorException{

        Stargate.log(Level.FINER, "....Choosing network name....");
        Stargate.log(Level.FINER, "initial name is " + name);
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(name);
        String unHighlightedName = HighlightingStyle.getNameFromHighlightedText(name);
        
        CreationAuthority authority;
        TwoTuple<NetworkType,String> data;
        
        if(flags.contains(PortalFlag.TERMINAL_NETWORK)) {
            authority = CreationAuthority.EXCPLICIT;
            data = new TwoTuple<>(NetworkType.TERMINAL,unHighlightedName);
        }
        else if(unHighlightedName.trim().isEmpty()) {
            authority = CreationAuthority.IMPLICIT;
            data  = getNetworkDataFromEmptyDefinition(player,permissionManager);
        }
        else if(NetworkType.styleGivesNetworkType(highlight)) {
            authority = CreationAuthority.EXCPLICIT;
            data = new TwoTuple<>(NetworkType.getNetworkTypeFromHighlight(highlight),unHighlightedName);
        }
        else {
            authority = CreationAuthority.IMPLICIT;
            data = getNetworkDataFromImplicitDefinition(unHighlightedName,player,permissionManager,flags.contains(PortalFlag.FANCY_INTER_SERVER));
        }
        String finalNetworkName = data.getSecondValue();
        NetworkType type = data.getFirstValue();
        return selectNetwork(finalNetworkName, type, flags.contains(PortalFlag.FANCY_INTER_SERVER));
    }

    /**
     * Gets the network with the given name, and creates it if it doesn't already exist
     *
     * @param name  <p>The name of the network to get</p>
     * @param type <p>The type of network to get</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public static Network selectNetwork(String name, NetworkType type, boolean isInterserver) throws NameErrorException {
        try {
            Stargate.getRegistryStatic().createNetwork(name, type, isInterserver);
        } catch (NameErrorException nameErrorException) {
            TranslatableMessage translatableMessage = nameErrorException.getErrorMessage();
            if (translatableMessage != null) {
                throw nameErrorException;
            }
        }
        return Stargate.getRegistryStatic().getNetwork(name, isInterserver);
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromImplicitDefinition(String name, Player player,
            PermissionManager permissionManager, boolean isInterserver) {
        if(name.equals(player.getName()) && permissionManager.canCreateInNetwork(name, NetworkType.PERSONAL)) {
            return new TwoTuple<>(NetworkType.PERSONAL,name);
        }
        if(name.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return new TwoTuple<>(NetworkType.DEFAULT,name);
        }
        if(Stargate.getRegistryStatic().getNetwork(name, isInterserver) != null) {
            return new TwoTuple<>(NetworkType.PERSONAL,name);
        }
        return new TwoTuple<>(NetworkType.CUSTOM,name);
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromEmptyDefinition(Player player,
            PermissionManager permissionManager) {
        if(permissionManager.canCreateInNetwork("", NetworkType.DEFAULT)) {
            return new TwoTuple<>(NetworkType.DEFAULT, ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK));
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
