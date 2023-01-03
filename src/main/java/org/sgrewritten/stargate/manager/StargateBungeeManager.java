package org.sgrewritten.stargate.manager;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.VirtualPortal;
import org.sgrewritten.stargate.property.StargateProtocolProperty;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StargateBungeeManager implements BungeeManager{
    
    private final RegistryAPI registry;
    private @NotNull LanguageManager languageManager;
    private final HashMap<String, Portal> bungeeQueue = new HashMap<>();

    public StargateBungeeManager(@NotNull RegistryAPI registry, @NotNull LanguageManager languageManager) {
        this.registry = Objects.requireNonNull(registry);
        this.languageManager = Objects.requireNonNull(languageManager);
    }
    
    @Override
    public void updateNetwork(String message) {
        JsonParser parser = new JsonParser();
        Stargate.log(Level.FINEST, message);
        JsonObject json = (JsonObject) parser.parse(message);

        String requestTypeString = json.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString();
        StargateProtocolRequestType requestType = StargateProtocolRequestType.valueOf(requestTypeString);

        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String network = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();
        String server = json.get(StargateProtocolProperty.SERVER.toString()).getAsString();
        Set<PortalFlag> flags = PortalFlag.parseFlags(json.get(StargateProtocolProperty.PORTAL_FLAG.toString()).getAsString());
        UUID ownerUUID = UUID.fromString(json.get(StargateProtocolProperty.OWNER.toString()).getAsString());

        try {
            registry.createNetwork(network, flags, false);
        } catch ( NameConflictException ignored) {
            
        } catch (InvalidNameException | NameLengthException  e) {
            e.printStackTrace();
        }
        try {
            InterServerNetwork targetNetwork = (InterServerNetwork) registry.getNetwork(network, true);
            VirtualPortal portal = new VirtualPortal(server, portalName, targetNetwork, flags, ownerUUID);
            switch (requestType) {
                case PORTAL_ADD:
                    targetNetwork.addPortal(portal, false);
                    Stargate.log(Level.FINE, String.format("Adding virtual portal %s in inter-server network %s", portalName, network));
                    break;
                case PORTAL_REMOVE:
                    Stargate.log(Level.FINE, String.format("Removing virtual portal %s in inter-server network %s", portalName, network));
                    targetNetwork.removePortal(portal, false);
                    break;
            }
            targetNetwork.updatePortals();
        } catch (NameConflictException ignored) {
        }
    }
    
    @Override
    public void playerConnect(String message) {
        JsonParser parser = new JsonParser();
        Stargate.log(Level.FINEST, message);

        JsonObject json = (JsonObject) parser.parse(message);
        String playerName = json.get(StargateProtocolProperty.PLAYER.toString()).getAsString();
        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String networkName = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();

        Player player = Bukkit.getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");
            addToQueue(playerName, portalName, networkName, true);
            return;
        }

        Stargate.log(Level.FINEST, "Player was not null; trying to teleport");
        Network network = registry.getNetwork(networkName, true);
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
        RegistryAPI registry = Stargate.getInstance().getRegistry();
        String bungeeNetworkName = BungeePortal.getLegacyNetworkName();

        String[] parts = message.split("#@#");

        String playerName = parts[0];
        String destination = parts[1];

        Stargate.log(Level.FINER, "destination=" + destination + ",player=" + playerName);

        // Check if the player is online, if so, teleport, otherwise, queue
        Player player = Stargate.getInstance().getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");

            addToQueue(playerName, destination, bungeeNetworkName, false);
        } else {
            Network network = BungeeHelper.getLegacyBungeeNetwork(registry, bungeeNetworkName);
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
     * @param isInterServer <p>Whether the entry portal belongs to an inter-server network</p>
     */
    private void addToQueue(String playerName, String portalName, String networkName,
                                  boolean isInterServer) {
        Network network = registry.getNetwork(networkName, isInterServer);

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
