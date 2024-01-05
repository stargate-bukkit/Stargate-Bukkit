package org.sgrewritten.stargate.network.proxy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.ForcibleFunctionAction;
import org.sgrewritten.stargate.property.PluginChannel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class BukkitPluginMessageInterface implements PluginMessageInterface {

    @Override
    public void scheduleSendMessage(String message, PluginChannel channel) {
        Stargate stargate = Stargate.getInstance();

        Stargate.addSynchronousSecAction(new ForcibleFunctionAction((forceEnd) -> {
            if (stargate.getServer().getOnlinePlayers().size() > 0 || forceEnd) {
                try {
                    this.sendMessage(message, channel, stargate);
                    return true;
                } catch (IOException e) {
                    Stargate.log(Level.WARNING, "Error sending BungeeCord connect packet");
                    Stargate.log(e);
                }
            }
            return false;

        }), true);
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
}
