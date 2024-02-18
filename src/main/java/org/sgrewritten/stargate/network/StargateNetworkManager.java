package org.sgrewritten.stargate.network;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.NetworkRegistry;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.thread.task.StargateGlobalTask;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;
import org.sgrewritten.stargate.util.NameHelper;
import org.sgrewritten.stargate.util.NetworkCreationHelper;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class StargateNetworkManager implements NetworkManager {

    private final RegistryAPI registry;
    private final StorageAPI storageAPI;

    public StargateNetworkManager(RegistryAPI registryAPI, StorageAPI storageAPI) {
        this.registry = registryAPI;
        this.storageAPI = storageAPI;
    }

    @Override
    public @NotNull Network selectNetwork(String name, PermissionManager permissionManager, OfflinePlayer player, Set<PortalFlag> flags) throws TranslatableException {

        if (flags.contains(PortalFlag.BUNGEE)) {
            return selectNetwork(ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK), NetworkType.CUSTOM, StorageType.LOCAL);
        }

        Stargate.log(Level.FINER, "....Choosing network name....");
        Stargate.log(Level.FINER, "initial name is '" + name + "'");
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(name);
        String unHighlightedName = NameHelper.getNormalizedName(HighlightingStyle.getNameFromHighlightedText(name));
        TwoTuple<NetworkType, String> data;
        StorageType storageType = flags.contains(PortalFlag.FANCY_INTER_SERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL;

        if (flags.contains(NetworkType.TERMINAL.getRelatedFlag())) {
            data = new TwoTuple<>(NetworkType.TERMINAL, unHighlightedName);
        } else if (unHighlightedName.trim().isEmpty()) {
            data = getNetworkDataFromEmptyDefinition(player, permissionManager);
        } else if (NetworkType.styleGivesNetworkType(highlight)) {
            data = getNetworkDataFromExplicitDefinition(highlight, unHighlightedName, registry, storageType);
        } else {
            data = getNetworkDataFromImplicitDefinition(unHighlightedName, player, permissionManager,
                    storageType, registry);
        }
        NetworkType type = data.getFirstValue();
        String finalNetworkName = data.getSecondValue();
        if (type == NetworkType.PERSONAL) {
            if (finalNetworkName.equals(player.getName())) {
                finalNetworkName = player.getUniqueId().toString();
            } else {
                UUID playerUUID = this.getPlayerUUID(finalNetworkName);
                if (playerUUID == null) {
                    throw new InvalidNameException("No such player: " + finalNetworkName);
                }
                finalNetworkName = playerUUID.toString();
            }
        }
        if (type == NetworkType.DEFAULT
                && finalNetworkName.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            finalNetworkName = StargateConstant.DEFAULT_NETWORK_ID;
        }
        Stargate.log(Level.FINE, "Ended up with: " + type + ", " + finalNetworkName);

        return selectNetwork(finalNetworkName, type, storageType);
    }

    @Override
    public @NotNull Network selectNetwork(String name, NetworkType type, StorageType storageType) throws TranslatableException {
        if (type == NetworkType.TERMINAL) {
            throw new UnimplementedFlagException("Terminal networks aare not implemented", PortalFlag.TERMINAL_NETWORK);
        }
        name = NameHelper.getTrimmedName(name);
        try {
            this.createNetwork(name, type, storageType, false);
        } catch (NameConflictException ignored) {
        }
        Network network = registry.getNetwork(name, storageType);
        if (network == null || network.getType() != type) {
            throw new NameConflictException("Could not find or create a network of type '" + type + "' with name '" + name + "'", true);
        }
        return network;
    }

    private TwoTuple<NetworkType, String> getNetworkDataFromExplicitDefinition(HighlightingStyle highlight, String name, RegistryAPI registry, StorageType storageType) {
        String nameToTestFor = name;
        NetworkType type = NetworkType.getNetworkTypeFromHighlight(highlight);
        if (type == NetworkType.CUSTOM || type == NetworkType.TERMINAL) {
            int i = 1;
            NetworkRegistry networkRegistry = registry.getNetworkRegistry(storageType);
            Network conflictingNetwork = networkRegistry.getFromName(nameToTestFor);
            while (NetworkCreationHelper.getDefaultNamesTaken().contains(nameToTestFor.toLowerCase()) || type == NetworkType.TERMINAL &&
                    conflictingNetwork != null) {
                nameToTestFor = name + i;
                conflictingNetwork = networkRegistry.getFromName(nameToTestFor);
                i++;
            }
        }
        return new TwoTuple<>(type, nameToTestFor);
    }

    private TwoTuple<NetworkType, String> getNetworkDataFromEmptyDefinition(OfflinePlayer player,
                                                                            PermissionManager permissionManager) {
        if (permissionManager.canCreateInNetwork("", NetworkType.DEFAULT)) {
            return new TwoTuple<>(NetworkType.DEFAULT, StargateConstant.DEFAULT_NETWORK_ID);
        }
        return new TwoTuple<>(NetworkType.PERSONAL, player.getName());
    }

    private TwoTuple<NetworkType, String> getNetworkDataFromImplicitDefinition(String name, OfflinePlayer player,
                                                                               PermissionManager permissionManager, StorageType storageType, RegistryAPI registry) {
        if (name.equals(player.getName()) && permissionManager.canCreateInNetwork(name, NetworkType.PERSONAL)) {
            return new TwoTuple<>(NetworkType.PERSONAL, name);
        }
        if (name.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return new TwoTuple<>(NetworkType.DEFAULT, StargateConstant.DEFAULT_NETWORK_ID);
        }
        Network possibleNetwork = registry.getNetwork(name, storageType);
        if (possibleNetwork != null) {
            return new TwoTuple<>(possibleNetwork.getType(), name);
        }
        if (registry.getNetworkRegistry(storageType).getFromName(name) != null) {
            return new TwoTuple<>(NetworkType.PERSONAL, name);
        }
        return new TwoTuple<>(NetworkType.CUSTOM, name);
    }

    @Override
    public Network createNetwork(String name, NetworkType type, StorageType storageType, boolean isForced) throws InvalidNameException, UnimplementedFlagException, NameLengthException, NameConflictException {
        NetworkRegistry networkRegistry = registry.getNetworkRegistry(storageType);
        if (networkRegistry.networkExists(name) || networkRegistry.networkNameExists(name)) {
            if (isForced && type == NetworkType.DEFAULT) {
                Network network = registry.getNetwork(name, storageType);
                if (network != null && network.getType() != type) {
                    String newId = registry.getValidNewName(network);
                    registry.renameNetwork(newId, network.getId(), network.getStorageType());
                    new StargateQueuedAsyncTask(() -> {
                        try {
                            storageAPI.updateNetworkName(newId, network.getName(), network.getStorageType());
                        } catch (StorageWriteException e) {
                            Stargate.log(e);
                        }
                    }).run();
                }
            }
            throw new NameConflictException("network of id '" + name + "' already exists", true);
        }
        Network network = storageAPI.createNetwork(name, type, storageType);
        registry.registerNetwork(network);
        Stargate.log(
                Level.FINEST, String.format("Adding network id %s to interServer = %s", network.getId(), storageType));
        return network;
    }


    @Override
    public Network createNetwork(String targetNetwork, Set<PortalFlag> flags, boolean isForced) throws InvalidNameException, NameLengthException, NameConflictException, UnimplementedFlagException {
        return this.createNetwork(targetNetwork, NetworkType.getNetworkTypeFromFlags(flags), flags.contains(PortalFlag.FANCY_INTER_SERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL, isForced);
    }


    @Override
    public void rename(Network network, String newName) throws InvalidNameException, NameLengthException, UnimplementedFlagException {
        String oldName = network.getName();
        registry.renameNetwork(newName, network.getId(), network.getStorageType());
        network.getPluginMessageSender().sendRenameNetwork(newName, oldName);
        try {
            storageAPI.updateNetworkName(newName, oldName, network.getStorageType());
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public void loadPortals(StargateAPI stargateAPI) {
        try {
            storageAPI.loadFromStorage(stargateAPI);
        } catch (StorageReadException e) {
            Stargate.log(e);
            return;
        }
        /*
         * Note one why we need to have this delay: it's because the loaded portals will be registered one tick after
         * startup, or even later than that if we're using the StargatePopulator, as there's lag on startup.
         * This is because of Folia compatibility, there's portal validation tasks that needs to be done using the
         * region scheduler. Temporary solution right now is to just have a large delay here.
         * TODO: come up with a general solution
         */
        new StargateGlobalTask(registry::updateAllPortals).runDelayed(20);
    }

    @Override
    public void rename(Portal portal, String newName) throws NameConflictException {
        Network network = portal.getNetwork();
        if (network.isPortalNameTaken(newName)) {
            throw new NameConflictException(String.format("Portal name %s is already used by another portal", newName), false);
        }
        try {
            storageAPI.updatePortalName(newName, portal.getGlobalId(), portal.getStorageType());
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
        network.getPluginMessageSender().sendRenamePortal(newName, portal.getName(), portal.getNetwork());
        // The portal is stored in a map with its name as a key; this key needs to be updated properly.
        network.removePortal(portal);
        portal.setName(newName);
        network.addPortal(portal);
        portal.getNetwork().updatePortals();
    }

    @Override
    public void savePortal(RealPortal portal, Network network) throws NameConflictException {
        network.addPortal(portal);
        new StargateQueuedAsyncTask(() -> {
            try {
                storageAPI.savePortalToStorage(portal);
            } catch (StorageWriteException e) {
                Stargate.log(e);
            }
        }).run();
        network.getPluginMessageSender().sendCreatePortal(portal);
        network.updatePortals();
    }

    @Override
    public void destroyPortal(RealPortal portal) {
        Network network = portal.getNetwork();
        network.removePortal(portal);
        portal.destroy();
        registry.unregisterPortal(portal);
        network.updatePortals();
        new StargateQueuedAsyncTask(() -> {
            try {
                storageAPI.removePortalFromStorage(portal);
            } catch (StorageWriteException e) {
                Stargate.log(e);
            }
        }).run();
        portal.getNetwork().getPluginMessageSender().sendDeletePortal(portal);
    }


    /**
     * Gets a player's UUID from the player's name
     *
     * @param playerName <p>The name of a player</p>
     * @return <p>The player's unique ID</p>
     */
    private @Nullable UUID getPlayerUUID(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(playerName);
        return offlinePlayer == null ? null : offlinePlayer.getUniqueId();
    }

}
