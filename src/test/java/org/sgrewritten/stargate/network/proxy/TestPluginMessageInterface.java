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

    Queue<TwoTuple<String, PluginChannel>> sentMessagesQueue = new ArrayDeque<>();

    @Override
    public void scheduleSendMessage(String message, PluginChannel channel) {
        try {
            this.sendMessage(message, channel, Stargate.getInstance());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(String dataMsg, PluginChannel pluginChannel, Plugin plugin) throws IOException {
        sentMessagesQueue.add(new TwoTuple<>(dataMsg, pluginChannel));
    }

    public @Nullable TwoTuple<String, PluginChannel> getSentMessageFromQueue() {
        return sentMessagesQueue.poll();
    }
}
