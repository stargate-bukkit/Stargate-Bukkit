package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class Network{
	/*
	 * Conceived as the class that can store a network of portals inside itself
	 * Portals inside this class can only communicate with each other
	 * 
	 * pros: 
	 *  - Makes searching algorithms somewhat easier/ less CPU intensive
	 *  - Segments code?
	 *  
	 *  Behaviours:
	 *  - Load network (each network might as well have an individual db)
	 *  - Get portal by name
	 */
	private HashMap<String,Portal> portalList;
	
	public Portal getPortal(String name){
		return portalList.get(name);
	}
	
	
	
	public class Portal {
		/**
		 * Behaviours:
		 * - Cycle through PortalStates, make current state listener for movements
		 * - (Constructor) Check validity, write sign, add self to a list in the network
		 * 
		 * Added behaviours
		 * - (Listener) Listen for stargate clock (maybe 1 tick per minute or something)
		 * maybe follow an external script that gives when the states should change
		 */
		
		
		Gate gate;
		
		public class NoFormatFound extends Exception{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1134125769081020233L;
			
		}
		
		public Portal(Block sign, String[] config) throws NoFormatFound {
			/* 
			 * Get the block behind the sign; the material of that block is stored in a
			 * register with available gateFormats
			 */
			Directional signDirection = (Directional) sign.getBlockData();
			Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
			List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
			
			gate = FindMatchingGate(gateFormats,sign.getLocation(),signDirection.getFacing());
		}
		
		private Gate FindMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing) throws NoFormatFound {
			Gate outputGate = null;
			for (GateFormat gateFormat : gateFormats) {
				try {
					outputGate = new Gate(gateFormat, signLocation, signFacing);
					return outputGate;
				} catch(Gate.InvalidStructure e) {}
			}
			throw new NoFormatFound();
		}
	}
}
