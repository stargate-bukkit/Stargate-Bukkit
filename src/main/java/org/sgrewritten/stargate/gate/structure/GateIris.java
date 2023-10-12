package org.sgrewritten.stargate.gate.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents the iris (opening) part of the gate structure
 */
public class GateIris extends GateStructure {

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
