package net.TheDgtl.Stargate.portal;

import java.util.ArrayList;
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
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.SyncronousPopulator;
import net.TheDgtl.Stargate.portal.Gate.GateConflict;
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
		DESTINAMESURROUND = new String[] { "<", ">" };
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
	
	
	static public Portal getPortal(Location loc, GateStructure.Type key) {
		return getPortal(new SGLocation(loc),key);
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
	
	public enum PortalFlag {
		RANDOM('R'), BUNGEE('U'), ALWAYSON('A'), BACKWARDS('B'), // Is this used?
		HIDDEN('H'), PRIVATE('P'), SHOW('S'), NONETWORK('N'), // ??
		FREE('F'), NETWORKED('W');

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

	public class Portal {
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
		String[] destinations;
		Player openFor;
		
		// used in networked portals
		static final private int NO_DESTI_SELECTED = -1;
		int selectedDesti = NO_DESTI_SELECTED;
		
		public class NameError extends Exception{
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

		public Portal(Block sign, String[] lines) throws NoFormatFound, GateConflict, NameError {
			/*
			 * Get the block behind the sign; the material of that block is stored in a
			 * register with available gateFormats
			 */
			this.name = lines[0];
			String destiName = lines[1];
			if (name.isBlank())
				throw new NameError("empty");
			if (portalList.containsKey(name)) {
				throw new NameError("taken");
			}
				
			
			Directional signDirection = (Directional) sign.getBlockData();
			Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
			List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
			gate = FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing());

			flags = getFlags(lines[3]);
			if (destiName.isBlank()) {
				flags.add(PortalFlag.NETWORKED);
			} else {
				destinations = new String[] { destiName };
				selectedDesti = 0;
			}

			portalList.put(name, this);
			for(GateStructure.Type key : gate.format.portalParts.keySet()) {
				if(!portalFromPartsMap.containsKey(key)) {
					portalFromPartsMap.put(key, new HashMap<SGLocation, Portal>());
				}
				List<SGLocation> locations = gate.getLocations(key);
				portalFromPartsMap.get(key).putAll(generateLocationHashMap(locations));
			}
			drawControll();
		}
		
		private String getDestination(int index) {
			if (index == NO_DESTI_SELECTED) {
				return "";
			}
			return getDestinations()[index];
		}
		
		private String[] getDestinations(){
			if(flags.contains(PortalFlag.NETWORKED)) {
				return portalList.keySet().toArray(new String[0]);
			}
			return destinations;
		}
		
		public void drawControll() {
			String[] lines = new String[4];
			lines[0] = surroundWith(name, PORTALNAMESURROUND);
			if (!flags.contains(PortalFlag.NETWORKED)) {
				lines[1] = surroundWith(getDestination(selectedDesti), DESTINAMESURROUND);
				lines[2] = surroundWith(netName, NETWORKNAMESURROUND);
				lines[3] = "";
			} else if (this.selectedDesti == NO_DESTI_SELECTED) {
				lines[1] = Stargate.langManager.getString("signRightClick");
				lines[2] = Stargate.langManager.getString("signToUse");
				lines[3] = surroundWith(netName, NETWORKNAMESURROUND);
			} else {
				lines[1] = getDestination(getNextDesti(-1, selectedDesti));
				lines[2] = surroundWith(getDestination(selectedDesti), DESTINAMESURROUND);
				lines[3] = getDestination(getNextDesti(1, selectedDesti));
			}
			gate.drawControll(lines);
		}
		
		private String surroundWith(String target, String[] surrounding) {
			return surrounding[0] + target + surrounding[1];
		}

		private HashMap<SGLocation, Portal> generateLocationHashMap(List<SGLocation> locations) {
			HashMap<SGLocation, Portal> output = new HashMap<>();
			for (SGLocation loc : locations) {
				output.put(loc, this);
			}
			return output;
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
		 * Remove all information stored on this gate
		 */
		public void destroy() {
			portalList.remove(name);
			for(GateStructure.Type formatType : portalFromPartsMap.keySet()) {
				for(SGLocation loc : this.gate.getLocations(formatType)) {
					portalFromPartsMap.get(formatType).remove(loc);
				}
			}
		}

		public void open(Player actor) {
			gate.open();
			this.openFor = actor;

			// Create action which will close this portal
			SyncronousPopulator.Action action = new SyncronousPopulator.Action() {

				@Override
				public void run() {
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
			this.openFor = null;
			if(flags.contains(PortalFlag.NETWORKED)) {
				this.selectedDesti = NO_DESTI_SELECTED;
			}
			gate.close();
			drawControll();
		}

		public boolean isOpen() {
			return gate.isOpen;
		}
		
		public boolean isOpenFor(Player player) {
			// TODO Auto-generated method stub
			return ((player == openFor) || (openFor == null));
		}

		public Portal getDestination() {
			if(selectedDesti == NO_DESTI_SELECTED)
				return null;
			return getPortal(getDestinations()[selectedDesti]);
		}

		public Location getExit() {
			return gate.getExit();
		}
		
		/**
		 * TODO have this individual for each player?
		 * @param action
		 * @param player 
		 */
		public void scrollDesti(Action action, Player actor) {
			Stargate.log(Level.FINEST, "Starting at pos " + selectedDesti);
			if (!(flags.contains(PortalFlag.NETWORKED)) || getDestinations().length < 2)
				return;
			openFor = actor;
			if ((selectedDesti == NO_DESTI_SELECTED) || getDestinations().length < 3) {
				selectedDesti = getNextDesti(1, -1);
			} else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
				int step = (action == Action.RIGHT_CLICK_BLOCK) ? -1 : 1;
				selectedDesti = getNextDesti(step, selectedDesti);
			}

			Stargate.log(Level.FINEST, "Ended with pos " + selectedDesti);
			drawControll();
		}

		private int getNextDesti(int step, int initialDesti) {
			HashSet<Integer> illigalDestis = new HashSet<>();
			return getNextDesti(step,initialDesti,illigalDestis);
		}
		/**
		 * 
		 * @param step
		 * @param initialDesti
		 * @param illigalDestis a list made to avoid infinite recursion
		 * @return
		 */
		private int getNextDesti(int step, int initialDesti, HashSet<Integer> illigalDestis) {
			int maxIndex = getDestinations().length - 1;
			Stargate.log(Level.FINEST, "initial destination: " + initialDesti);
			illigalDestis.add(initialDesti);
			//Avoid infinite recursion if this is the only gate available
			if(maxIndex < 1) {
				return -1;
			}
			int testDesti = initialDesti + step;
			
			if (testDesti < 0)
				testDesti += (maxIndex + 1);
			if (testDesti > maxIndex)
				testDesti -= (maxIndex + 1);
			
			if(illigalDestis.contains(testDesti)) {
				Stargate.log(Level.FINEST, "Illigal destination");
				return -1;
			}
			if (getDestinations()[testDesti] == this.name) {
				return getNextDesti((step > 0) ? 1 : -1, testDesti, illigalDestis);
			}

			return testDesti;
		}
	}
}
