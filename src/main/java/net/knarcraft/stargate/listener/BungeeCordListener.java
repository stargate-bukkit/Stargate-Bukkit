package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.utility.BungeeHelper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

/**
 * This listener teleports a user if a valid message is received from BungeeCord
 *
 * <p>Specifically, if a string starts with SGBungee encoded to be readable by readUTF followed by
 * [PlayerUUID]delimiter[DestinationPortal] is received on the BungeeCord channel, this listener will teleport the
 * player to the destination portal.</p>
 */
public class BungeeCordListener implements PluginMessageListener {

    /**
     * Receives plugin messages
     *
     * @param channel <p>The channel the message was received on</p>
     * @param unused  <p>Unused.</p>
     * @param message <p>The message received from the plugin</p>
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player unused, byte[] message) {
        //Ignore plugin messages if some other plugin message is received
        if (!channel.equals(BungeeHelper.getBungeeChannel())) {
            return;
        }

        //Try to read the plugin message
        String receivedMessage = BungeeHelper.readPluginMessage(message);
        if (receivedMessage == null) {
            return;
        }

        //Use the message to initiate teleportation
        BungeeHelper.handleTeleportMessage(receivedMessage);
    }


}
