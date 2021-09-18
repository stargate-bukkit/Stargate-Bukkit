package net.knarcraft.stargate.container;

import org.bukkit.Axis;
import org.bukkit.Material;

/**
 * Represents a request for changing a block into another material
 */
public class BlockChangeRequest {

    private BlockLocation blockLocation;
    private Material newMaterial;
    private Axis newAxis;

    /**
     * Instantiates a new block change request
     *
     * @param blockLocation <p>The location of the block to change</p>
     * @param material      <p>The new material to change the block to</p>
     * @param axis          <p>The new axis to orient the block along</p>
     */
    public BlockChangeRequest(BlockLocation blockLocation, Material material, Axis axis) {
        this.blockLocation = blockLocation;
        newMaterial = material;
        newAxis = axis;
    }

    /**
     * Gets the location of the block to change
     *
     * @return <p>The location of the block</p>
     */
    public BlockLocation getBlockLocation() {
        return blockLocation;
    }

    /**
     * Sets the location of the block
     *
     * @param blockLocation <p>The new location of the block</p>
     */
    public void setBlockLocation(BlockLocation blockLocation) {
        this.blockLocation = blockLocation;
    }

    /**
     * Gets the material to change the block into
     *
     * @return <p>The material to change the block into</p>
     */
    public Material getMaterial() {
        return newMaterial;
    }

    /**
     * Sets the material to change the block into
     *
     * @param material <p>The new material</p>
     */
    public void setMaterial(Material material) {
        newMaterial = material;
    }

    /**
     * Gets the axis to orient the block along
     *
     * @return <p>The axis to orient the block along</p>
     */
    public Axis getAxis() {
        return newAxis;
    }

    /**
     * Sets the axis to orient the block along
     *
     * @param axis <p>The new axis to orient the block along</p>
     */
    public void setAxis(Axis axis) {
        newAxis = axis;
    }

}
