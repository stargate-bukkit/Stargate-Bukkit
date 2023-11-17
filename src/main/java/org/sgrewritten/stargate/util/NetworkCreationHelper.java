package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.StorageType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
     * Gets a player's UUID from the player's name
     *
     * @param playerName <p>The name of a player</p>
     * @return <p>The player's unique ID</p>
     */
    public static UUID getPlayerUUID(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).getUniqueId();
    }

    /**
     * Get a network that is conflicting with the
     * @param network
     * @param registry
     * @return
     */
    public static Network getInterServerLocalConflict(Network network, RegistryAPI registry) {
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
