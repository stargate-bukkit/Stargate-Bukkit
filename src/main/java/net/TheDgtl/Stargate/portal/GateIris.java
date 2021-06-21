package net.TheDgtl.Stargate.portal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateIris extends GateStructure{
	
	HashSet<Material> irisOpen;
	HashSet<Material> irisClosed;
	boolean isOpen = false;
	Vector exit;
	protected List<Vector> blocks;
	
	
	public GateIris(HashSet<Material> irisOpen, HashSet<Material> irisClosed) {
		this.irisOpen = irisOpen;
		this.irisClosed = irisClosed;
	}
	
	public void addPart(Vector blockVector) {
		blocks.add(blockVector);
	}
	
	public void addExit(Vector exitpoint) {
		this.exit = exitpoint;
		addPart(exitpoint);
	}
	
	@Override
	public boolean isInPortal(@NotNull Vector relativeLocation) {
		//TODO write bounds checking algoritm / or use old
		return isOpen;
	}
	
	public void open() {
		//TODO
	}
	
	public void close() {
		//TODO
	}

	@Override
	public void generateBlocks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Set<Vector> getPartsPos() {
		return (Set<Vector>) blocks;
	}

	@Override
	protected boolean isValidBlock(Vector vec, Material mat) {
		return (isOpen ? irisOpen : irisClosed).contains(mat);
	}
}
