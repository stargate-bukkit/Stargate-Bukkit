package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateControll extends GateStructure{
	HashSet<Vector> parts;
	
	public GateControll() {
		parts = new HashSet<>();
	}
	
	
	@Override
	public boolean isInPortal(@NotNull Vector relativeLocation) {
		return false;
	}
	
	public void addPart(Vector vec) {
		parts.add(vec);
	}


	@Override
	public void generateBlocks() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected Set<Vector> getPartsPos() {
		return parts;
	}


	@Override
	protected boolean isValidBlock(Vector vec, Material mat) {
		//TODO maybe add some fancy detection here
		return true;
	}
}
