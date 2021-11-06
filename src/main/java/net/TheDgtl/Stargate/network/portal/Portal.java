package net.TheDgtl.Stargate.network.portal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.InvalidStructure;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFlagFound;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.Network;

/**
 * The parent class for ever portal that interacts with server worlds
 * @author Thorin
 *
 */
public abstract class Portal implements IPortal {
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
	int delay = 20; // seconds
	private Gate gate;
	private EnumSet<PortalFlag> flags;
	String name;
	Player openFor;
	IPortal destination = null;
	private long openTime = -1;
	

	Portal(Network network, String name, Block sign, EnumSet<PortalFlag> flags)
			throws NameError, NoFormatFound, GateConflict {
		this.network = network;
		this.name = name;
		this.flags = flags;
		if (name.isBlank() || (name.length() == Stargate.MAX_TEXT_LENGTH))
			throw new NameError(LangMsg.NAME_LENGTH_FAULT);
		if (this.network.isPortalNameTaken(name)) {
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

		String msg = "Selected with flags ";
		for (PortalFlag flag : flags) {
			msg = msg + flag.label;
		}
		Stargate.log(Level.FINE, msg);
		
		for (GateStructureType key : getGate().getFormat().portalParts.keySet()) {
			List<SGLocation> locations = getGate().getLocations(key);
			network.registerLocations(key, generateLocationHashMap(locations));
		}
		if(hasFlag(PortalFlag.ALWAYS_ON))
			this.open(null);
	}
	
	/**
	 * Look through every stored gateFormat, checks every possible rotation / flip
	 * @param gateFormats
	 * @param signLocation
	 * @param signFacing
	 * @return A gate with matching format
	 * @throws NoFormatFound
	 * @throws GateConflict
	 */
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
	
	private HashMap<SGLocation, IPortal> generateLocationHashMap(List<SGLocation> locations) {
		HashMap<SGLocation, IPortal> output = new HashMap<>();
		for (SGLocation loc : locations) {
			output.put(loc, this);
		}
		return output;
	}
	
	@Override
	public Location getSignPos() {
		return gate.getSignLoc();
	}

	public abstract void onSignClick(Action action, Player actor);

	public abstract void drawControll();

	public abstract IPortal loadDestination();

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
		this.network.removePortal(this);
		String[] lines = new String[] { name, "", "", "" };
		getGate().drawControll(lines, false);
		for (GateStructureType formatType : GateStructureType.values()) {
			for (SGLocation loc : this.getGate().getLocations(formatType)) {
				network.unRegisterLocation(formatType, loc);
			}
		}
		network.updatePortals();
	}

	public void open(Player actor) {
		getGate().open();
		this.openFor = actor;
		if(hasFlag(PortalFlag.ALWAYS_ON)) {
			return;
		}
		long openTime = System.currentTimeMillis();
		this.openTime = openTime;

		
		
		// Create action which will close this portal
		PopulatorAction action = new PopulatorAction() {

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
		new DelayedAction(Stargate.syncSecPopulator, delay, action);
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
		if(hasFlag(PortalFlag.ALWAYS_ON))
			return;
		getGate().close();
		drawControll();
	}
	
	public boolean isOpenFor(Player player) {
		// TODO Auto-generated method stub
		return ((openFor == null) || (player == openFor));
	}

	public Location getExit() {
		return gate.getExit(hasFlag(PortalFlag.BACKWARDS));
	}

	public void setOverrideDesti(IPortal desti) {
		this.destination = desti;
	}

	public Network getNetwork() {
		return this.network;
	}

	public void setNetwork(Network net) {
		this.network = net;
		this.drawControll();
	}

	protected IPortal getFinalDesti() {
		if(destination == null)
			destination = loadDestination();
		return destination;
	}
	
	public void onButtonClick(Player player) {
		IPortal destination = loadDestination();
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
	
	public void teleportHere(Player player) {
		Location exit = getExit();
		player.teleport(exit);
	}
	
	public void doTeleport(Player player) {
		IPortal desti = getFinalDesti();
		if(desti == null) {
			player.sendMessage(Stargate.langManager.getMessage(LangMsg.INVALID, true));
			player.teleport(getExit());
			return;
		}
		desti.teleportHere(player);
		desti.close();
		close();
	}
	
	public static IPortal createPortalFromSign(Network net, String[] lines, Block block, EnumSet<PortalFlag> flags)
			throws NameError, NoFormatFound, GateConflict {
		if(flags.contains(PortalFlag.BUNGEE))
			return new BungeePortal(net,lines[0],lines[1],lines[2],block,flags);
		if (flags.contains(PortalFlag.RANDOM))
			return new RandomPortal(net, lines[0], block, flags);
		if ((lines[1] == null) || lines[1].isBlank())
			return new NetworkedPortal(net, lines[0], block, flags);
		return new FixedPortal(net, lines[0], lines[1], block, flags);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public boolean hasFlag(PortalFlag flag) {
		return flags.contains(flag);
	}
	
	public String getAllFlagsString() {
		String out = "";
		for(PortalFlag flag : flags) {
			out = out + flag.label;
		}
		return out;
	}
}