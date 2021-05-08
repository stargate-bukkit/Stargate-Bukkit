package net.TheDgtl.portal;

import org.bukkit.Location;

public class GateIrisStructure extends GateStructure{
	
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
