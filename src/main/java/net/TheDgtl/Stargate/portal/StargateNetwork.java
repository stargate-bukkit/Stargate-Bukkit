package net.TheDgtl.Stargate.portal;

import java.util.HashMap;

import org.bukkit.Location;

public class StargateNetwork{
	/**
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
	
	public class InvalidPortalStructure extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5580284561192990683L;
		
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
		
		
		GateLayout gate;
		
		public Portal(Location signLoc, String[] config) throws InvalidPortalStructure{
			// Check validity, write sign, add self to a list in the network
		}
	}
}
