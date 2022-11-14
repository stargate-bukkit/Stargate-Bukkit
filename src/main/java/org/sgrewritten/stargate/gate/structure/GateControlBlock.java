package org.sgrewritten.stargate.gate.structure;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one of the control blocks in a gate structure
 */
public class GateControlBlock extends GateStructure {

    final List<BlockVector> parts;

    /**
     * Instantiates a new gate control block container
     */
    public GateControlBlock() {
        parts = new ArrayList<>();
    }

    /**
     * Adds a vector to the list of control blocks
     *
     * @param blockVector <p>The block vector to add</p>
     */
    public void addPart(BlockVector blockVector) {
        parts.add(blockVector);
    }

    @Override
    public List<BlockVector> getStructureTypePositions() {
        return parts;
    }

    @Override
    protected boolean isValidBlock(BlockVector blockVector, Material material) {
        //TODO maybe add some fancy detection here
        return true;
    }

}
