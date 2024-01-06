package org.sgrewritten.stargate.network.proxy;

import org.bukkit.plugin.Plugin;
import org.sgrewritten.stargate.property.PluginChannel;

import java.io.IOException;

public interface PluginMessageInterface {

    void scheduleSendMessage(String message, PluginChannel channel);

    void sendMessage(String dataMsg, PluginChannel pluginChannel, Plugin plugin) throws IOException;

    void sendDirectedMessage(String message, PluginChannel channel, Plugin plugin, String server) throws IOException;
}
