package org.sgrewritten.stargate.gate.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;

import java.util.List;
import java.util.logging.Level;

/**
 * Represents one of the structures used in a gate
 */
public abstract class GateStructure {

    /**
     * Checks if all the blocks part of this structure matches a built structure
     *
     * <p>Goes through every part of the structure, finds the hypothetical location of
     * this part by doing a vector operation. Checks if that position is allowed to
     * have the material it has.</p>
     *
     * @param vectorOperation <p>The vector operation to use for rotating the structure</p>
     * @param topLeft         <p>The location of the built portal's top-left block</p>
     * @return true if all parts had valid materials
     */
    public boolean isValidState(VectorOperation vectorOperation, Location topLeft) {
        List<BlockVector> partsPos = getStructureTypePositions();
        World world = topLeft.getWorld();
        if (world == null) {
            Stargate.log(Level.WARNING, "Unable to find the world of the portal at " + topLeft);
            return false;
        }
        WorldBorder border = world.getWorldBorder();
        for (BlockVector partPos : partsPos) {
            BlockVector inverse = vectorOperation.performToRealSpaceOperation(partPos);
            Location partLoc = topLeft.clone().add(inverse);
            Stargate.log(Level.FINEST,
                    "Checking location (" + partLoc.getBlockX() + "," + partLoc.getBlockY() + "," + partLoc.getBlockZ()
                            + ") relative pos[" + inverse.getBlockX() + "," + inverse.getBlockY() + "," + inverse.getBlockZ() + "]");
            Block block = partLoc.getBlock();

            if (!border.isInside(partLoc)) {
                return false;
            }

            if (!isValidBlock(partPos, block.getType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all positions, represented by block vectors, where this structure type is used
     *
     * @return <p>All positions where this structure type is used</p>
     */
    public abstract List<BlockVector> getStructureTypePositions();

    /**
     * Checks if a block in the built portal matches the block in this structure
     *
     * @param blockVector <p>The position of the block to check</p>
     * @param material    <p>The material found in the built portal</p>
     * @return <p>True if the material matches</p>
     */
    protected abstract boolean isValidBlock(BlockVector blockVector, Material material);

    public abstract void generateStructure(VectorOperation converter, Location topLeft);
}
