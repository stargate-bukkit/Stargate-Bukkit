package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.PlayerTeleporter;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains helpful functions to help with sending and receiving BungeeCord plugin messages
 */
public final class BungeeHelper {

    private final static String bungeeSubChannel = "SGBungee";
    private final static String bungeeChannel = "BungeeCord";
    private final static String teleportMessageDelimiter = "#@#";
    private final static Map<String, String> bungeeQueue = new HashMap<>();

    private BungeeHelper() {

    }

    /**
     * Removes a player from the queue of players teleporting through BungeeCord
     *
     * <p>Whenever a BungeeCord teleportation message is received and the player is not currently connected to this
     * server, it'll be added to this queue. Once the player joins this server, the player should be removed from the
     * queue and teleported to the destination.</p>
     *
     * @param playerName <p>The name of the player to remove</p>
     * @return <p>The name of the destination portal the player should be teleported to</p>
     */
    public static String removeFromQueue(String playerName) {
        return bungeeQueue.remove(playerName.toLowerCase());
    }

    /**
     * Sends a plugin message to BungeeCord allowing the target server to catch it
     *
     * @param player         <p>The player teleporting</p>
     * @param entrancePortal <p>The portal the player is teleporting from</p>
     * @return <p>True if the message was successfully sent</p>
     */
    public static boolean sendTeleportationMessage(Player player, Portal entrancePortal) {
        try {
            // Build the message, format is <player>#@#<destination>
            String message = player.getName() + teleportMessageDelimiter + entrancePortal.getDestinationName();
            // Build the message data, sent over the SGBungee BungeeCord channel
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF("Forward");
            dataOutputStream.writeUTF(entrancePortal.getNetwork());    // Server
            //Specify SGBungee channel/tag
            dataOutputStream.writeUTF(bungeeSubChannel);
            //Length of the message
            dataOutputStream.writeShort(message.length());
            //The data to send
            dataOutputStream.writeBytes(message);
            player.sendPluginMessage(Stargate.stargate, bungeeChannel, byteArrayOutputStream.toByteArray());
        } catch (IOException ex) {
            Stargate.getConsoleLogger().severe(Stargate.getString("prefix") + "Error sending BungeeCord teleport packet");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Sends the bungee message necessary to change the server
     *
     * @param player         <p>The player to teleport</p>
     * @param entrancePortal <p>The bungee portal the player teleports from</p>
     * @return <p>True if able to send the plugin message</p>
     */
    public static boolean changeServer(Player player, Portal entrancePortal) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF("Connect");
            dataOutputStream.writeUTF(entrancePortal.getNetwork());

            player.sendPluginMessage(Stargate.stargate, bungeeChannel, byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.reset();
        } catch (IOException ex) {
            Stargate.getConsoleLogger().severe(Stargate.getString("prefix") +
                    "Error sending BungeeCord connect packet");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Reads a plugin message byte array to a string if it's sent from another stargate plugin
     *
     * @param message <p>The byte array to read</p>
     * @return <p>The message contained in the byte array or null on failure</p>
     */
    public static String readPluginMessage(byte[] message) {
        // Get data from message
        byte[] data;
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(message));
            String subChannel = dataInputStream.readUTF();
            //Only listen for the SGBungee channel
            if (!subChannel.equals(bungeeSubChannel)) {
                return null;
            }
            short dataLength = dataInputStream.readShort();
            data = new byte[dataLength];
            dataInputStream.readFully(data);
        } catch (IOException ex) {
            Stargate.getConsoleLogger().severe(Stargate.getString("prefix") +
                    "Error receiving BungeeCord message");
            ex.printStackTrace();
            return null;
        }

        // Data should be player name, and destination gate name
        return new String(data);
    }

    /**
     * Handles the receival of a teleport message
     *
     * @param receivedMessage <p>The received message</p>
     */
    public static void handleTeleportMessage(String receivedMessage) {
        String[] messageParts = receivedMessage.split(teleportMessageDelimiter);

        String playerName = messageParts[0];
        String destination = messageParts[1];

        // Check if the player is online, if so, teleport, otherwise, queue
        Player player = Stargate.server.getPlayer(playerName);
        if (player == null) {
            bungeeQueue.put(playerName.toLowerCase(), destination);
        } else {
            Portal destinationPortal = PortalHandler.getBungeePortal(destination);
            // Specified an invalid gate. For now, we'll just let them connect at their current location
            if (destinationPortal == null) {
                Stargate.getConsoleLogger().info(Stargate.getString("prefix") + "Bungee gate " +
                        destination + " does not exist");
                return;
            }
            new PlayerTeleporter(destinationPortal, player).teleport(destinationPortal, null);
        }
    }

}
