package net.TheDgtl.Stargate.portal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.SyncronousPopulator;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.InvalidStructure;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.portal.PortalFlag.NoFlagFound;

public abstract class Portal {
	/**
	 * 
	 */
	protected Network network;
	/**
	 * Behaviours: - Cycle through PortalStates, make current state listener for
	 * movements - (Constructor) Check validity, write sign, add self to a list in
	 * the network
	 * 
	 * Added behaviours - (Listener) Listen for stargate clock (maybe 1 tick per
	 * minute or something) maybe follow an external script that gives when the
	 * states should change
	 */
	int delay = 20 * 20; // ticks
	private Gate gate;
	EnumSet<PortalFlag> flags;
	String name;
	Player openFor;
	Portal destination = null;
	private long openTime = -1;
	Portal(Network network, Block sign, String[] lines) throws NameError, NoFormatFound, GateConflict {

		this.network = network;
		this.name = lines[0];
		if (name.isBlank())
			throw new NameError(LangMsg.NAME_LENGTH_FAULT);
		if (this.network.portalList.containsKey(name)) {
			throw new NameError(LangMsg.ALREADY_EXIST);
		}

		/*
		 * Get the block behind the sign; the material of that block is stored in a
		 * register with available gateFormats
		 */
		Directional signDirection = (Directional) sign.getBlockData();
		Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
		List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
		setGate(FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing()));

		flags = parseFlags(lines[3]);
		String msg = "Selected with flags ";
		for (PortalFlag flag : flags) {
			msg = msg + flag.label;
		}
		Stargate.log(Level.FINE, msg);
		
		this.network.addPortal(this);
		for (String portal : this.network.portalList.keySet()) {
			this.network.portalList.get(portal).drawControll();
		}
		for (GateStructureType key : getGate().getFormat().portalParts.keySet()) {
			if (!Network.portalFromPartsMap.containsKey(key)) {
				Network.portalFromPartsMap.put(key, new HashMap<SGLocation, Portal>());
			}
			List<SGLocation> locations = getGate().getLocations(key);
			Network.portalFromPartsMap.get(key).putAll(generateLocationHashMap(locations));
		}
		if(flags.contains(PortalFlag.ALWAYS_ON))
			this.open(null);
	}

	private Gate FindMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing)
			throws NoFormatFound, GateConflict {
		Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
		for (GateFormat gateFormat : gateFormats) {
			Stargate.log(Level.FINE, "--------- " + gateFormat.name + " ---------");
			try {
				return new Gate(gateFormat, signLocation, signFacing);
			} catch (InvalidStructure e) {
			}
		}
		throw new NoFormatFound();
	}

	/**
	 * Go through every character in line, and
	 * 
	 * @param line
	 */
	private EnumSet<PortalFlag> parseFlags(String line) {
		EnumSet<PortalFlag> foundFlags = EnumSet.noneOf(PortalFlag.class);
		char[] charArray = line.toUpperCase().toCharArray();
		for (char character : charArray) {
			try {
				foundFlags.add(PortalFlag.valueOf(character));
			} catch (NoFlagFound e) {
			}
		}
		return foundFlags;
	}

	private HashMap<SGLocation, Portal> generateLocationHashMap(List<SGLocation> locations) {
		HashMap<SGLocation, Portal> output = new HashMap<>();
		for (SGLocation loc : locations) {
			output.put(loc, this);
		}
		return output;
	}

	public abstract void onSignClick(Action action, Player actor);

	public abstract void drawControll();

	public abstract Portal loadDestination();

	public boolean isOpen() {
		return getGate().isOpen();
	}

	public EnumSet<PortalFlag> getFlags() {
		return flags;
	}

	/**
	 * Remove all information stored on this gate
	 */
	public void destroy() {
		this.network.portalList.remove(name);
		String[] lines = new String[] { name, "", "", "" };
		getGate().drawControll(lines, false);
		for (GateStructureType formatType : Network.portalFromPartsMap.keySet()) {
			for (SGLocation loc : this.getGate().getLocations(formatType)) {
				Network.portalFromPartsMap.get(formatType).remove(loc);
			}
		}

		// Refresh all portals in this network. TODO is this too extensive?
		for (String portal : this.network.portalList.keySet()) {
			this.network.portalList.get(portal).drawControll();
		}
	}

	public void open(Player actor) {
		getGate().open();
		this.openFor = actor;
		long openTime = System.currentTimeMillis();
		this.openTime = openTime;

		if(flags.contains(PortalFlag.ALWAYS_ON)) {
			return;
		}
		
		// Create action which will close this portal
		SyncronousPopulator.Action action = new SyncronousPopulator.Action() {

			@Override
			public void run(boolean forceEnd) {
				close(openTime);
			}

			@Override
			public boolean isFinished() {
				return true;
			}
		};
		// Make the action on a delay
		Stargate.syncPopulator.new DelayedAction(delay, action);
	}

	/**
	 * Everytime most of the portals opens, there is going to be a scheduled event
	 * to close it after a specific time. If a player enters the portal before this,
	 * then it is going to close, but the scheduled close event is still going to be
	 * there. And if the portal gets activated again, it is going to close
	 * prematurely, because of this already scheduled event. Solution to avoid this
	 * is to assign a opentime for each scheduled close event and only close if the
	 * related open time matches with the most recent time the portal was closed.
	 * 
	 * @param relatedOpenTime
	 */
	public void close(long relatedOpenTime) {
		if (relatedOpenTime == openTime)
			close();
	}

	public void close() {
		if(flags.contains(PortalFlag.ALWAYS_ON))
			return;
		getGate().close();
		drawControll();
	}

	protected boolean isHidden() {
		return flags.contains(PortalFlag.HIDDEN);
	}
	
	public boolean isOpenFor(Player player) {
		// TODO Auto-generated method stub
		return ((openFor == null) || (player == openFor));
	}

	public Location getExit() {
		return gate.getExit(flags.contains(PortalFlag.BACKWARDS));
	}

	public void setOverrideDesti(Portal desti) {
		this.destination = desti;
	}

	public Network getNetwork() {
		return this.network;
	}

	public void setNetwork(Network net) {
		this.network = net;
		this.drawControll();
	}

	protected Portal getFinalDesti() {
		if(destination == null)
			destination = loadDestination();
		return destination;
	}
	
	public void onButtonClick(Player player) {
		Portal destination = loadDestination();
		if (destination == null) {
			player.sendMessage(Stargate.langManager.getMessage(LangMsg.INVALID, true));
			return;
		}
		// TODO checkPerms
		this.destination = destination;
		open(player);
		destination.open(player);
	}

	public Gate getGate() {
		return gate;
	}

	public void setGate(Gate gate) {
		this.gate = gate;
	}
	
	public void teleportToExit(Player player) {
		Location exit = getExit();
		player.teleport(exit);
	}
	
	public void doTeleport(Player player) {
		Portal desti = getFinalDesti();
		if(desti == null) {
			player.sendMessage(Stargate.langManager.getMessage(LangMsg.INVALID, true));
			player.teleport(getExit());
			return;
		}
		desti.teleportToExit(player);
		desti.close();
		close();
	}
	
	public static Portal createPortalFromSign(Network net, Block block, String[] lines)
			throws NameError, NoFormatFound, GateConflict {
		if (lines[3].toUpperCase().contains("R"))
			return new RandomPortal(net, block, lines);
		if (lines[1].isBlank())
			return new NetworkedPortal(net, block, lines);
		return new FixedPortal(net, block, lines);
	}
	
}