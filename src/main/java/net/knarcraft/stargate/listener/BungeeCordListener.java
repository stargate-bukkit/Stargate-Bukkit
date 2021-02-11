package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.Stargate;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * This listener teleports a user if a valid message is received from BungeeCord
 *
 * <p>Specifically, if a string starts with SGBungee encoded to be readable by readUTF followed by
 * PlayerName#@#DestinationPortal is received on the BungeeCord channel, this listener will teleport the player to the
 * destination portal.</p>
 */
public class BungeeCordListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player unused, byte[] message) {
        if (!Stargate.enableBungee || !channel.equals("BungeeCord")) {
            return;
        }

        String receivedMessage = readPluginMessage(message);
        if (receivedMessage == null) {
            return;
        }

        String[] messageParts = receivedMessage.split("#@#");

        String playerName = messageParts[0];
        String destination = messageParts[1];

        // Check if the player is online, if so, teleport, otherwise, queue
        Player player = Stargate.server.getPlayer(playerName);
        if (player == null) {
            Stargate.bungeeQueue.put(playerName.toLowerCase(), destination);
        } else {
            Portal destinationPortal = Portal.getBungeeGate(destination);
            // Specified an invalid gate. For now we'll just let them connect at their current location
            if (destinationPortal == null) {
                Stargate.log.info(Stargate.getString("prefix") + "Bungee gate " + destination + " does not exist");
                return;
            }
            destinationPortal.teleport(player, destinationPortal, null);
        }
    }

    /**
     * Reads a plugin message byte array to a string
     * @param message <p>The byte array to read</p>
     * @return <p>The message contained in the byte array</p>
     */
    private String readPluginMessage(byte[] message) {
        // Get data from message
        String inChannel;
        byte[] data;
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(message));
            inChannel = dataInputStream.readUTF();
            short dataLength = dataInputStream.readShort();
            data = new byte[dataLength];
            dataInputStream.readFully(data);
        } catch (IOException ex) {
            Stargate.log.severe(Stargate.getString("prefix") + "Error receiving BungeeCord message");
            ex.printStackTrace();
            return null;
        }

        // Verify that it's an SGBungee packet
        if (!inChannel.equals("SGBungee")) {
            return null;
        }

        // Data should be player name, and destination gate name
        return new String(data);
    }

}
