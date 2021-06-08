package net.TheDgtl.Stargate.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

public class Gate{
	public static List<Gate> gateTypes;
	
	HashMap<String,GateStructure> portalParts;
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	public boolean isInPortal(Location location) {
		return portalParts.get("iris").isInPortal(location);
	}
	
	public static List<Gate> loadGates(){
		List<Gate> allGateTypes = new ArrayList<Gate>();
		
		
		
		return allGateTypes;
	}
}
