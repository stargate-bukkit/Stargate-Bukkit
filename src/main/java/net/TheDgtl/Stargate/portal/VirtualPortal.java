package net.TheDgtl.Stargate.portal;


import java.util.EnumSet;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.StargateData.PlayerDataSender;

/**
 * A virtual portal, which does not exist. Symbolises a portal that is outside
 * this server and acts as an interface to send relevant interserver packets.
 * 
 * @author Thorin
 *
 */
public class VirtualPortal implements IPortal{

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
		PlayerDataSender sender = new PlayerDataSender(server,name,player);
		sender.send();
	}

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

	@Override
	public void setOverrideDesti(IPortal destination) {}

	@Override
	public Network getNetwork() {
		return network;
	}
	
	@Override
	public void open(Player player) {}
	
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
	public void onSignClick(Action action, Player actor) {}

	@Override
	public void drawControll() {}
	
	@Override
	public void doTeleport(Player player) {}

	@Override
	public boolean isOpen() {return false;}

	@Override
	public boolean isOpenFor(Player player) {return false;}
	
	@Override
	public void onButtonClick(Player player) {}

	@Override
	public String getAllFlagsString() {
		return "";
	}

	
}
