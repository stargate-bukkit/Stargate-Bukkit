package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateFrame extends GateStructure{
	HashMap<Vector,HashSet<Material>> parts;

	@Override
	public boolean isInPortal(@NotNull Vector relativeLocation) {
		return false; // portal is closed
	}
	
	
	public void addPart(Vector vec, HashSet<Material> hashSet) {
		parts.put(vec, hashSet);
	}

	@Override
	public void generateBlocks() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected Set<Vector> getPartsPos() {
		return parts.keySet();
	}


	@Override
	protected boolean isValidBlock(Vector vec, Material mat) {
		return parts.get(vec).contains(mat);
	}
}
