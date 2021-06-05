package net.TheDgtl.Stargate.portal;

import org.bukkit.Location;

public class GateIris extends GateStructure{
	
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
