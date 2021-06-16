package net.TheDgtl.Stargate.portal;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class GateStructure {
	/**
	 * 
	 */
	public abstract boolean isInPortal(@NotNull Vector relativeLocation);
	public abstract boolean isValidState();
	public abstract void addPart(Vector vec);
	public void generateBlocks() {
		
	}
}
