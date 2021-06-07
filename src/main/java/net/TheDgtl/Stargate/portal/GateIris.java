package net.TheDgtl.Stargate.portal;

import org.bukkit.Location;
import org.bukkit.Material;

public class GateIris extends GateStructure{
	
	Material openBlock;
	Material closedBlock;
	boolean isOpen = false;
	
	@Override
	public boolean isInPortal(Location location) {
		//TODO write bounds checking algoritm / or use old
		return isOpen;
	}

	@Override
	public boolean isValidState() {
		// TODO
		return false;
	}
	
	public void open() {
		//TODO
	}
	
	public void close() {
		//TODO
	}
}
