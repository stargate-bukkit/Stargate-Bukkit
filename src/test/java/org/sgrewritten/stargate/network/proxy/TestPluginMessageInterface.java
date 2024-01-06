package org.sgrewritten.stargate.network.proxy;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.property.PluginChannel;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class TestPluginMessageInterface implements PluginMessageInterface {

    final Queue<TwoTuple<String, PluginChannel>> sentMessagesQueue = new ArrayDeque<>();

    @Override
    public void scheduleSendMessage(String message, PluginChannel channel) {
        this.sendMessage(message, channel, Stargate.getInstance());
    }

    @Override
    public void sendMessage(String dataMsg, PluginChannel pluginChannel, Plugin plugin) {
        sentMessagesQueue.add(new TwoTuple<>(dataMsg, pluginChannel));
    }

    @Override
    public void sendDirectedMessage(String message, PluginChannel channel, Plugin plugin, String server) throws IOException {
        sentMessagesQueue.add(new TwoTuple<>(message, channel));
    }

    public @Nullable TwoTuple<String, PluginChannel> getSentMessageFromQueue() {
        return sentMessagesQueue.poll();
    }
}
