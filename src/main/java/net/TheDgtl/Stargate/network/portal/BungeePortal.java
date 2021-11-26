package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.UUID;
import java.util.logging.Level;

public class BungeePortal extends Portal {

    private static Network LEGACY_NETWORK;

    static {
        try {
            LEGACY_NETWORK = new Network("§§§§§§#BUNGEE#§§§§§§", null, null);
        } catch (NameError e) {
            e.printStackTrace();
        }
    }

    /**
     * CHEATS! we love cheats. This one helps saving the legacy bungee gate into sql table so that the
     * target server is stored as a replacement to network.
     */
    private Network cheatNet;
    private LegacyVirtualPortal targetPortal;
    private String serverDesti;

    BungeePortal(Network network, String name, String desti, String serverDesti, Block sign, EnumSet<PortalFlag> flags, UUID ownerUUID)
            throws NameError, NoFormatFound, GateConflict {
        super(network, name, sign, flags, ownerUUID);

        /*
         * Create a virtual portal that handles everything related
         * to moving the player to a different server. This is set
         * as destination portal, of course.
         *
         * Note that this is only used locally inside this portal
         * and can not be found (should not) in any network anywhere.
         */
        targetPortal = new LegacyVirtualPortal(serverDesti, desti, LEGACY_NETWORK, EnumSet.noneOf(PortalFlag.class), ownerUUID);
        this.serverDesti = serverDesti;
        cheatNet = new Network(serverDesti, null, null);
        drawControll();
    }

    @Override
    public void onSignClick(Action action, Player actor) {
    }

    @Override
    public void drawControll() {
        Stargate.log(Level.FINEST, "serverDesti = " + serverDesti);

        String[] lines = new String[4];
        lines[0] = IPortal.getDefaultColor(super.isLightSign()) + NameSurround.PORTAL.getSurround(getColoredName(super.isLightSign()));
        lines[1] = IPortal.getDefaultColor(super.isLightSign()) + NameSurround.DESTI.getSurround(targetPortal.getColoredName(super.isLightSign()));
        lines[2] = IPortal.getDefaultColor(super.isLightSign()) + serverDesti;
        lines[3] = "";
        getGate().drawControll(lines, !hasFlag(PortalFlag.ALWAYS_ON));
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

        public LegacyVirtualPortal(String server, String name, Network net, EnumSet<PortalFlag> flags, UUID ownerUUID) {
            super(server, name, net, flags, ownerUUID);
        }

        @Override
        public void teleportHere(Entity target, BlockFace originFacing) {
            Stargate plugin = JavaPlugin.getPlugin(Stargate.class);
            if (!(target instanceof Player)) {
                return;
            }
            Player player = (Player) target;
            try {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF(Channel.FORWARD.getChannel());
                msgData.writeUTF(server);
                msgData.writeUTF(Channel.LEGACY_BUNGEE.getChannel());
                String msg = player.getName() + "#@#" + destination.getName();
                msgData.writeUTF(msg);
                Stargate.log(Level.FINEST, bao.toString());
                player.sendPluginMessage(plugin, Channel.BUNGEE.getChannel(), bao.toByteArray());
            } catch (IOException ex) {
                Stargate.log(Level.SEVERE, "[Stargate] Error sending BungeeCord teleport packet");
                ex.printStackTrace();
                return;
            }

            try {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                DataOutputStream msgData = new DataOutputStream(bao);
                msgData.writeUTF(Channel.PLAYER_CONNECT.getChannel());
                msgData.writeUTF(server);
                player.sendPluginMessage(plugin, Channel.BUNGEE.getChannel(), bao.toByteArray());
            } catch (IOException ex) {
                Stargate.log(Level.SEVERE, "[Stargate] Error sending BungeeCord connect packet");
                ex.printStackTrace();
            }

        }
    }
}
