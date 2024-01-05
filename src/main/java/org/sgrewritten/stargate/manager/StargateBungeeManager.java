package org.sgrewritten.stargate.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.manager.BungeeManager;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.VirtualPortal;
import org.sgrewritten.stargate.property.StargateProtocolProperty;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;

import java.util.*;
import java.util.logging.Level;

public class StargateBungeeManager implements BungeeManager {

    private final RegistryAPI registry;
    private final @NotNull LanguageManager languageManager;
    private final HashMap<String, Portal> bungeeQueue = new HashMap<>();
    private final NetworkManager networkManager;

    public StargateBungeeManager(@NotNull RegistryAPI registry, @NotNull LanguageManager languageManager, @NotNull NetworkManager networkManager) {
        this.registry = Objects.requireNonNull(registry);
        this.languageManager = Objects.requireNonNull(languageManager);
        this.networkManager = Objects.requireNonNull(networkManager);
    }

    @Override
    public void updateNetwork(String message) {
        Stargate.log(Level.FINEST, message);
        // Yes, a depricated method, needs to be there as spigot 1.16.5 does not support the new method
        JsonObject json = (JsonObject) new JsonParser().parse(message);

        String requestTypeString = json.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString();
        StargateProtocolRequestType requestType = StargateProtocolRequestType.valueOf(requestTypeString);
        switch (requestType){
            case PORTAL_ADD, PORTAL_REMOVE-> {
                portalAddOrRemove(json, requestType);
            }
            case NETWORK_RENAME -> {
                String oldId = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
                String newId = json.get(StargateProtocolProperty.NEW_NETWORK_NAME.toString()).getAsString();
                try {
                    registry.renameNetwork(newId, oldId, StorageType.INTER_SERVER);
                } catch (InvalidNameException | UnimplementedFlagException | NameLengthException e) {
                    Stargate.log(e);
                }
            }
            case PORTAL_RENAME -> {
                String oldName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
                String newName = json.get(StargateProtocolProperty.NEW_PORTAL_NAME.toString()).getAsString();
                String networkId = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
                Network network = registry.getNetwork(networkId,StorageType.INTER_SERVER);
                if(network == null){
                    Stargate.log(Level.WARNING, "Could not rename cross server portal, as network did not exist");
                    return;
                }
                try {
                    network.renamePortal(newName,oldName);
                } catch (InvalidNameException e) {
                    Stargate.log(e);
                }
            }
        }
    }

    private void portalAddOrRemove(JsonObject json, StargateProtocolRequestType requestType){

        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String network = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
        String server = json.get(StargateProtocolProperty.SERVER.toString()).getAsString();
        String flagString = json.get(StargateProtocolProperty.PORTAL_FLAG.toString()).getAsString();
        Set<PortalFlag> flags = PortalFlag.parseFlags(flagString);
        Set<Character> unrecognisedFlags = PortalFlag.getUnrecognisedFlags(flagString);
        UUID ownerUUID = UUID.fromString(json.get(StargateProtocolProperty.OWNER.toString()).getAsString());

        try {
            networkManager.createNetwork(network, flags, false);
        } catch (NameConflictException ignored) {}
        catch (InvalidNameException | NameLengthException | UnimplementedFlagException e) {
            Stargate.log(e);
        }
        try {
            Network targetNetwork = registry.getNetwork(network, StorageType.INTER_SERVER);
            if (targetNetwork == null) {
                Stargate.log(Level.WARNING, "Unable to get inter-server network " + network);
                return;
            }
            VirtualPortal portal = new VirtualPortal(server, portalName, targetNetwork, flags, unrecognisedFlags, ownerUUID);
            switch (requestType) {
                case PORTAL_ADD -> {
                    targetNetwork.addPortal(portal);
                    Stargate.log(Level.FINE, String.format("Adding virtual portal %s in inter-server network %s", portalName, network));
                }
                case PORTAL_REMOVE -> {
                    Stargate.log(Level.FINE, String.format("Removing virtual portal %s in inter-server network %s", portalName, network));
                    targetNetwork.removePortal(portal);
                }
            }
            targetNetwork.updatePortals();
        } catch (NameConflictException exception) {
            Stargate.log(exception);
        }
    }

    @Override
    public void playerConnect(String message) {
        Stargate.log(Level.FINEST, message);

        // Yes, a depricated method, needs to be there as spigot 1.16.5 does not support the new method
        JsonObject json = (JsonObject) new JsonParser().parse(message);
        String playerName = json.get(StargateProtocolProperty.PLAYER.toString()).getAsString();
        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String networkName = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();

        Player player = Bukkit.getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");
            addToQueue(playerName, portalName, networkName, StorageType.INTER_SERVER);
            return;
        }

        Stargate.log(Level.FINEST, "Player was not null; trying to teleport");
        Network network = registry.getNetwork(networkName, StorageType.INTER_SERVER);
        if (network == null) {
            player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.BUNGEE_INVALID_NETWORK));
            return;
        }
        Portal destinationPortal = network.getPortal(portalName);
        if (destinationPortal == null) {
            player.sendMessage(languageManager.getErrorMessage(TranslatableMessage.BUNGEE_INVALID_GATE));
            return;
        }
        destinationPortal.teleportHere(player, null);
    }

    @Override
    public void legacyPlayerConnect(String message) {
        String bungeeNetworkName = BungeePortal.getLegacyNetworkName();

        String[] parts = message.split("#@#");

        String playerName = parts[0];
        String destination = parts[1];

        Stargate.log(Level.FINER, "destination=" + destination + ",player=" + playerName);

        // Check if the player is online, if so, teleport, otherwise, queue
        Player player = Bukkit.getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");

            addToQueue(playerName, destination, bungeeNetworkName, StorageType.LOCAL);
        } else {
            Network network;
            try {
                network = BungeeHelper.getLegacyBungeeNetwork(registry, networkManager, bungeeNetworkName);
            } catch (UnimplementedFlagException e) {
                Stargate.log(e);
                return;
            }
            if (network == null) {
                Stargate.log(Level.WARNING, "The legacy bungee network is missing, this is most definitly a bug please contact developers (/sg about)");
                return;
            }
            //If the destination is invalid, just let the player teleport to their last location
            Portal destinationPortal = network.getPortal(destination);
            if (destinationPortal == null) {
                Stargate.log(Level.FINE, String.format("Could not find destination portal with name '%s'", destination));
                return;
            }

            Stargate.log(Level.FINE, String.format("Teleporting player to destination portal '%s'", destinationPortal.getName()));
            destinationPortal.teleportHere(player, null);
        }
    }

    /**
     * Adds a player to the BungeeCord teleportation queue
     *
     * @param playerName    <p>The name of the player to add to the queue</p>
     * @param portalName    <p>The name of the portal the player is teleporting to</p>
     * @param networkName   <p>The name of the network the entry portal belongs to</p>
     * @param storageType <p>Whether the entry portal belongs to an inter-server network</p>
     */
    private void addToQueue(String playerName, String portalName, String networkName,
                            StorageType storageType) {
        Network network = registry.getNetwork(networkName, storageType);

        /*
         * In some cases, there might be issues with a portal being deleted in a server, but still present in the
         * inter-server database. Therefore, we have to check for that...
         */
        if (network == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = network.
            String msg = String.format("Inter-server network ''%s'' could not be found", networkName);
            Stargate.log(Level.WARNING, msg);
        }
        Portal portal = network == null ? null : network.getPortal(portalName);
        if (portal == null) {
            // Error: This bungee portal's %type% has been removed from the destination server instance.
            //(See Discussion One) %type% = gate.
            String msg = String.format("Inter-server portal ''%s'' in network ''%s'' could not be found", portalName, networkName);
            Stargate.log(Level.WARNING, msg);
        }
        bungeeQueue.put(playerName, portal);
    }

    @Override
    public Portal pullFromQueue(String playerName) {
        return bungeeQueue.remove(playerName);
    }
}
