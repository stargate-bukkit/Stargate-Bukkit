package org.sgrewritten.stargate.gate.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.structure.GateStructure;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;
import org.sgrewritten.stargate.manager.StargateBlockDropManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

/**
 * Represents the frame part of the gate structure
 */
public class GateFrame extends GateStructure {

    final Map<BlockVector, Set<Material>> parts;
    final static Random RANDOM = new Random();
    private final BoundingBox boundingBox;

    /**
     * Instantiates a new gate frame
     */
    public GateFrame() {
        parts = new HashMap<>();
        this.boundingBox = new BoundingBox();
    }

    /**
     * Adds a block to this gate frame
     *
     * @param blockVector <p>The block vector describing the location of the block</p>
     * @param materials   <p>The usable materials for the block</p>
     */
    public void addPart(BlockVector blockVector, Set<Material> materials) {
        parts.put(blockVector, materials);
        boundingBox.union(blockVector);
    }

    @Override
    public List<BlockVector> getStructureTypePositions() {
        return new ArrayList<>(parts.keySet());
    }

    @Override
    protected boolean isValidBlock(BlockVector blockVector, Material material) {
        Set<Material> materials = parts.get(blockVector);
        if (materials == null) {
            Stargate.log(Level.SEVERE, "Could not get materials for: " + blockVector);
            return false;
        }
        return materials.contains(material);
    }

    @Override
    public void generateStructure(VectorOperation converter, Location topLeft) {
        for (Map.Entry<BlockVector,Set<Material>> entry : parts.entrySet()) {
            Location location = topLeft.clone().add(converter.performToRealSpaceOperation(entry.getKey()));
            Set<Material> materialsAtPosition = entry.getValue();
            Material material = materialsAtPosition.toArray(new Material[0])[RANDOM.nextInt(materialsAtPosition.size())];
            Block block = location.getBlock();
            if(material == block.getType()){
                continue;
            }
            block.setType(material);
            StargateBlockDropManager.disableBlockDrops(block);
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

}
