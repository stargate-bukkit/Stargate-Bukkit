package net.TheDgtl.Stargate.portal;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.Stargate;

public abstract class GateStructure {
	/**
	 * 
	 */
	public abstract boolean isInPortal(@NotNull Vector relativeLocation);

	public boolean isValidState(Gate.VectorOperation converter, Location topleft) {
		List<Vector> partsPos = getPartsPos();
		for (Vector partPos : partsPos) {
			Location partLoc = topleft.add(converter.doInverse(partPos));
			Stargate.log(Level.FINEST, "Checking location"+ partLoc.getBlockX()
			+ "," + partLoc.getBlockY() + "," + partLoc.getBlockZ());
			Block block = partLoc.getBlock();
			if (!isValidBlock(partPos, block.getType())) {
				return false;
			}
		}

		return true;
	}

	public abstract void generateBlocks();

	protected abstract List<Vector> getPartsPos();

	protected abstract boolean isValidBlock(Vector vec, Material mat);
}
