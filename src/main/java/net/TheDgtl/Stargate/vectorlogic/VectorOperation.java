package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * A vector operation helps with various operations done on vectors
 *
 * <p>Vector operations keeps vectors immutable.</p>
 */
public class VectorOperation implements IVectorOperation {

    private final Axis irisNormal;
    private boolean flipZAxis = false;
    private final MatrixYRotation matrixRotation;
    private final MatrixYRotation matrixInverseRotation;
    private final BlockFace facing;

    /**
     * Instantiates a vector operation to rotate vectors in the direction of a sign face
     *
     * <p>Gate structures have their relative location represented by a vector where x = outwards, y = down and
     * z = right. The vector operation rotates the given vectors so that "outwards" is going the same direction as the
     * given sign face.</p>
     *
     * @param signFace <p>The sign face of a gate's sign</p>
     * @param logger   <p>The logger to use for logging debug messages</p>
     * @throws InvalidStructureException <p>If given a sign face which is not one of EAST, SOUTH, WEST or NORTH</p>
     */
    public VectorOperation(BlockFace signFace, StargateLogger logger) throws InvalidStructureException {
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
                throw new InvalidStructureException();
        }

        this.facing = signFace;
        matrixRotation = new MatrixYRotation(rotation);
        matrixInverseRotation = matrixRotation.getInverse();
        logger.logMessage(Level.FINER, "Chose a format rotation of " + rotation + " radians");
    }

    @Override
    public BlockFace getFacing() {
        return facing;
    }

    @Override
    public Axis getIrisNormal() {
        return irisNormal;
    }

    @Override
    public void setFlipZAxis(boolean flipZAxis) {
        this.flipZAxis = flipZAxis;
    }

    @Override
    public Vector performToAbstractSpaceOperation(@NotNull Vector vector) {
        Vector output = matrixRotation.performOperation(vector);
        if (flipZAxis) {
            output.setZ(-output.getZ());
        }
        return output;
    }

    @Override
    public Vector performToRealSpaceOperation(@NotNull Vector vector) {
        Vector output = vector.clone();
        if (flipZAxis) {
            output.setZ(-output.getZ());
        }
        return matrixInverseRotation.performOperation(output);
    }

    @Override
    public BlockVector performToRealSpaceOperation(@NotNull BlockVector vector) {
        return performToRealSpaceOperation((Vector) vector).toBlockVector();
    }

}
