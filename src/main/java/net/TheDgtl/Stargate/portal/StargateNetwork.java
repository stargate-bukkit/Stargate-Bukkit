package net.TheDgtl.Stargate.portal;

import java.util.HashMap;

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
	
	public class Portal {
		/**
		 * Behaviours:
		 * - Cycle through PortalStates, make current state listener for movements
		 * - (Static) check if layout is valid, return a layout with relevant info 
		 * about validity, rotation, location and portaltype
		 * - (Constructor) Write sign and do various logic that has not been done with 
		 * layout already
		 * - (Constructor) load from db
		 * 
		 * Added behaviours
		 * - (Listener) Listen for stargate clock (maybe 1 tick per minute or something)
		 * maybe follow an external script that gives when the states should change
		 */
		
		
		GateLayout gate;
		
	}
}
