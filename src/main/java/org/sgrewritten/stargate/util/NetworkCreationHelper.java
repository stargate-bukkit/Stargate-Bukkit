package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.api.manager.PermissionManager;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.HashSet;
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
     * Interprets a networkname and type, then selects it or creates it if it does not already exist
     *
     * @param name              <p> Initial name of the network</p>
     * @param permissionManager <p> A permission manager of the player</p>
     * @param player            <p> The player selecting the network</p>
     * @param flags             <p> flags of a portal this selection or creation comes from</p>
     * @param registry          <p> Where named network is (or will be) registered</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws TranslatableException <p>If invalid input is given</p>
     */
    public static Network selectNetwork(String name, PermissionManager permissionManager, Player player, Set<PortalFlag> flags, RegistryAPI registry) throws TranslatableException {

        Stargate.log(Level.FINER, "....Choosing network name....");
        Stargate.log(Level.FINER, "initial name is '" + name + "'");
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(name);
        String unHighlightedName = NameHelper.getTrimmedName(HighlightingStyle.getNameFromHighlightedText(name));
        TwoTuple<NetworkType, String> data;

        if (flags.contains(NetworkType.TERMINAL.getRelatedFlag())) {
            data = new TwoTuple<>(NetworkType.TERMINAL, unHighlightedName);
        } else if (unHighlightedName.trim().isEmpty()) {
            data = getNetworkDataFromEmptyDefinition(player, permissionManager);
        } else if (NetworkType.styleGivesNetworkType(highlight)) {
            data = getNetworkDataFromExplicitDefinition(highlight, unHighlightedName, registry, flags.contains(PortalFlag.FANCY_INTER_SERVER));
        } else {
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
            finalNetworkName = LocalNetwork.DEFAULT_NETWORK_ID;
        }
        Stargate.log(Level.FINE, "Ended up with: " + type + ", " + finalNetworkName);

        return selectNetwork(finalNetworkName, type, flags.contains(PortalFlag.FANCY_INTER_SERVER), registry);
    }

    /**
     * Gets the network with the given name, and creates it if it doesn't already exist
     *
     * @param name          <p>The name of the network to get</p>
     * @param type          <p>The type of network to get</p>
     * @param isInterServer <p>Whether or not the network works (or will work) across instances.
     * @param registry      <p> Where the network is (or will be) registered</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws TranslatableException <p>If the network name is invalid</p>
     */
    public static Network selectNetwork(String name, NetworkType type, boolean isInterServer, RegistryAPI registry) throws TranslatableException {
        name = NameHelper.getTrimmedName(name);
        try {
            registry.createNetwork(name, type, isInterServer, false);
        } catch (NameConflictException ignored) {
        }
        Network network = registry.getNetwork(name, isInterServer);
        if (network == null || network.getType() != type) {
            throw new NameConflictException("Could not find or create a network of type '" + type + "' with name '" + name + "'", true);
        }
        return network;
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromImplicitDefinition(String name, Player player,
                                                                                      PermissionManager permissionManager, boolean isInterserver, RegistryAPI registry) {
        if (name.equals(player.getName()) && permissionManager.canCreateInNetwork(name, NetworkType.PERSONAL)) {
            return new TwoTuple<>(NetworkType.PERSONAL, name);
        }
        if (name.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return new TwoTuple<>(NetworkType.DEFAULT, LocalNetwork.DEFAULT_NETWORK_ID);
        }
        Network possibleNetwork = registry.getNetwork(name, isInterserver);
        if (possibleNetwork != null) {
            return new TwoTuple<>(possibleNetwork.getType(), name);
        }
        UUID playerUUID = getPlayerUUID(name);
        if (registry.getNetwork(playerUUID.toString(), isInterserver) != null) {
            return new TwoTuple<>(NetworkType.PERSONAL, name);
        }
        return new TwoTuple<>(NetworkType.CUSTOM, name);
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromExplicitDefinition(HighlightingStyle highlight, String name, RegistryAPI registry, boolean isInterserver) {
        String nameToTestFor = name;
        NetworkType type = NetworkType.getNetworkTypeFromHighlight(highlight);
        if (type == NetworkType.CUSTOM || type == NetworkType.TERMINAL) {
            int i = 1;
            UUID possiblePlayerUUID = getPlayerUUID(nameToTestFor);
            while (getDefaultNamesTaken().contains(nameToTestFor.toLowerCase()) || type == NetworkType.TERMINAL &&
                    registry.getNetwork(possiblePlayerUUID.toString(), isInterserver) != null) {
                nameToTestFor = name + i;
                possiblePlayerUUID = getPlayerUUID(nameToTestFor);
                i++;
            }
        }
        return new TwoTuple<>(type, nameToTestFor);
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromEmptyDefinition(Player player,
                                                                                   PermissionManager permissionManager) {
        if (permissionManager.canCreateInNetwork("", NetworkType.DEFAULT)) {
            return new TwoTuple<>(NetworkType.DEFAULT, LocalNetwork.DEFAULT_NETWORK_ID);
        }
        return new TwoTuple<>(NetworkType.PERSONAL, player.getName());
    }

    /**
     * Gets a player's UUID from the player's name
     *
     * @param playerName <p>The name of a player</p>
     * @return <p>The player's unique ID</p>
     */
    public static UUID getPlayerUUID(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).getUniqueId();
    }

    public static Network getInterserverLocalConflict(Network network, RegistryAPI registry) {
        String[] idsToCompare = {network.getName(), getPlayerUUID(network.getName()).toString()};

        for (String idToCompare : idsToCompare) {
            if (network.getStorageType() == StorageType.LOCAL) {
                if (registry.getBungeeNetworkMap().containsKey(idToCompare)) {
                    return registry.getBungeeNetworkMap().get(idToCompare);
                }
            } else if (registry.getNetworkMap().containsKey(idToCompare)) {
                return registry.getNetworkMap().get(idToCompare);
            }
        }
        return null;
    }
}
