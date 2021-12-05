package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.PluginChannel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.NoFormatFoundException;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class BungeePortal extends Portal {

    private static Network LEGACY_NETWORK;

    static {
        try {
            LEGACY_NETWORK = new Network("§§§§§§#BUNGEE#§§§§§§", null, null);
        } catch (NameErrorException e) {
            e.printStackTrace();
        }
    }

    /**
     * CHEATS! we love cheats. This one helps to save the legacy bungee gate into sql table so that the
     * target server is stored as a replacement to network.
     */
    private final Network cheatNet;
    private final LegacyVirtualPortal targetPortal;
    private final String serverDestination;

    BungeePortal(Network network, String name, String destination, String serverDestination, Block sign, Set<PortalFlag> flags, UUID ownerUUID)
            throws NameErrorException, NoFormatFoundException, GateConflictException {
        super(network, name, sign, flags, ownerUUID);

        /*
         * Create a virtual portal that handles everything related
         * to moving the player to a different server. This is set
         * as destination portal, of course.
         *
         * Note that this is only used locally inside this portal
         * and can not be found (should not) in any network anywhere.
         */
        targetPortal = new LegacyVirtualPortal(serverDestination, destination, LEGACY_NETWORK, EnumSet.noneOf(PortalFlag.class), ownerUUID);
        this.serverDestination = serverDestination;
        cheatNet = new Network(serverDestination, null, null);
    }

    @Override
    public void drawControlMechanism() {
        Stargate.log(Level.FINEST, "serverDestination = " + serverDestination);

        String[] lines = new String[4];
        lines[0] = super.colorDrawer.parseName(NameSurround.PORTAL, this);
        lines[1] = super.colorDrawer.parseName(NameSurround.DESTINATION, loadDestination());
        lines[2] = super.colorDrawer.parseLine(serverDestination);
        lines[3] = "";
        getGate().drawControlMechanism(lines, !hasFlag(PortalFlag.ALWAYS_ON));
    }

    @Override
    public IPortal loadDestination() {
        return targetPortal;
    }

    @Override
    public Network getNetwork() {
        return cheatNet;
    }

    class LegacyVirtualPortal extends VirtualPortal {

        public LegacyVirtualPortal(String server, String name, Network net, Set<PortalFlag> flags, UUID ownerUUID) {
            super(server, name, net, flags, ownerUUID);
        }

        @Override
        public void teleportHere(Entity target, Portal origin) {
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
                String msg = player.getName() + "#@#" + destination.getName();
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
}
