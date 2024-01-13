package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkRegistry;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.config.ConfigurationHelper;
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
     * Get a network that is conflicting with the
     *
     * @param network
     * @param registry
     * @return
     */
    public static Network getInterServerLocalConflict(Network network, RegistryAPI registry) {
        NetworkRegistry networkRegistry;
        if (network.getStorageType() == StorageType.LOCAL) {
            networkRegistry = registry.getNetworkRegistry(StorageType.INTER_SERVER);
        } else {
            networkRegistry = registry.getNetworkRegistry(StorageType.LOCAL);
        }
        return networkRegistry.getFromName(network.getName());
    }
}
