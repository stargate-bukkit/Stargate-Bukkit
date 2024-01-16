package org.sgrewritten.stargate.network.proxy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.ForcibleFunctionAction;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.thread.task.StargateGlobalTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class BukkitPluginMessageInterface implements PluginMessageInterface {

    @Override
    public void scheduleSendMessage(String message, PluginChannel channel) {
        Stargate stargate = Stargate.getInstance();


        new StargateGlobalTask(() -> {
            try {
                this.sendMessage(message, channel, stargate);
            } catch (IOException e) {
                Stargate.log(Level.WARNING, "Error sending BungeeCord connect packet");
                Stargate.log(e);
            }
        }).run(true);
    }


    @Override
    public void sendMessage(String message, PluginChannel channel, Plugin plugin) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF(PluginChannel.FORWARD.getChannel());
            dataOutputStream.writeUTF("ALL");
            dataOutputStream.writeUTF(channel.getChannel());
            Stargate.log(Level.FINER, String.format("Sending bungee message:%n%s", message));
            dataOutputStream.writeUTF(message);
            Bukkit.getServer().sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), byteArrayOutputStream.toByteArray());
        }
    }

    @Override
    public void sendDirectedMessage(String message, PluginChannel channel, Plugin plugin, String server) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF(PluginChannel.FORWARD.getChannel());
            dataOutputStream.writeUTF(server);
            dataOutputStream.writeUTF(channel.getChannel());
            Stargate.log(Level.FINER, String.format("Sending bungee message:%n%s", message));
            dataOutputStream.writeUTF(message);
            Bukkit.getServer().sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), byteArrayOutputStream.toByteArray());
        }
    }
}
