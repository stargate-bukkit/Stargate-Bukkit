package net.TheDgtl.Stargate.portal;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class GateStructure {
	/**
	 * 
	 */
	public abstract boolean isInPortal(@NotNull Vector relativeLocation);

	public boolean isValidState(Gate.VectorOperation converter) {
		Set<Vector> partsPos = getPartsPos();
		for (Vector partPos : partsPos) {
			Block block = converter.doInverse(partPos).getBlock();
			if (!isValidBlock(partPos, block.getType())) {
				return false;
			}
		}

		return true;
	}

	public abstract void generateBlocks();

	protected abstract Set<Vector> getPartsPos();

	protected abstract boolean isValidBlock(Vector vec, Material mat);
}
