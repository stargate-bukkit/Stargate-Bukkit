package org.sgrewritten.stargate.network;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class StargateNetworkManager implements NetworkManager {

    private final RegistryAPI registry;

    public StargateNetworkManager(RegistryAPI registryAPI){
        this.registry = registryAPI;
    }

    @Override
    public Network selectNetwork(String name, PermissionManager permissionManager, OfflinePlayer player, Set<PortalFlag> flags) throws TranslatableException {
        return NetworkCreationHelper.selectNetwork(name, permissionManager, player, flags, registry);
    }

    @Override
    public Network selectNetwork(String name, NetworkType type, boolean isInterServer) throws TranslatableException {
        return NetworkCreationHelper.selectNetwork(name, type, isInterServer, registry);
    }
}
