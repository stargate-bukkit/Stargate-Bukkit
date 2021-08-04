package net.TheDgtl.Stargate.network.portal;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Stargate;
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

	private String server;
	private String name;
	private InterserverNetwork network;
	private EnumSet<PortalFlag> flags;

	public VirtualPortal(String server, String name, InterserverNetwork net, EnumSet<PortalFlag> flags) {
		this.server = server;
		this.name = name;
		this.network = net;
		this.flags = flags;
		network.addVirtualPortal(this);
	}

	@Override
	public void teleportHere(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(Channel.PLAYER_TELEPORT.getChannel());
		out.writeUTF(server);
		JsonObject data = new JsonObject();
		data.add("playerName", new JsonPrimitive(player.getName()));
		data.add("portalName", new JsonPrimitive(this.name));
		data.add("network", new JsonPrimitive(network.getName()));
		data.add("isPrivate", new JsonPrimitive(flags.contains(PortalFlag.PERSONAL_NETWORK)));
		String dataMsg = data.toString();
		out.writeShort(dataMsg.length());
		out.writeUTF(dataMsg);
		Stargate plugin = JavaPlugin.getPlugin(Stargate.class);
		player.sendPluginMessage(plugin, Channel.BUNGEE.getChannel(), out.toByteArray());

		ByteArrayDataOutput out1 = ByteStreams.newDataOutput();
		out1.writeUTF(Channel.PLAYER_CONNECT.getChannel());
		out1.writeUTF(server);
		player.sendPluginMessage(plugin, Channel.BUNGEE.getChannel(), out1.toByteArray());
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
		network.removeVirtualPortal(this);
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
