package net.knarcraft.stargate;

import org.bukkit.Axis;
import org.bukkit.Material;

/**
 * Used to store information about a custom block populator
 */
public class BloxPopulator {

    private BlockLocation blockLocation;
    private Material nextMat;
    private Axis nextAxis;

    /**
     * Instantiates a new block populator
     *
     * @param blockLocation <p>The location to start from</p>
     * @param material      <p>The material to populate</p>
     */
    public BloxPopulator(BlockLocation blockLocation, Material material) {
        this.blockLocation = blockLocation;
        nextMat = material;
        nextAxis = null;
    }

    /**
     * Instantiates a new block populator
     *
     * @param blockLocation <p>The location to start from</p>
     * @param material      <p>The material to populate</p>
     * @param axis          <p>The axis to populate along</p>
     */
    public BloxPopulator(BlockLocation blockLocation, Material material, Axis axis) {
        this.blockLocation = blockLocation;
        nextMat = material;
        nextAxis = axis;
    }

    /**
     * Gets the location to start from
     *
     * @return <p>The location to start from</p>
     */
    public BlockLocation getBlockLocation() {
        return blockLocation;
    }

    /**
     * Sets the location to start from
     *
     * @param blockLocation <p>The new start location</p>
     */
    public void setBlockLocation(BlockLocation blockLocation) {
        this.blockLocation = blockLocation;
    }

    /**
     * Gets the material used for population
     *
     * @return <p>The material used for population</p>
     */
    public Material getMaterial() {
        return nextMat;
    }

    /**
     * Sets the polulator material
     *
     * @param material <p>The new populator material</p>
     */
    public void setMat(Material material) {
        nextMat = material;
    }

    /**
     * Gets the current population axis
     *
     * @return <p>The current population axis</p>
     */
    public Axis getAxis() {
        return nextAxis;
    }

    /**
     * Sets the populator axis
     *
     * @param axis <p>The new populator axis</p>
     */
    public void setAxis(Axis axis) {
        nextAxis = axis;
    }

}
