package net.TheDgtl.Stargate.portal;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateControll extends GateStructure{
	HashMap<Vector, Material> parts;
	
	
	@Override
	public boolean isInPortal(@NotNull Vector relativeLocation) {
		return false;
	}

	@Override
	public boolean isValidState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPart(Vector vec) {
		
	}

}
