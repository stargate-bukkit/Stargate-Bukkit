package net.TheDgtl.portal;

import org.bukkit.Location;

public class OpenedPortal extends GateState{
	
	@Override
	public boolean isInPortal(Location location) {
		//TODO write bounds checking algoritm / or use old
		return false;
	}

	@Override
	public boolean isValidState() {
		// TODO
		return false;
	}
}
