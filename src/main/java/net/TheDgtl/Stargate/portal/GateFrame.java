package net.TheDgtl.Stargate.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateFrame extends GateStructure{
	HashMap<Vector,HashSet<Material>> parts;

	public GateFrame() {
		parts = new HashMap<>();
	}
	
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
	protected List<Vector> getPartsPos() {
		return new ArrayList<>(parts.keySet());
	}


	@Override
	protected boolean isValidBlock(Vector vec, Material mat) {
		return parts.get(vec).contains(mat);
	}
}
