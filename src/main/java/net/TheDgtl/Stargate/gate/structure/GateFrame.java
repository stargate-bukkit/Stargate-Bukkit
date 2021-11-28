package net.TheDgtl.Stargate.gate.structure;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the frame part of the gate structure
 */
public class GateFrame extends GateStructure {

    Map<BlockVector, Set<Material>> parts;

    /**
     * Instantiates a new gate frame
     */
    public GateFrame() {
        parts = new HashMap<>();
    }

    /**
     * Adds a block to this gate frame
     *
     * @param blockVector <p>The block vector describing the location of the block</p>
     * @param materials   <p>The usable materials for the block</p>
     */
    public void addPart(BlockVector blockVector, Set<Material> materials) {
        parts.put(blockVector, materials);
    }

    @Override
    public List<BlockVector> getStructureTypePositions() {
        return new ArrayList<>(parts.keySet());
    }

    @Override
    protected boolean isValidBlock(BlockVector blockVector, Material material) {
        return parts.get(blockVector).contains(material);
    }

}
