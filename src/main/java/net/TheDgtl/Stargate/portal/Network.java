package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.BlockVector;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.SyncronousPopulator;
import net.TheDgtl.Stargate.portal.Gate.GateConflict;
import net.TheDgtl.Stargate.portal.GateStructure.Type;
import net.TheDgtl.Stargate.portal.Network.PortalFlag.NoFlagFound;

public class Network {
	/*
	 * Conceived as the class that can store a network of portals inside itself
	 * Portals inside this class can only communicate with each other
	 * 
	 * pros: - Makes searching algorithms somewhat easier/ less CPU intensive -
	 * Segments code?
	 * 
	 * Behaviours: - Load network (each network might as well have an individual db)
	 * - Get portal by name
	 */
	private HashMap<String, Portal> portalList;
	public static final HashMap<String, Network> networkList = new HashMap<>();
	private String netName;

	public static final String DEFAULTNET = "central";
	static final String[] PORTALNAMESURROUND;
	static final String[] DESTINAMESURROUND;
	static final String[] NETWORKNAMESURROUND;
	static {
		PORTALNAMESURROUND = new String[] { "-", "-" };
		DESTINAMESURROUND = new String[] { ">", "<" };
		NETWORKNAMESURROUND = new String[] { "(", ")" };
	}

	final static HashMap<GateStructure.Type, HashMap<SGLocation, Portal>> portalFromPartsMap = new HashMap<>();

	public Network(String netName) {
		this.netName = netName;
		portalList = new HashMap<>();
	}

	public Portal getPortal(String name) {
		return portalList.get(name);
	}
	
	public void addPortal(Portal portal) {
		this.portalList.put(portal.name, portal);
	}
	
	static public Portal getPortal(Location loc, GateStructure.Type key) {
		return getPortal(new SGLocation(loc),key);
	}
	
	public static Portal getPortal(Location loc, Type[] keys) {
		
		return getPortal(new SGLocation(loc), keys);
	}
	
	/**
	 * Get a portal from location and the type of gateStructure targeted
	 * @param loc
	 * @param key
	 * @return
	 */
	static public Portal getPortal(SGLocation loc, GateStructure.Type key) {
		if (!(portalFromPartsMap.containsKey(key))) {
			Stargate.log(Level.FINER, "portalFromPartsMap does not contain key " + key);
			return null;
		}
		Portal portal = portalFromPartsMap.get(key).get(loc);
		return portal;
	}
	
	/**
	 * Get a portal from location and the types of gateStructures targeted
	 * @param loc
	 * @param keys
	 * @return
	 */
	static public Portal getPortal(SGLocation loc, GateStructure.Type[] keys) {
		for(GateStructure.Type key : keys) {
			Portal portal = getPortal(loc, key);
			if(portal != null)
				return portal;
		}
		return null;
	}
	
	public static boolean isInPortal(List<Block> blocks, GateStructure.Type[] keys) {
		for (Block block : blocks) {
			if (getPortal(block.getLocation(), GateStructure.Type.values()) != null) {
				return true;
			}
		}
		return false;
	}
	
	//Adjacent
	public static boolean isNextToPortal(Location loc, GateStructure.Type key) {
		BlockVector adjacentVec = new BlockVector(1,0,0);
		for(int i = 0; i < 4; i++) {
			Location adjacentLoc = loc.clone().add(adjacentVec);
			if(getPortal(adjacentLoc, key) != null) {
				return true;
			}
			adjacentVec.rotateAroundY(Math.PI/2);
		}
		return false;
	}

	public static Network getNetwork(String networkName) {
		return networkList.get(networkName);
	}
	
	public enum PortalFlag {
		RANDOM('R'), BUNGEE('U'), ALWAYSON('A'), BACKWARDS('B'),
		HIDDEN('H'), PRIVATE('P'), SHOW('S'), NONETWORK('N'), // ??
		FREE('F');

		public final char label;

		private PortalFlag(char label) {
			this.label = label;
		}

		public static PortalFlag valueOf(char label) throws NoFlagFound {
			for (PortalFlag flag : values()) {
				if (flag.label == label) {
					return flag;
				}
			}
			throw new NoFlagFound();
		}

		static public class NoFlagFound extends Exception {
		}
	}

	public abstract class Portal{
		/**
		 * Behaviours: - Cycle through PortalStates, make current state listener for
		 * movements - (Constructor) Check validity, write sign, add self to a list in
		 * the network
		 * 
		 * Added behaviours - (Listener) Listen for stargate clock (maybe 1 tick per
		 * minute or something) maybe follow an external script that gives when the
		 * states should change
		 */
		int delay = 20*20; // ticks
		Gate gate;
		HashSet<PortalFlag> flags;
		String name;
		Player openFor;
		
		Portal(Block sign, String[] lines) throws NameError, NoFormatFound, GateConflict{
			
			this.name = lines[0];
			if (name.isBlank())
				throw new NameError("empty");
			if (portalList.containsKey(name)) {
				throw new NameError("taken");
			}
			
			/*
			 * Get the block behind the sign; the material of that block is stored in a
			 * register with available gateFormats
			 */
			Directional signDirection = (Directional) sign.getBlockData();
			Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
			List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
			gate = FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing());

			flags = getFlags(lines[3]);
			String msg = "Selected with flags ";
			for(PortalFlag flag : flags) {
				msg = msg + flag.label;
			}
			addPortal(this);
			Stargate.log(Level.FINE, msg);
			for(GateStructure.Type key : gate.format.portalParts.keySet()) {
				if(!portalFromPartsMap.containsKey(key)) {
					portalFromPartsMap.put(key, new HashMap<SGLocation, Portal>());
				}
				List<SGLocation> locations = gate.getLocations(key);
				portalFromPartsMap.get(key).putAll(generateLocationHashMap(locations));
			}
		}
		
		private Gate FindMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing)
				throws NoFormatFound, GateConflict {
			Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
			for (GateFormat gateFormat : gateFormats) {
				Stargate.log(Level.FINE, "--------- " + gateFormat.name + " ---------");
				try {
					return new Gate(gateFormat, signLocation, signFacing);
				} catch (Gate.InvalidStructure e) {
				}
			}
			throw new NoFormatFound();
		}
		
		/**
		 * Go through every character in line, and
		 * 
		 * @param line
		 */
		private HashSet<PortalFlag> getFlags(String line) {
			HashSet<PortalFlag> foundFlags = new HashSet<>();
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
		
		public abstract Portal getDestination();
		
		public boolean isOpen() {
			return gate.isOpen;
		}
		
		/**
		 * Remove all information stored on this gate
		 */
		public void destroy() {
			portalList.remove(name);
			String[] lines = new String[] {name,"","",""};
			gate.drawControll(lines);
			for(GateStructure.Type formatType : portalFromPartsMap.keySet()) {
				for(SGLocation loc : this.gate.getLocations(formatType)) {
					portalFromPartsMap.get(formatType).remove(loc);
				}
			}
			
			// Refresh all portals in this network. TODO is this too extensive?
			for (String portal : portalList.keySet()) {
				portalList.get(portal).drawControll();
			}
		}
		
		public void open(Player actor) {
			gate.open();
			this.openFor = actor;

			// Create action which will close this portal
			SyncronousPopulator.Action action = new SyncronousPopulator.Action() {

				@Override
				public void run(boolean forceEnd) {
					close();
				}

				@Override
				public boolean isFinished() {
					return true;
				}
			};
			// Make the action on a delay
			Stargate.syncPopulator.new DelayedAction(delay, action);
		}
		
		public void close() {
			gate.close();
			drawControll();
		}
		
		public boolean isOpenFor(Player player) {
			// TODO Auto-generated method stub
			return ((player == openFor) || (openFor == null));
		}
		
		public Location getExit() {
			return gate.getExit();
		}
		
		/**
		 * Surrounds one string with two strings
		 * @param target
		 * @param surrounding
		 * @return
		 */
		protected String surroundWith(String target, String[] surrounding) {
			return surrounding[0] + target + surrounding[1];
		}
		
		public class NameError extends Exception{
			/**
			 * 
			 */
			private static final long serialVersionUID = -9187508162071170232L;

			public NameError(String msg) {
				super(msg);
			}
		}
		
		public class NoFormatFound extends Exception {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1134125769081020233L;

		}
	}
	
	public class FixedPortal extends Portal{
		String destination;

		public FixedPortal(Block sign, String[] lines) throws NoFormatFound, GateConflict, NameError {
			super(sign,lines);
			destination = lines[1];
			
			drawControll();
		}
		
		
		
		/**
		 * What will happen when a player clicks the sign?
		 * @param action
		 * @param player 
		 */
		@Override
		public void onSignClick(Action action, Player actor) {}

		@Override
		public void drawControll() {
			String[] lines = new String[4];
			lines[0] = surroundWith(name, PORTALNAMESURROUND);
			lines[1] = surroundWith(destination, DESTINAMESURROUND);
			lines[2] = surroundWith(netName, NETWORKNAMESURROUND);
			lines[3] = (portalList.containsKey(destination)) ? ""
						: Stargate.langManager.getString("signDisconnected");
			gate.drawControll(lines);
		}
		
		@Override
		public Portal getDestination() {
			return getPortal(destination);
		}
		
		@Override
		public void close() {
			super.close();
			this.openFor = null;
		}
	}

	public class NetworkedPortal extends Portal {
		// used in networked portals
		static final private int NO_DESTI_SELECTED = -1;
		private int selectedDesti = NO_DESTI_SELECTED;
		
		public NetworkedPortal(Block sign, String[] lines) throws NoFormatFound, GateConflict, NameError {
			super(sign, lines);
			
			drawControll();
		}
		
		/**
		 * TODO have this individual for each player?
		 * @param action
		 * @param player 
		 */
		@Override
		public void onSignClick(Action action, Player actor) {
			if (getDestinations().length < 1)
				return;
			openFor = actor;
			if ((selectedDesti == NO_DESTI_SELECTED) || getDestinations().length < 2) {
				selectedDesti = getNextDesti(1, -1);
			} else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
				int step = (action == Action.RIGHT_CLICK_BLOCK) ? -1 : 1;
				selectedDesti = getNextDesti(step, selectedDesti);
			}
			drawControll();
		}
		
		private String getDestination(int index) {
			if (index == NO_DESTI_SELECTED) {
				return "";
			}
			return getDestinations()[index];
		}
		
		private String[] getDestinations() {
			HashSet<String> tempPortalList = new HashSet<>(portalList.keySet());
			tempPortalList.remove(name);
			return tempPortalList.toArray(new String[0]);
		}
		
		@Override
		public Portal getDestination() {
			if(selectedDesti == NO_DESTI_SELECTED)
				return null;
			return getPortal(getDestinations()[selectedDesti]);
		}
		/**
		 * A method which allows selecting a index x steps away from a reference index
		 * without having to bother with index out of bounds stuff. If the index is out
		 * of bounds, it will just start counting from 0
		 * @param step
		 * @param initialDesti
		 * @return
		 */
		private int getNextDesti(int step, int initialDesti) {
			int destiLength = getDestinations().length;
			// Avoid infinite recursion if this is the only gate available
			if (destiLength < 1) {
				return -1;
			}
			int temp = initialDesti + destiLength;
			return (temp + step) % destiLength;
		}
		
		@Override
		public void close() {
			this.selectedDesti = NO_DESTI_SELECTED;
			super.close();
		}
		
		@Override
		public void drawControll() {
			String[] lines = new String[4];
			lines[0] = surroundWith(name, PORTALNAMESURROUND);
			if (this.selectedDesti == NO_DESTI_SELECTED) {
				lines[1] = Stargate.langManager.getString("signRightClick");
				lines[2] = Stargate.langManager.getString("signToUse");
				lines[3] = surroundWith(netName, NETWORKNAMESURROUND);
			} else {
				int destiIndex = selectedDesti % 3;
				int desti1 = selectedDesti - destiIndex;
				int maxLength = getDestinations().length;
				for (int i = 0; i < 3; i++) {
					int desti = i + desti1;
					if(desti == maxLength)
						break;
					String name = getDestination(desti);
					if (destiIndex == i) {
						name = surroundWith(name, DESTINAMESURROUND);
					}
					lines[i + 1] = name;
				}
			}
			gate.drawControll(lines);
		}
	}

	
}
