package net.TheDgtl.Stargate.network.portal;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocolProperty;
import net.TheDgtl.Stargate.network.Network;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A virtual portal, which does not exist. Symbolises a portal that is outside
 * this server and acts as an interface to send relevant inter-server packets.
 *
 * @author Thorin
 */
public class VirtualPortal implements IPortal {

    protected final String server;
    private final String name;
    private Network network;
    private final EnumSet<PortalFlag> flags;
    private final UUID ownerUUID;

    public VirtualPortal(String server, String name, Network net, EnumSet<PortalFlag> flags, UUID ownerUUID) {
        this.server = server;
        this.name = name;
        this.network = net;
        this.flags = flags;
        this.ownerUUID = ownerUUID;
        network.addPortal(this, false);
    }

    @Override
    public void teleportHere(Entity target, Portal origin) {
        //TODO: implement vehicle compatibility.
        Stargate.log(Level.FINEST, "");
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
            msgData.writeUTF(Channel.PLAYER_TELEPORT.getChannel());
            JsonObject data = new JsonObject();
            data.add(StargateProtocolProperty.PLAYER.toString(), new JsonPrimitive(player.getName()));
            data.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(this.name));
            data.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(network.getName()));
            String dataMsg = data.toString();
            msgData.writeUTF(dataMsg);
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

    /**
     * TODO not implemented, probably never will / as it would be overusing much needed BungeeCord message data
     */
    @Override
    public void close(boolean force) {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setNetwork(Network targetNet) {
        this.network = targetNet;
    }

    /**
     * TODO Not implemented
     */
    @Override
    public void setOverrideDestination(IPortal destination) {
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    /**
     * TODO not implemented
     */
    @Override
    public void open(Player player) {
    }

    @Override
    public void destroy() {
        network.removePortal(this, false);
    }

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public void doTeleport(Entity player) {
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isOpenFor(Entity player) {
        return false;
    }

    @Override
    public String getAllFlagsString() {
        return "";
    }

    @Override
    public Location getSignPos() {
        return null;
    }

    @Override
    public String getDesignName() {
        return null;
    }

    @Override
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void update() {
    }

}
