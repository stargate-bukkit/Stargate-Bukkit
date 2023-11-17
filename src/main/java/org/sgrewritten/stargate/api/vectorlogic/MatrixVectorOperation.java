package org.sgrewritten.stargate.api.vectorlogic;

import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.InvalidStructureException;

import java.util.logging.Level;

/**
 * A vector operation helps with various operations done on vectors
 *
 * <p>Vector operations keeps vectors immutable.</p>
 */
public class MatrixVectorOperation implements VectorOperation {

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
     * @throws InvalidStructureException <p>If given a sign face which is not one of EAST, SOUTH, WEST or NORTH</p>
     */
    public MatrixVectorOperation(BlockFace signFace) throws InvalidStructureException {
        double rotation;

        switch (signFace) {
            case EAST -> {
                rotation = 0;
                irisNormal = Axis.Z;
            }
            case SOUTH -> {
                rotation = Math.PI / 2;
                irisNormal = Axis.X;
            }
            case WEST -> {
                rotation = Math.PI;
                irisNormal = Axis.Z;
            }
            case NORTH -> {
                rotation = -Math.PI / 2;
                irisNormal = Axis.X;
            }
            default -> throw new InvalidStructureException();
        }

        this.facing = signFace;
        matrixRotation = new MatrixYRotation(rotation);
        matrixInverseRotation = matrixRotation.getInverse();
        Stargate.log(Level.FINER, "Chose a format rotation of " + rotation + " radians");
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
