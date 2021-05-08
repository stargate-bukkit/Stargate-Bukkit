package net.TheDgtl.Stargate.portal;

import java.util.HashMap;

import org.bukkit.Location;

public class Gate {
	/**
	 * Behaviors:
	 * - (Static) a method that loads all existing portalTypes from file and saves
	 * and saves it here as static
	 * - (Constructor) Load portaltype from .gate file
	 * - Check if a possition of this portaltype at specified pos is valid
	 * - have function that returns portalstates?
	 */
	
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
