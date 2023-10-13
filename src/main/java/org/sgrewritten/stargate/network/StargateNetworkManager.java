package org.sgrewritten.stargate.network;

import org.bukkit.OfflinePlayer;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.util.NetworkCreationHelper;

import java.util.Set;

public class StargateNetworkManager implements NetworkManager {

    private final RegistryAPI registry;

    public StargateNetworkManager(RegistryAPI registryAPI) {
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
