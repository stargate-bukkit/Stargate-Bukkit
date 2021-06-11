package net.TheDgtl.Stargate.portal;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateIris extends GateStructure{
	
	Material irisOpen;
	Material irisClosed;
	boolean isOpen = false;
	Vector exit;
	protected List<Vector> blocks;
	
	
	public GateIris(Material irisOpen, Material irisClosed) {
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
