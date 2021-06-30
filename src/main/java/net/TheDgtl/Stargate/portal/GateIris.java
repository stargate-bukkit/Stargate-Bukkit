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
	
	public final HashSet<Material> irisOpen;
	public final HashSet<Material> irisClosed;
	BlockVector exit;
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
		this.exit = exitpoint.clone();
		exit.add(new BlockVector(1,0,0));
		addPart(exitpoint);
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
		return irisClosed.contains(mat);
	}
	
	public Material getMat(boolean isOpen) {
		//TODO a bit lazy
		return (isOpen?irisOpen : irisClosed).iterator().next();
	}

	public BlockVector getExit() {
		return exit;
	}
}
