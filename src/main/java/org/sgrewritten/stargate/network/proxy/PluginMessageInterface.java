package org.sgrewritten.stargate.network.proxy;

import org.bukkit.plugin.Plugin;
import org.sgrewritten.stargate.property.PluginChannel;

import java.io.IOException;

/**
 * Interface for sending plugin messages if necessary
 */
public interface PluginMessageInterface {

    /**
     * Schedule sending a plugin message until a player joins the server. Will use {@link PluginMessageInterface#sendMessage}
     * to send the message.
     *
     * @param message <p>The message to send</p>
     * @param channel <p>Stargate channel of the message</p>
     */
    void scheduleSendMessage(String message, PluginChannel channel);

    /**
     * Send a plugin message to all servers in specified network. Does not wait until a player joins this server. Data can be lost.
     * @param dataMsg <p>The plugin message to send</p>
     * @param pluginChannel <p>The stargate channel to send the message in</p>
     * @param plugin <p>A plugin (necessary for bukkit api)</p>
     * @throws IOException <p>If unable to send the message</p>
     */
    void sendMessage(String dataMsg, PluginChannel pluginChannel, Plugin plugin) throws IOException;

    /**
     * Send a plugin message directed to only one server
     * @param message <p>The plugin message to send</p>
     * @param channel <p>The stargate channel to send the message in</p>
     * @param plugin <p>A plugin (necessary for bukkit api)</p>
     * @param server <p>The server to send this message to</p>
     * @throws IOException <p>If unable to send the message</p>
     */
    void sendDirectedMessage(String message, PluginChannel channel, Plugin plugin, String server) throws IOException;
}
