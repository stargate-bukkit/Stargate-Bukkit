package net.TheDgtl.Stargate.network.portal;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateProtocol;
import net.TheDgtl.Stargate.network.InterserverNetwork;
import net.TheDgtl.Stargate.network.Network;

/**
 * A virtual portal, which does not exist. Symbolises a portal that is outside
 * this server and acts as an interface to send relevant interserver packets.
 * 
 * @author Thorin
 *
 */
public class VirtualPortal implements IPortal {

	protected String server;
	private String name;
	private Network network;
	private EnumSet<PortalFlag> flags;

	public VirtualPortal(String server, String name, Network net, EnumSet<PortalFlag> flags) {
		this.server = server;
		this.name = name;
		this.network = net;
		this.flags = flags;
		network.addPortal(this,false);
	}

	@Override
	public void teleportHere(Player player) {
		Stargate plugin = JavaPlugin.getPlugin(Stargate.class);
		try {
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DataOutputStream msgData = new DataOutputStream(bao);
            msgData.writeUTF(Channel.FORWARD.getChannel());
            msgData.writeUTF(server);
            msgData.writeUTF(Channel.PLAYER_TELEPORT.getChannel());
			JsonObject data = new JsonObject();
			data.add(StargateProtocol.PLAYER.toString(), new JsonPrimitive(player.getName()));
			data.add(StargateProtocol.PORTAL.toString(), new JsonPrimitive(this.name));
			data.add(StargateProtocol.NETWORK.toString(), new JsonPrimitive(network.getName()));
			String dataMsg = data.toString();
			msgData.writeUTF(dataMsg);
			Stargate.log(Level.FINEST, bao.toString());
			player.sendPluginMessage(plugin, Channel.BUNGEE.getChannel(), bao.toByteArray());
		} catch (IOException ex) {
            Stargate.log(Level.SEVERE,"[Stargate] Error sending BungeeCord teleport packet");
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
             Stargate.log(Level.SEVERE,"[Stargate] Error sending BungeeCord connect packet");
             ex.printStackTrace();
             return;
        }
		
	}

	/**
	 * TODO not implemented
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setNetwork(Network targetNet) {
		this.network = (InterserverNetwork) targetNet;
	}

	/**
	 * TODO Not implemented
	 */
	@Override
	public void setOverrideDesti(IPortal destination) {
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
		network.removePortal(this,false);
	}

	@Override
	public boolean hasFlag(PortalFlag flag) {
		return flags.contains(flag);
	}

	/*
	 * WASTE METHODS WHICH WILL NEVER BE TRIGGERED IN ANY CIRCUMSTANCE
	 */
	@Override
	public void onSignClick(Action action, Player actor) {
	}

	@Override
	public void drawControll() {
	}

	@Override
	public void doTeleport(Player player) {
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isOpenFor(Player player) {
		return false;
	}

	@Override
	public void onButtonClick(Player player) {
	}

	@Override
	public String getAllFlagsString() {
		return "";
	}

	@Override
	public Location getSignPos() {
		return null;
	}
	
}
