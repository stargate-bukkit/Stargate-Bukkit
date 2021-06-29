package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;
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

	static public Portal getPortal(Location loc, GateStructure.Type[] keys) {
		SGLocation sLoc = new SGLocation(loc);
		Stargate.log(Level.FINEST, "Checking location" + loc.toString());
		for(GateStructure.Type key : keys) {
			if (!(portalFromPartsMap.containsKey(key))) {
				Stargate.log(Level.FINER, "portalFromPartsMap does not contain key " + key);
				continue;
			}
			String debugMsg = "";
			for(SGLocation logLoc : portalFromPartsMap.get(key).keySet()) {
				debugMsg = debugMsg + " " + logLoc.toString();
			}
			Stargate.log(Level.FINEST, debugMsg);
			Portal portal = portalFromPartsMap.get(key).get(sLoc);
			if(portal != null)
				return portal;
		}
		return null;
	}

	public enum PortalFlag {
		RANDOM('R'), BUNGEE('U'), ALWAYSON('A'), BACKWARDS('B'), // Is this used?
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

		Gate gate;
		HashSet<PortalFlag> flags;
		
		public class NoFormatFound extends Exception {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1134125769081020233L;

		}

		public Portal(Block sign, String[] lines) throws NoFormatFound, GateConflict {
			/*
			 * Get the block behind the sign; the material of that block is stored in a
			 * register with available gateFormats
			 */
			String name = lines[0];
			if (name.isBlank())
				throw new NoFormatFound();
			Directional signDirection = (Directional) sign.getBlockData();
			Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
			List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
			gate = FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing());

			flags = getFlags(lines[3]);
			
			// TODO Check perms for flags (remove flags that are not permitted)
			// TODO move drawControll to a seperate function, which does not get triggered
			// in constructor
			lines[0] = PORTALNAMESURROUND[0] + name + PORTALNAMESURROUND[1];
			if (!(lines[1].isBlank())) {
				lines[1] = DESTINAMESURROUND[0] + lines[1] + DESTINAMESURROUND[1];
			}
			lines[2] = NETWORKNAMESURROUND[0] + netName + NETWORKNAMESURROUND[1];
			lines[3] = "";
			gate.drawControll(lines);
			
			portalList.put(name, this);
			for(GateStructure.Type key : gate.format.portalParts.keySet()) {
				if(!portalFromPartsMap.containsKey(key)) {
					portalFromPartsMap.put(key, new HashMap<SGLocation, Portal>());
				}
				List<SGLocation> locations = gate.getLocations(key);
				portalFromPartsMap.get(key).putAll(generateLocationHashMap(locations));
			}
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
	}
}
