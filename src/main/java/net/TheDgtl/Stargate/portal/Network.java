package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.portal.Network.Flags.InvalidLabel;

public class Network{
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

	public Portal getPortal(String name) {
		return portalList.get(name);
	}
	
	public enum Flags{
		RANDOM('R'),
		BUNGEE('U'),
		ALWAYSON('A'),
		BACKWARDS('B'), //Is this used?
		HIDDEN('H'),
		PRIVATE('P'),
		SHOW('S'),
		NONETWORK('N'), // ??
		FREE('F');
		
		public final char label;
		private Flags(char label) {
			this.label = label;
		}
		
		public static Flags valueOf(char label) throws InvalidLabel {
		    for (Flags e : values()) {
		        if (e.label == label) {
		            return e;
		        }
		    }
		    throw new InvalidLabel();
		}
		static public class InvalidLabel extends Exception{}
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
		HashSet<Flags> flags;
		
		
		public class NoFormatFound extends Exception {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1134125769081020233L;

		}

		public Portal(Block sign, String[] lines) throws NoFormatFound {
			/*
			 * Get the block behind the sign; the material of that block is stored in a
			 * register with available gateFormats
			 */
			Directional signDirection = (Directional) sign.getBlockData();
			Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
			List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
			gate = FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing());
			
			flags = new HashSet<>();
			setFlagsFromLine(lines[3]);
			
			portalList.put(lines[0], this);
		}
		/**
		 * Go through every 
		 * @param line
		 */
		private void setFlagsFromLine(String line) {
			char[] charArray = line.toUpperCase().toCharArray();
			for(char character : charArray) {
				try {
					flags.add(Flags.valueOf(character));
				} catch (InvalidLabel e) {}
			}
		}

		private Gate FindMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing)
				throws NoFormatFound {
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
