package net.TheDgtl.Stargate.portal;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class GateFrame extends GateStructure{

	@Override
	public boolean isInPortal(@NotNull Location loc) {
		return false; // portal is closed
	}

	@Override
	public boolean isValidState() {
		/*
		 * TODO Check if state is valid, for example if the portal is destroyed 
		 */
		
		return false;
	}


}
