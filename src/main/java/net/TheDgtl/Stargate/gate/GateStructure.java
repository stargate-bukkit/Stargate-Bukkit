package net.TheDgtl.Stargate.gate;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import net.TheDgtl.Stargate.Stargate;

public abstract class GateStructure {
	/**
	 * Goes through every part of the structure, finds the hypothetical location of
	 * this part by doing a vector operation. Checks if that position is allowed to
	 * have the material it has.
	 * 
	 * @param converter
	 * @param topleft
	 * @return true if all parts had valid materials
	 */
	public boolean isValidState(Gate.VectorOperation converter, Location topleft) {
		List<BlockVector> partsPos = getPartsPos();
		for (BlockVector partPos : partsPos) {
			BlockVector inverse = converter.doInverse(partPos);
			Location partLoc = topleft.clone().add(inverse);
			Stargate.log(Level.FINEST,
					"Checking location (" + partLoc.getBlockX() + "," + partLoc.getBlockY() + "," + partLoc.getBlockZ()
					+") relative pos[" + inverse.getBlockX() + "," + inverse.getBlockY() + "," + inverse.getBlockZ() + "]");
			Block block = partLoc.getBlock();
			if (!isValidBlock(partPos, block.getType())) {
				return false;
			}
		}
		return true;
	}

	public abstract void generateBlocks();

	protected abstract List<BlockVector> getPartsPos();

	protected abstract boolean isValidBlock(BlockVector vec, Material mat);
}
