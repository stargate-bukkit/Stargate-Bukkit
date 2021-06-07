package net.TheDgtl.Stargate.portal;

import java.util.HashMap;

import org.bukkit.Location;

public class Gate{
	HashMap<String,GateStructure> portalParts;
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	public boolean isInPortal(Location location) {
		return portalParts.get("iris").isInPortal(location);
	}
	
	
}
