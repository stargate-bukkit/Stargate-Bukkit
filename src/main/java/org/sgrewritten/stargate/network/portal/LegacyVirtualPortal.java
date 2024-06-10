package org.sgrewritten.stargate.network.portal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.util.BungeeHelper;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A virtual portal compatible with the legacy BungeeCord behavior
 */
public class LegacyVirtualPortal extends VirtualPortal {

    /**
     * Instantiates a new legacy virtual portal
     *
     * @param server    <p>The server this virtual portal belongs to</p>
     * @param name      <p>The name of this virtual portal</p>
     * @param network   <p>The network this virtual portal belongs to</p>
     * @param flags     <p>The flags enabled for this virtual portal</p>
     * @param ownerUUID <p>The UUID of this virtual portal's owner</p>
     */
    public LegacyVirtualPortal(String server, String name, Network network,
                               Set<PortalFlag> flags, Set<Character> unrecognisedFlags, UUID ownerUUID) {
        super(server, name, network, flags, ownerUUID);
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {
        Stargate plugin = JavaPlugin.getPlugin(Stargate.class);
        if (!(target instanceof Player player)) {
            return;
        }
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream msgData = new DataOutputStream(bao);
            msgData.writeUTF(PluginChannel.FORWARD.getChannel());
            msgData.writeUTF(server);
            msgData.writeUTF(PluginChannel.LEGACY_BUNGEE.getChannel());
            String msg = BungeeHelper.generateLegacyTeleportMessage(player.getName(), this);
            msgData.writeUTF(msg);
            Stargate.log(Level.FINEST, "Sending plugin message: " + bao);
            player.sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), bao.toByteArray());
        } catch (IOException e) {
            Stargate.log(Level.WARNING, "[Stargate] Error sending BungeeCord teleport packet");
            Stargate.log(e);
            return;
        }

        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream msgData = new DataOutputStream(bao);
            msgData.writeUTF(PluginChannel.PLAYER_CONNECT.getChannel());
            msgData.writeUTF(server);
            Stargate.log(Level.FINEST, "Sending plugin message: " + bao);
            player.sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), bao.toByteArray());
        } catch (IOException e) {
            Stargate.log(Level.WARNING, "[Stargate] Error sending BungeeCord connect packet");
            Stargate.log(e);
        }

    }

}
