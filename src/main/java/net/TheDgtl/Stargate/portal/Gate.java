package net.TheDgtl.Stargate.portal;

import java.util.HashMap;

import org.bukkit.Location;

public class Gate extends GateLayout{
	GateStructure currentState;
	HashMap<String,GateStructure> availableStates;
	
	/**
	 * @param identifier
	 */
	public void setGateState(String identifier){
		currentState = availableStates.get(identifier);
		
	}
	public boolean isInPortal(Location location) {
		return currentState.isInPortal(location);
	}
}
