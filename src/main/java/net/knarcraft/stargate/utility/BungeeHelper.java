package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.teleporter.PlayerTeleporter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class contains helpful functions to help with sending and receiving BungeeCord plugin messages
 */
public final class BungeeHelper {

    private final static String bungeeSubChannel = "SGBungee";
    private final static String bungeeChannel = "BungeeCord";
    private final static String teleportMessageDelimiter = "#@#";
    private final static Map<UUID, String> bungeeQueue = new HashMap<>();

    private BungeeHelper() {

    }

    /**
     * Get the plugin message channel use for BungeeCord messages
     *
     * @return <p>The bungee plugin channel</p>
     */
    public static String getBungeeChannel() {
        return bungeeChannel;
    }

    /**
     * Removes a player from the queue of players teleporting through BungeeCord
     *
     * <p>Whenever a BungeeCord teleportation message is received and the player is not currently connected to this
     * server, it'll be added to this queue. Once the player joins this server, the player should be removed from the
     * queue and teleported to the destination.</p>
     *
     * @param playerUUID <p>The UUID of the player to remove</p>
     * @return <p>The name of the destination portal the player should be teleported to</p>
     */
    public static String removeFromQueue(UUID playerUUID) {
        return bungeeQueue.remove(playerUUID);
    }

    /**
     * Sends a plugin message to BungeeCord allowing the target server to catch it
     *
     * @param player         <p>The teleporting player</p>
     * @param entrancePortal <p>The portal the player is teleporting from</p>
     * @return <p>True if the message was successfully sent</p>
     */
    public static boolean sendTeleportationMessage(Player player, Portal entrancePortal) {
        try {
            //Build the teleportation message, format is <player identifier>delimiter<destination>
            String message = player.getUniqueId() + teleportMessageDelimiter + entrancePortal.getDestinationName();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            //Build the message data and send it over the SGBungee BungeeCord channel
            dataOutputStream.writeUTF("Forward");
            //Send the message to the server defined in the entrance portal's network line
            dataOutputStream.writeUTF(stripColor(entrancePortal.getNetwork()));
            //Specify the sub-channel/tag to make it recognizable on arrival
            dataOutputStream.writeUTF(bungeeSubChannel);
            //Write the length of the message
            dataOutputStream.writeShort(message.length());
            //Write the actual message
            dataOutputStream.writeBytes(message);
            //Send the plugin message
            player.sendPluginMessage(Stargate.getInstance(), bungeeChannel, byteArrayOutputStream.toByteArray());
        } catch (IOException ex) {
            Stargate.logSevere("Error sending BungeeCord teleport packet");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Sends the bungee message necessary to make a player connect  to another server
     *
     * @param player         <p>The player to teleport</p>
     * @param entrancePortal <p>The bungee portal the player is teleporting from</p>
     * @return <p>True if the plugin message was sent successfully</p>
     */
    public static boolean changeServer(Player player, Portal entrancePortal) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            //Send a connect-message to connect the player to the server defined in the entrance portal's network line
            dataOutputStream.writeUTF("Connect");
            dataOutputStream.writeUTF(stripColor(entrancePortal.getNetwork()));

            //Send the plugin message
            player.sendPluginMessage(Stargate.getInstance(), bungeeChannel, byteArrayOutputStream.toByteArray());
        } catch (IOException ex) {
            Stargate.logSevere("Error sending BungeeCord connect packet");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Reads a plugin message byte array to a string if it's sent from another stargate plugin
     *
     * @param message <p>The byte array to read</p>
     * @return <p>The message contained in the byte array, or null on failure</p>
     */
    public static String readPluginMessage(byte[] message) {
        byte[] data;
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(message));
            String subChannel = dataInputStream.readUTF();
            //Only listen for the SGBungee channel
            if (!subChannel.equals(bungeeSubChannel)) {
                return null;
            }

            //Get the length of the contained message
            short dataLength = dataInputStream.readShort();
            //Prepare a byte array for the sent message
            data = new byte[dataLength];
            //Read the message to the prepared array
            dataInputStream.readFully(data);
        } catch (IOException ex) {
            Stargate.logSevere("Error receiving BungeeCord message");
            ex.printStackTrace();
            return null;
        }
        return new String(data);
    }

    /**
     * Handles the receival of a teleport message
     *
     * @param receivedMessage <p>The received teleport message</p>
     */
    public static void handleTeleportMessage(String receivedMessage) {
        //Get the player id and destination from the message
        String[] messageParts = receivedMessage.split(teleportMessageDelimiter);
        UUID playerUUID = UUID.fromString(messageParts[0]);
        String destination = messageParts[1];

        //Check if the player is online, if so, teleport, otherwise, queue
        Player player = Stargate.getInstance().getServer().getPlayer(playerUUID);
        if (player == null) {
            bungeeQueue.put(playerUUID, destination);
        } else {
            Portal destinationPortal = PortalHandler.getBungeePortal(destination);
            //If teleporting to an invalid portal, let the server decide where the player arrives
            if (destinationPortal == null) {
                Stargate.logInfo(String.format("Bungee portal %s does not exist", destination));
                return;
            }
            new PlayerTeleporter(destinationPortal, player).teleport(destinationPortal, null);
        }
    }

    /**
     * Teleports a player to a bungee gate
     *
     * @param player         <p>The player to teleport</p>
     * @param entrancePortal <p>The gate the player is entering from</p>
     * @param event          <p>The event causing the teleportation</p>
     * @return <p>True if the teleportation was successful</p>
     */
    public static boolean bungeeTeleport(Player player, Portal entrancePortal, PlayerMoveEvent event) {
        //Check if bungee is actually enabled
        if (!Stargate.getGateConfig().enableBungee()) {
            if (!entrancePortal.getOptions().isSilent()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("bungeeDisabled"));
            }
            entrancePortal.getPortalOpener().closePortal(false);
            return false;
        }

        //Teleport the player back to this gate, for sanity's sake
        new PlayerTeleporter(entrancePortal, player).teleport(entrancePortal, event);

        //Send the SGBungee packet first, it will be queued by BC if required
        if (!BungeeHelper.sendTeleportationMessage(player, entrancePortal)) {
            Stargate.debug("bungeeTeleport", "Unable to send teleportation message");
            return false;
        }

        //Send the connect-message to make the player change server
        if (!BungeeHelper.changeServer(player, entrancePortal)) {
            Stargate.debug("bungeeTeleport", "Unable to change server");
            return false;
        }

        Stargate.debug("bungeeTeleport", "Teleported player to another server");
        return true;
    }

    /**
     * Strips all color tags from a string
     *
     * @param string <p>The string to strip color from</p>
     * @return <p>The string without color codes</p>
     */
    private static String stripColor(String string) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', string));
    }

}
