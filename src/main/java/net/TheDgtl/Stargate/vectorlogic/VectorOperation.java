package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.InvalidStructure;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.logging.Level;

/**
 * A vector operation helps with various operations done on vectors
 *
 * <p>Vector operations keeps vectors immutable.</p>
 */
public class VectorOperation {

    private final Axis irisNormal;
    private boolean flipZAxis = false;
    private final MatrixYRotation matrixRotation;
    private final MatrixYRotation matrixInverseRotation;
    private final BlockFace facing;

    /**
     * Instantiates a vector operation which matches with the direction of a sign
     *
     * @param signFace <p>The sign face of a gate's sign</p>
     * @throws InvalidStructure <p>If given a sign face which is not one of EAST, SOUTH, WEST or NORTH</p>
     */
    public VectorOperation(BlockFace signFace) throws InvalidStructure {
        double rotation;

        switch (signFace) {
            case EAST:
                rotation = 0;
                irisNormal = Axis.Z;
                break;
            case SOUTH:
                rotation = Math.PI / 2;
                irisNormal = Axis.X;
                break;
            case WEST:
                rotation = Math.PI;
                irisNormal = Axis.Z;
                break;
            case NORTH:
                rotation = -Math.PI / 2;
                irisNormal = Axis.X;
                break;
            default:
                throw new InvalidStructure();
        }

        this.facing = signFace;
        matrixRotation = new MatrixYRotation(rotation);
        matrixInverseRotation = matrixRotation.getInverse();
        Stargate.log(Level.FINER, "Chose a format rotation of " + rotation + " radians");
    }

    /**
     * Gets the block face of a sign given upon instantiation
     *
     * @return <p>The block face of a sign given upon instantiation</p>
     */
    public BlockFace getFacing() {
        return facing;
    }

    /**
     * Gets the normal axis orthogonal to the iris plane
     *
     * <p>Said another way, get the axis going directly towards or away from a stargate's entrance.</p>
     *
     * @return <p>The normal axis orthogonal to the iris plane</p>
     */
    public Axis getIrisNormal() {
        return irisNormal;
    }

    /**
     * Sets whether to flip the Z- axis
     *
     * @param flipZAxis <p>Whether to flip the z-axis</p>
     */
    public void setFlipZAxis(boolean flipZAxis) {
        this.flipZAxis = flipZAxis;
    }

    /**
     * Performs this vector operation on the given vector
     *
     * <p>Inverse operation of doInverse; A vector operation that rotates around the origin, and flips the z-axis.
     * Does not permute input vector</p>
     *
     * @param vector <p>The vector to perform the operation on</p>
     * @return vector <p>A new vector with the operation applied</p>
     */
    public Vector performOperation(Vector vector) {
        Vector output = matrixRotation.performOperation(vector);
        if (flipZAxis) {
            output.setZ(-output.getZ());
        }
        return output;
    }

    /**
     * Performs the reverse of this vector operation on the given vector
     *
     * <p>Inverse operation of doOperation; A vector operation that rotates around
     * the origin and flips the z-axis. Does not permute input vector</p>
     *
     * @param vector <p>The vector to perform the inverse operation on</p>
     * @return vector <p>A new vector with the inverse operation applied</p>
     */
    public Vector performInverseOperation(Vector vector) {
        Vector output = vector.clone();
        if (flipZAxis) {
            output.setZ(-output.getZ());
        }
        return matrixInverseRotation.performOperation(output);
    }

    /**
     * Performs the reverse of this vector operation on the given vector
     *
     * <p>Inverse operation of doOperation; A vector operation that rotates around
     * the origin and flips the z-axis. Does not permute input vector</p>
     *
     * @param vector <p>The vector to perform the inverse operation on</p>
     * @return vector <p>A new vector with the inverse operation applied</p>
     */
    public BlockVector performInverseOperation(BlockVector vector) {
        return performInverseOperation((Vector) vector).toBlockVector();
    }

}
