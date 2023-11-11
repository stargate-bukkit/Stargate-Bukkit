package org.sgrewritten.stargate.network;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
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
    public Network selectNetwork(String name, PermissionManager permissionManager, OfflinePlayer player, Set<PortalFlag> flags) throws TranslatableException {

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
            finalNetworkName = NetworkCreationHelper.getPlayerUUID(finalNetworkName).toString();
        }
        if (type == NetworkType.DEFAULT
                && finalNetworkName.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            finalNetworkName = StargateNetwork.DEFAULT_NETWORK_ID;
        }
        Stargate.log(Level.FINE, "Ended up with: " + type + ", " + finalNetworkName);

        return selectNetwork(finalNetworkName, type, flags.contains(PortalFlag.FANCY_INTER_SERVER));
    }

    @Override
    public Network selectNetwork(String name, NetworkType type, boolean isInterServer) throws TranslatableException {
        name = NameHelper.getTrimmedName(name);
        try {
            this.createNetwork(name, type, isInterServer, false);
        } catch (NameConflictException ignored) {
        }
        Network network = registry.getNetwork(name, isInterServer);
        if (network == null || network.getType() != type) {
            throw new NameConflictException("Could not find or create a network of type '" + type + "' with name '" + name + "'", true);
        }
        return network;
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromExplicitDefinition(HighlightingStyle highlight, String name, RegistryAPI registry, boolean isInterserver) {
        String nameToTestFor = name;
        NetworkType type = NetworkType.getNetworkTypeFromHighlight(highlight);
        if (type == NetworkType.CUSTOM || type == NetworkType.TERMINAL) {
            int i = 1;
            UUID possiblePlayerUUID = NetworkCreationHelper.getPlayerUUID(nameToTestFor);
            while (NetworkCreationHelper.getDefaultNamesTaken().contains(nameToTestFor.toLowerCase()) || type == NetworkType.TERMINAL &&
                    registry.getNetwork(possiblePlayerUUID.toString(), isInterserver) != null) {
                nameToTestFor = name + i;
                possiblePlayerUUID = NetworkCreationHelper.getPlayerUUID(nameToTestFor);
                i++;
            }
        }
        return new TwoTuple<>(type, nameToTestFor);
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromEmptyDefinition(OfflinePlayer player,
                                                                                   PermissionManager permissionManager) {
        if (permissionManager.canCreateInNetwork("", NetworkType.DEFAULT)) {
            return new TwoTuple<>(NetworkType.DEFAULT, StargateNetwork.DEFAULT_NETWORK_ID);
        }
        return new TwoTuple<>(NetworkType.PERSONAL, player.getName());
    }

    private static TwoTuple<NetworkType, String> getNetworkDataFromImplicitDefinition(String name, OfflinePlayer player,
                                                                                      PermissionManager permissionManager, boolean isInterserver, RegistryAPI registry) {
        if (name.equals(player.getName()) && permissionManager.canCreateInNetwork(name, NetworkType.PERSONAL)) {
            return new TwoTuple<>(NetworkType.PERSONAL, name);
        }
        if (name.equals(ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK))) {
            return new TwoTuple<>(NetworkType.DEFAULT, StargateNetwork.DEFAULT_NETWORK_ID);
        }
        Network possibleNetwork = registry.getNetwork(name, isInterserver);
        if (possibleNetwork != null) {
            return new TwoTuple<>(possibleNetwork.getType(), name);
        }
        UUID playerUUID = NetworkCreationHelper.getPlayerUUID(name);
        if (registry.getNetwork(playerUUID.toString(), isInterserver) != null) {
            return new TwoTuple<>(NetworkType.PERSONAL, name);
        }
        return new TwoTuple<>(NetworkType.CUSTOM, name);
    }

    @Override
    public Network createNetwork(String name, NetworkType type, boolean isInterServer, boolean isForced) throws InvalidNameException, UnimplementedFlagException, NameLengthException, NameConflictException {
        if (registry.networkExists(name, isInterServer)
                || registry.networkExists(NetworkCreationHelper.getPlayerUUID(name).toString(), isInterServer)) {
            if (isForced && type == NetworkType.DEFAULT) {
                Network network = registry.getNetwork(name, isInterServer);
                if (network != null && network.getType() != type) {
                    String newId = registry.getValidNewName(network);
                    registry.updateName(network, newId);
                    Bukkit.getScheduler().runTaskAsynchronously(Stargate.getInstance(), () -> {
                        try {
                            storageAPI.updateNetworkName(newId, network.getName(), network.getStorageType());
                        } catch (StorageWriteException e) {
                            Stargate.log(e);
                        }
                    });
                }
            }
            throw new NameConflictException("network of id '" + name + "' already exists", true);
        }
        Network network = storageAPI.createNetwork(name, type, isInterServer);
        registry.registerNetwork(network);
        Stargate.log(
                Level.FINEST, String.format("Adding networkid %s to interServer = %b", network.getId(), isInterServer));
        return network;
    }


    @Override
    public Network createNetwork(String targetNetwork, Set<PortalFlag> flags, boolean isForced) throws InvalidNameException, NameLengthException, NameConflictException, UnimplementedFlagException {
        return this.createNetwork(targetNetwork, NetworkType.getNetworkTypeFromFlags(flags), flags.contains(PortalFlag.FANCY_INTER_SERVER), isForced);
    }


    @Override
    public void rename(Network network, String newName) throws InvalidNameException, NameLengthException, UnimplementedFlagException {
        String oldName = network.getName();
        registry.updateName(network, newName);
        network.getPluginMessageSender().sendRenameNetwork(newName, oldName);
        try {
            storageAPI.updateNetworkName(newName,oldName,network.getStorageType());
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public void loadPortals(StargateAPI stargateAPI) {
        try {
            storageAPI.loadFromStorage(registry, stargateAPI);
        } catch (StorageReadException e) {
            Stargate.log(e);
            return;
        }
        Stargate.addSynchronousTickAction(new SupplierAction(() -> {
            registry.updateAllPortals();
            return true;
        }));
    }

    @Override
    public void rename(Portal portal, String newName) throws NameConflictException {
        Network network = portal.getNetwork();
        if(network.isPortalNameTaken(newName)){
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

}
