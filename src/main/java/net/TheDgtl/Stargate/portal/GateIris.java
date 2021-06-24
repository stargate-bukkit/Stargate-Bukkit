package net.TheDgtl.Stargate.portal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.Stargate;

public class GateIris extends GateStructure{
	
	HashSet<Material> irisOpen;
	HashSet<Material> irisClosed;
	boolean isOpen = false;
	Vector exit;
	protected List<BlockVector> blocks;
	
	
	public GateIris(HashSet<Material> irisOpen, HashSet<Material> irisClosed) {
		this.irisOpen = irisOpen;
		this.irisClosed = irisClosed;
		blocks = new ArrayList<>();
	}
	
	public void addPart(BlockVector blockVector) {
		blocks.add(blockVector);
	}
	
	public void addExit(BlockVector exitpoint) {
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
	protected List<BlockVector> getPartsPos() {
		return blocks;
	}

	@Override
	protected boolean isValidBlock(BlockVector vec, Material mat) {
		return (isOpen ? irisOpen : irisClosed).contains(mat);
	}
}
