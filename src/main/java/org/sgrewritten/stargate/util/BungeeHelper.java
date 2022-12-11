package org.sgrewritten.stargate.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.property.StargateProtocolProperty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A helper class for dealing with BungeeCord
 */
public final class BungeeHelper {

    private static final HashMap<String, Portal> bungeeQueue = new HashMap<>();

    private BungeeHelper() {

    }

    /**
     * Gets the server's unique ID
     *
     * <p>If the server this plugin runs on is missing an id, a new unique id will be generated.</p>
     *
     * @param dataFolder     <p>The folder containing plugin data</p>
     * @param internalFolder <p>The folder containing internal hidden files</p>
     */
    public static void getServerId(String dataFolder, String internalFolder) {
        Stargate.log(Level.FINEST, dataFolder);
        File path = new File(dataFolder, internalFolder);
        if (!path.exists() && path.mkdir()) {
            try {
                Files.setAttribute(path.toPath(), "dos:hidden", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(path, "serverUUID.txt");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new FileNotFoundException("serverUUID.txt was not found and could not be created");
                }
                BufferedWriter writer = FileHelper.getBufferedWriter(file, false);
                writer.write(UUID.randomUUID().toString());
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            BufferedReader reader = FileHelper.getBufferedReader(file);
            Stargate.setServerUUID(UUID.fromString(reader.readLine()));
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a player to the BungeeCord teleportation queue
     *
     * @param registry      <p>The registry to use for looking up networks</p>
     * @param playerName    <p>The name of the player to add to the queue</p>
     * @param portalName    <p>The name of the portal the player is teleporting to</p>
     * @param networkName   <p>The name of the network the entry portal belongs to</p>
     * @param isInterServer <p>Whether the entry portal belongs to an inter-server network</p>
     */
    public static void addToQueue(RegistryAPI registry, String playerName, String portalName, String networkName,
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

    /**
     * Gets a portal from the BungeeCord teleportation queue
     *
     * @param playerName <p>The player to pull from the queue</p>
     * @return <p>The portal the player should be teleported to</p>
     */
    public static Portal pullFromQueue(String playerName) {
        return bungeeQueue.remove(playerName);
    }


    /**
     * Handle the connection of a player using the legacy Stargate method
     *
     * <p>This is done to let servers on any of the old Stargate forks connect to this version.</p>
     *
     * @param message <p>The legacy connect message to parse and handle</p>
     */
    public static void legacyPlayerConnect(String message) {
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

            BungeeHelper.addToQueue(registry, playerName, destination, bungeeNetworkName, false);
        } else {
            Network network = getLegacyBungeeNetwork(registry, bungeeNetworkName);
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
     * Gets the legacy bungee network
     *
     * <p>If the network doesn't already exist, it will be created</p>
     *
     * @param registry      <p>The registry to use</p>
     * @param bungeeNetwork <p>The name of the legacy bungee network</p>
     * @return <p>The legacy bungee network, or null if unobtainable</p>
     */
    private static Network getLegacyBungeeNetwork(RegistryAPI registry, String bungeeNetwork) {
        Network network = registry.getNetwork(bungeeNetwork, false);
        //Create the legacy network if it doesn't already exist
        try {
            if (network == null) {
                registry.createNetwork(bungeeNetwork, NetworkType.CUSTOM, false, false);
                network = registry.getNetwork(bungeeNetwork, false);
            }
        } catch (InvalidNameException | NameLengthException | NameConflictException e) {
            //Ignored as the null check will take care of this
        }
        if (network == null) {
            Stargate.log(Level.WARNING, "Unable to get or create the legacy bungee network");
        }
        return network;
    }


    /**
     * Handles a player teleport message
     *
     * @param message <p>The player teleport message to parse and handle</p>
     */
    public static void playerConnect(String message) {
        JsonParser parser = new JsonParser();
        Stargate.log(Level.FINEST, message);

        JsonObject json = (JsonObject) parser.parse(message);
        String playerName = json.get(StargateProtocolProperty.PLAYER.toString()).getAsString();
        String portalName = json.get(StargateProtocolProperty.PORTAL.toString()).getAsString();
        String networkName = json.get(StargateProtocolProperty.NETWORK.toString()).getAsString();

        Player player = Stargate.getInstance().getServer().getPlayer(playerName);
        if (player == null) {
            Stargate.log(Level.FINEST, "Player was null; adding to queue");
            BungeeHelper.addToQueue(Stargate.getRegistryStatic(), playerName, portalName, networkName, true);
            return;
        }

        Stargate.log(Level.FINEST, "Player was not null; trying to teleport");
        Network network = Stargate.getRegistryStatic().getNetwork(networkName, true);
        if (network == null) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.BUNGEE_INVALID_NETWORK));
            return;
        }
        Portal destinationPortal = network.getPortal(portalName);
        if (destinationPortal == null) {
            player.sendMessage(Stargate.getLanguageManagerStatic().getErrorMessage(TranslatableMessage.BUNGEE_INVALID_GATE));
            return;
        }
        destinationPortal.teleportHere(player, null);

    }
}
