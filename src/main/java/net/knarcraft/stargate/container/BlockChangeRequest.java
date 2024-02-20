package net.knarcraft.stargate.container;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a request for changing a block into another material
 */
public class BlockChangeRequest {

    private final BlockLocation blockLocation;
    private final Material newMaterial;
    private final Axis newAxis;

    /**
     * Instantiates a new block change request
     *
     * @param blockLocation <p>The location of the block to change</p>
     * @param material      <p>The new material to change the block to</p>
     * @param axis          <p>The new axis to orient the block along</p>
     */
    public BlockChangeRequest(@NotNull BlockLocation blockLocation, @NotNull Material material, @Nullable Axis axis) {
        this.blockLocation = blockLocation;
        newMaterial = material;
        newAxis = axis;
    }

    /**
     * Gets the location of the block to change
     *
     * @return <p>The location of the block</p>
     */
    @NotNull
    public BlockLocation getBlockLocation() {
        return blockLocation;
    }

    /**
     * Gets the material to change the block into
     *
     * @return <p>The material to change the block into</p>
     */
    @NotNull
    public Material getMaterial() {
        return newMaterial;
    }

    /**
     * Gets the axis to orient the block along
     *
     * @return <p>The axis to orient the block along</p>
     */
    @Nullable
    public Axis getAxis() {
        return newAxis;
    }

}
