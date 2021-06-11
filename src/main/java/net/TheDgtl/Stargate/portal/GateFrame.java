package net.TheDgtl.Stargate.portal;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateFrame extends GateStructure{
	HashMap<Vector,Material> parts;

	@Override
	public boolean isInPortal(@NotNull Vector relativeLocation) {
		return false; // portal is closed
	}

	@Override
	public boolean isValidState() {
		/*
		 * TODO Check if state is valid, for example if the portal is destroyed 
		 */
		
		return false;
	}

	public void addPart(Vector vec, Material mat) {
		parts.put(vec, mat);
	}
}
