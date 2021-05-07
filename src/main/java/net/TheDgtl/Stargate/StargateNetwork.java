package net.TheDgtl.Stargate;

import org.bukkit.event.Listener;

public class StargateNetwork implements Listener{
	/**
	 * Conceived as the class that can store a network of portals inside itself
	 * Portals inside this class can only communicate with each other
	 * 
	 * cons: 
	 *  - makes crossnetwork portals harder to create.
	 * pros: 
	 *  - Makes searching algorithms somewhat easier/ less CPU intensive
	 *  - Segments code?
	 *  
	 *  Behaviours:
	 *  - Load network (each network might as well have an individual db)
	 *  - Get portal by name
	 *  - (listener) createPortalEvent; add portal if it is for this network
	 *  - (listener) destroyPortalEvent; remove portal if in this network
	 */
}
