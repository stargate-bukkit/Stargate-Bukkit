package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PluginChannel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A virtual portal compatible with the legacy BungeeCord behavior
 */
class LegacyVirtualPortal extends VirtualPortal {

    private final BungeePortal bungeePortal;

    /**
     * Instantiates a new legacy virtual portal
     *
     * @param bungeePortal <p>The Bungee portal this virtual portal represents</p>
     * @param server       <p>The server this virtual portal belongs to</p>
     * @param name         <p>The name of this virtual portal</p>
     * @param network      <p>The network this virtual portal belongs to</p>
     * @param flags        <p>The flags enabled for this virtual portal</p>
     * @param ownerUUID    <p>The UUID of this virtual portal's owner</p>
     */
    public LegacyVirtualPortal(BungeePortal bungeePortal, String server, String name, Network network,
                               Set<PortalFlag> flags, UUID ownerUUID) {
        super(server, name, network, flags, ownerUUID);
        this.bungeePortal = bungeePortal;
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {
        Stargate plugin = JavaPlugin.getPlugin(Stargate.class);
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream msgData = new DataOutputStream(bao);
            msgData.writeUTF(PluginChannel.FORWARD.getChannel());
            msgData.writeUTF(server);
            msgData.writeUTF(PluginChannel.LEGACY_BUNGEE.getChannel());
            String msg = player.getName() + "#@#" + bungeePortal.destination.getName();
            msgData.writeUTF(msg);
            Stargate.log(Level.FINEST, bao.toString());
            player.sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), bao.toByteArray());
        } catch (IOException ex) {
            Stargate.log(Level.SEVERE, "[Stargate] Error sending BungeeCord teleport packet");
            ex.printStackTrace();
            return;
        }

        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream msgData = new DataOutputStream(bao);
            msgData.writeUTF(PluginChannel.PLAYER_CONNECT.getChannel());
            msgData.writeUTF(server);
            player.sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), bao.toByteArray());
        } catch (IOException ex) {
            Stargate.log(Level.SEVERE, "[Stargate] Error sending BungeeCord connect packet");
            ex.printStackTrace();
        }

    }

}
