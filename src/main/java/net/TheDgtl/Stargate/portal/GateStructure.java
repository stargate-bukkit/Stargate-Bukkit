package net.TheDgtl.Stargate.portal;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class GateStructure {
	/**
	 * 
	 */
	public abstract boolean isInPortal(@NotNull Location loc);
	public abstract boolean isValidState();
	public void generateBlocks() {
		
	}
	 
}
