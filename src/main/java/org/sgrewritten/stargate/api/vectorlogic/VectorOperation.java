package org.sgrewritten.stargate.api.vectorlogic;

import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface VectorOperation {

    /**
     * Gets the block face of a sign given upon instantiation
     *
     * @return <p>The block face of a sign given upon instantiation</p>
     */
    BlockFace getFacing();

    /**
     * Gets the normal axis orthogonal to the iris plane
     *
     * <p>Said another way, get the axis going directly towards or away from a stargate's entrance.</p>
     *
     * @return <p>The normal axis orthogonal to the iris plane</p>
     */
    Axis getIrisNormal();

    /**
     * Sets whether to flip the Z- axis
     *
     * @param flipZAxis <p>Whether to flip the z-axis</p>
     */
    void setFlipZAxis(boolean flipZAxis);

    /**
     * Performs this vector operation on the given vector
     *
     * <p>Inverse operation of doInverse; A vector operation that rotates around the origin, and flips the z-axis.
     * Does not permute input vector</p>
     *
     * @param vector <p>The vector to perform the operation on</p>
     * @return vector <p>A new vector with the operation applied</p>
     */
    Vector performToAbstractSpaceOperation(@NotNull Vector vector);

    /**
     * Performs the reverse of this vector operation on the given vector
     *
     * <p>Inverse operation of doOperation; A vector operation that rotates around
     * the origin and flips the z-axis. Does not permute input vector</p>
     *
     * @param vector <p>The vector to perform the inverse operation on</p>
     * @return vector <p>A new vector with the inverse operation applied</p>
     */
    Vector performToRealSpaceOperation(@NotNull Vector vector);

    /**
     * Performs the reverse of this vector operation on the given vector
     *
     * <p>Inverse operation of doOperation; A vector operation that rotates around
     * the origin and flips the z-axis. Does not permute input vector</p>
     *
     * @param vector <p>The vector to perform the inverse operation on</p>
     * @return vector <p>A new vector with the inverse operation applied</p>
     */
    BlockVector performToRealSpaceOperation(@NotNull BlockVector vector);

}
