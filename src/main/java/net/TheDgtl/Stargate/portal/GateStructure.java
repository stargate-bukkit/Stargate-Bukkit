package net.TheDgtl.Stargate.portal;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class GateStructure {
	private List<Vector> blocks;
	/**
	 * 
	 */
	public abstract boolean isInPortal(@NotNull Location loc);
	public abstract boolean isValidState();
	public void generateBlocks() {
		
	}
}
