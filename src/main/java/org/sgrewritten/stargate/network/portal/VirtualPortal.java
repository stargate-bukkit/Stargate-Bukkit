package org.sgrewritten.stargate.network.portal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.proxy.BukkitPluginMessageInterface;
import org.sgrewritten.stargate.network.proxy.PluginMessageInterface;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.util.BungeeHelper;
import org.sgrewritten.stargate.util.ExceptionHelper;
import org.sgrewritten.stargate.util.NameHelper;

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
    private final Set<Character> unrecognisedFlags;
    private final PluginMessageInterface pluginMessageInterface;
    private String name;
    private Network network;
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
    public VirtualPortal(String server, String name, Network network, Set<PortalFlag> flags, Set<Character> unrecognisedFlags, UUID ownerUUID) {
        this.server = server;
        this.name = name;
        this.network = network;
        this.flags = flags;
        this.unrecognisedFlags = unrecognisedFlags;
        this.ownerUUID = ownerUUID;
        this.pluginMessageInterface = new BukkitPluginMessageInterface();
    }

    @Override
    public void teleportHere(Entity target, RealPortal origin) {
        //TODO: implement vehicle compatibility.
        Stargate plugin = JavaPlugin.getPlugin(Stargate.class);
        if (!(target instanceof Player player)) {
            return;
        }

        try {
            sendTeleportMessage(plugin, player);
            sendConnectMessage(plugin, player);
        } catch (IOException e) {
            Stargate.log(e);
        }
    }

    @Override
    public void close(boolean force) {
        // Nothing is currently done cross server, small actions like this seem unnecessary
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
        // Nothing is currently done cross server, small actions like this seem unnecessary
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void open(Player player) {
        // Nothing is currently done cross server, small actions like this seem unnecessary
    }

    @Override
    public void destroy() {
        network.removePortal(this);
    }

    @Override
    public boolean hasFlag(PortalFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public boolean hasFlag(char flag) {
        return unrecognisedFlags.contains(flag) || (ExceptionHelper.doesNotThrow(() -> PortalFlag.valueOf(flag)) && flags.contains(PortalFlag.valueOf(flag)));
    }

    @Override
    public void addFlag(Character flag) {
        // Nothing is currently done cross server, small actions like this seem unnecessary
    }

    @Override
    public void removeFlag(Character flag) {
        // Nothing is currently done cross server, small actions like this seem unnecessary
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
    public void updateState() {
        // Nothing is currently done cross server, small actions like this seem unnecessary
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
            String dataMsg = BungeeHelper.generateTeleportJsonMessage(player.getName(), this);
            pluginMessageInterface.sendDirectedMessage(dataMsg, PluginChannel.PLAYER_TELEPORT, plugin, server);
        } catch (IOException exception) {
            Stargate.log(Level.WARNING, "Error sending BungeeCord teleport packet");
            throw exception;
        }
    }

    @Override
    public void setOwner(UUID targetPlayer) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getId() {
        return NameHelper.getNormalizedName(name);
    }

    @Override
    public GlobalPortalId getGlobalId() {
        return GlobalPortalId.getFromPortal(this);
    }

    public String getServer() {
        return server;
    }

    public StorageType getStorageType() {
        return (flags.contains(PortalFlag.FANCY_INTER_SERVER) ? StorageType.INTER_SERVER : StorageType.LOCAL);
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
