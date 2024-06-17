package org.sgrewritten.stargate.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.property.StargateProtocolProperty;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A helper class for dealing with BungeeCord
 */
public final class BungeeHelper {
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
                Stargate.log(e);
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
                Stargate.log(e1);
            }
        }
        try {
            BufferedReader reader = FileHelper.getBufferedReader(file, "utf8");
            Stargate.setServerUUID(UUID.fromString(reader.readLine()));
            reader.close();
        } catch (IOException e) {
            Stargate.log(e);
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
     * @throws UnimplementedFlagException <p>If an unimplemented network type was used in creation (should not trigger)</p>
     */
    public static @Nullable Network getLegacyBungeeNetwork(RegistryAPI registry, NetworkManager networkManager, String bungeeNetwork) throws UnimplementedFlagException {
        Network network = registry.getNetwork(bungeeNetwork, StorageType.LOCAL);
        //Create the legacy network if it doesn't already exist
        try {
            if (network == null) {
                networkManager.createNetwork(bungeeNetwork, NetworkType.CUSTOM, StorageType.LOCAL, false);
                network = registry.getNetwork(bungeeNetwork, StorageType.LOCAL);
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
     * @param portal      <p>The portal which was created or destroyed</p>
     * @param requestType <p>Whether the portal was created or destroyed</p>
     * @return <p>A json string message</p>
     */
    public static String generateJsonMessage(Portal portal, StargateProtocolRequestType requestType) {
        JsonObject jsonData = new JsonObject();
        jsonData.add(StargateProtocolProperty.REQUEST_TYPE.toString(), new JsonPrimitive(requestType.toString()));
        jsonData.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(portal.getNetwork().getId()));
        jsonData.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(portal.getName()));
        jsonData.add(StargateProtocolProperty.SERVER.toString(), new JsonPrimitive(Stargate.getServerName()));
        jsonData.add(StargateProtocolProperty.PORTAL_FLAG.toString(), new JsonPrimitive(portal.getAllFlagsString()));
        jsonData.add(StargateProtocolProperty.OWNER.toString(), new JsonPrimitive(portal.getOwnerUUID().toString()));
        return jsonData.toString();
    }

    /**
     * Get json message for changing the id of a network
     * @param newId <p>The id to change to</p>
     * @param oldId <p>Previous id of the network</p>
     * @return <p>A json string with a rename network message</p>
     */
    public static String generateRenameNetworkMessage(String newId, String oldId) {
        JsonObject jsonData = new JsonObject();
        jsonData.add(StargateProtocolProperty.REQUEST_TYPE.toString(), new JsonPrimitive(StargateProtocolRequestType.NETWORK_RENAME.toString()));
        jsonData.add(StargateProtocolProperty.NEW_NETWORK_NAME.toString(), new JsonPrimitive(newId));
        jsonData.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(oldId));
        return jsonData.toString();
    }

    /**
     *
     * @param newName <p>The new portal name to change to</p>
     * @param oldName <p>The previous portal name</p>
     * @param network <p>The network of the portal</p>
     * @return <p>A json string with a rename portal message</p>
     */
    public static String generateRenamePortalMessage(String newName, String oldName, Network network) {
        JsonObject jsonData = new JsonObject();
        jsonData.add(StargateProtocolProperty.REQUEST_TYPE.toString(), new JsonPrimitive(StargateProtocolRequestType.PORTAL_RENAME.toString()));
        jsonData.add(StargateProtocolProperty.NEW_PORTAL_NAME.toString(), new JsonPrimitive(newName));
        jsonData.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(network.getId()));
        jsonData.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(oldName));
        return jsonData.toString();
    }

    /**
     * @param player <p>The player to teleport</p>
     * @param portal <p>The portal to teleport to (in the other server)</p>
     * @return <p>A json string with a teleport player message</p>
     */
    public static String generateTeleportJsonMessage(String player, Portal portal) {
        JsonObject jsonData = new JsonObject();
        jsonData.add(StargateProtocolProperty.PLAYER.toString(), new JsonPrimitive(player));
        jsonData.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(portal.getName()));
        jsonData.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(portal.getNetwork().getId()));
        return jsonData.toString();
    }

    /**
     * @param player <p>The player to teleport</p>
     * @param portal <p>The portal to teleport</p>
     * @return <p>A legacy format (csv-ish) with teleport message</p>
     */
    public static String generateLegacyTeleportMessage(String player, Portal portal) {
        return player + "#@#" + portal.getName();
    }

    /**
     * To send bungee messages, it is required that a player is online and that stargate knows the server name as defined
     * In the velocity / bungee config.
     * @return <p>True if the server can send bungee messages</p>
     */
    public static boolean canSendBungeeMessages() {
        return (!Bukkit.getServer().getOnlinePlayers().isEmpty() && Stargate.knowsServerName());
    }
}
