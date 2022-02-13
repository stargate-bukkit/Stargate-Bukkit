package net.TheDgtl.Stargate.network.portal;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.TheDgtl.Stargate.PluginChannel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocolProperty;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.NetworkAPI;

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
 * A virtual representation of a portal on a different server
 *
 * <p>A virtual portal, which does not exist. Symbolises a portal that is outside this server and acts as an interface
 * to send relevant inter-server packets.</p>
 *
 * @author Thorin
 */
public class VirtualPortal implements Portal {

    protected final String server;
    private final String name;
    private NetworkAPI network;
    private final Set<PortalFlag> flags;
    private final UUID ownerUUID;

    /**
     * Instantiates a new virtual portal
     *
     * @param server    <p>The name of the server this portal belongs to</p>
     * @param name      <p>The name of this portal</p>
     * @param network   <p>The network this virtual portal belongs to</p>
     * @param flags     <p>The portal flags enabled for this virtual portal</p>
     * @param ownerUUID <p>The UUID of this virtual portal's owner</p>
     */
    public VirtualPortal(String server, String name, NetworkAPI network, Set<PortalFlag> flags, UUID ownerUUID) {
        this.server = server;
        this.name = name;
        this.network = network;
        this.flags = flags;
        this.ownerUUID = ownerUUID;
        this.network.addPortal(this, false);
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {
        //TODO: implement vehicle compatibility.
        Stargate plugin = JavaPlugin.getPlugin(Stargate.class);
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;

        try {
            sendTeleportMessage(plugin, player);
            sendConnectMessage(plugin, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close(boolean force) {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setNetwork(Network targetNetwork) {
        this.network = targetNetwork;
    }

    @Override
    public void overrideDestination(Portal destination) {
        //TODO: Not implemented
    }

    @Override
    public NetworkAPI getNetwork() {
        return network;
    }

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
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void update() {
    }

    @Override
    public Portal loadDestination() {
        return null;
    }

    /**
     * Sends a BungeeCord connect message to make the player change server
     *
     * @param plugin <p>A Stargate plugin reference</p>
     * @param player <p>The player to teleport</p>
     * @throws IOException <p>If the plugin message cannot be sent</p>
     */
    private void sendConnectMessage(Stargate plugin, Player player) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF(PluginChannel.PLAYER_CONNECT.getChannel());
            dataOutputStream.writeUTF(server);
            player.sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), byteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            Stargate.log(Level.WARNING, "[Stargate] Error sending BungeeCord connect packet");
            throw exception;
        }
    }

    /**
     * Sends a message to the target server about the incoming player
     *
     * <p>Sends a message to this portal's target server telling the server to teleport the incoming player to this
     * virtual portal's real portal equivalent.</p>
     *
     * @param plugin <p>A Stargate plugin reference</p>
     * @param player <p>The player to teleport</p>
     * @throws IOException <p>If the plugin message cannot be sent</p>
     */
    private void sendTeleportMessage(Stargate plugin, Player player) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF(PluginChannel.FORWARD.getChannel());
            dataOutputStream.writeUTF(server);
            dataOutputStream.writeUTF(PluginChannel.PLAYER_TELEPORT.getChannel());
            JsonObject JsonData = new JsonObject();
            JsonData.add(StargateProtocolProperty.PLAYER.toString(), new JsonPrimitive(player.getName()));
            JsonData.add(StargateProtocolProperty.PORTAL.toString(), new JsonPrimitive(this.name));
            JsonData.add(StargateProtocolProperty.NETWORK.toString(), new JsonPrimitive(network.getName()));
            String dataMsg = JsonData.toString();
            dataOutputStream.writeUTF(dataMsg);
            Stargate.log(Level.FINEST, byteArrayOutputStream.toString());
            player.sendPluginMessage(plugin, PluginChannel.BUNGEE.getChannel(), byteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            Stargate.log(Level.WARNING, "[Stargate] Error sending BungeeCord teleport packet");
            throw exception;
        }
    }

    @Override
    public void setOwner(UUID targetPlayer) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
