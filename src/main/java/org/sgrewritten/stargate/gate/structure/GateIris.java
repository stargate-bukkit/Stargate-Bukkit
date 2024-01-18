package org.sgrewritten.stargate.gate.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.api.gate.structure.GateStructure;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.util.VectorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Represents the iris (opening) part of the gate structure
 */
public class GateIris extends GateStructure {
    private static final Random RANDOM = new Random();

    public final Set<Material> irisOpen;
    public final Set<Material> irisClosed;
    BlockVector exit;
    protected final List<BlockVector> blocks;

    /**
     * Instantiates a new gate iris
     *
     * @param irisOpen   <p>The set of materials usable for an open iris</p>
     * @param irisClosed <p>The set of materials usable for a closed iris</p>
     */
    public GateIris(Set<Material> irisOpen, Set<Material> irisClosed) {
        this.irisOpen = irisOpen;
        this.irisClosed = irisClosed;
        blocks = new ArrayList<>();
    }

    /**
     * Adds a block to the blocks which are part of this iris
     *
     * @param blockVector <p>A block vector describing the location of a block</p>
     */
    public void addPart(BlockVector blockVector) {
        blocks.add(blockVector);
    }

    /**
     * Adds an exit point to this iris
     *
     * @param exitPoint <p>The point at which players should exit from the iris</p>
     */
    public void addExit(BlockVector exitPoint) {
        this.exit = exitPoint.clone();
        addPart(exitPoint);
    }

    @Override
    public List<BlockVector> getStructureTypePositions() {
        return blocks;
    }

    @Override
    protected boolean isValidBlock(BlockVector blockVector, Material material) {
        return irisClosed.contains(material) || irisOpen.contains(material);
    }

    @Override
    public void generateStructure(VectorOperation converter, Location topLeft) {
        // (Clear all blocks that are in the portal iris)
        Material[] irisClosedList = irisClosed.toArray(new Material[0]);
        for(BlockVector blockVector : this.blocks){
            int target = RANDOM.nextInt(irisClosedList.length)-1;
            Material chosenType = irisClosedList[target];
            Location location = VectorUtils.getLocation(topLeft,converter,blockVector);
            new StargateRegionTask(location, () -> {
                Block block = location.getBlock();
                BlockData blockData = chosenType.createBlockData();
                // Over-engineering :)
                if(blockData instanceof Orientable orientable){
                    orientable.setAxis(converter.getIrisNormal());
                }
                block.setBlockData(blockData);
            }).run();
        }
    }

    /**
     * Gets one of the materials used for this iris
     *
     * @param isOpen <p>Whether to get an open-material or a closed-material</p>
     * @return <p>One of the usable materials</p>
     */
    public Material getMaterial(boolean isOpen) {
        return (isOpen ? irisOpen : irisClosed).iterator().next();
    }

    /**
     * Gets the exit location of this iris
     *
     * @return <p>The exit location of this iris</p>
     */
    public BlockVector getExit() {
        return exit;
    }

}
