package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.exception.InvalidStructureException;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

/**
 * A simpler version of the vector operation class, but with the same functionality
 *
 * @author Kristian
 */
public class SimpleVectorOperation implements IVectorOperation {

    private final Axis irisNormal;
    private boolean flipZAxis = false;
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
    public SimpleVectorOperation(BlockFace signFace) throws InvalidStructureException {

        if (signFace == BlockFace.EAST || signFace == BlockFace.WEST) {
            irisNormal = Axis.Z;
        } else if (signFace == BlockFace.NORTH || signFace == BlockFace.SOUTH) {
            irisNormal = Axis.X;
        } else if (signFace == BlockFace.UP || signFace == BlockFace.DOWN) {
            irisNormal = Axis.Y;
        } else {
            throw new InvalidStructureException();
        }

        this.facing = signFace;
    }

    /**
     * Gets the block face of a sign given upon instantiation
     *
     * @return <p>The block face of a sign given upon instantiation</p>
     */
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
    public Vector performOperation(Vector vector) {
        return performOperation(vector, facing);
    }

    @Override
    public Vector performInverseOperation(Vector vector) {
        return performOperation(vector, facing.getOppositeFace());
    }

    @Override
    public BlockVector performInverseOperation(BlockVector vector) {
        return performInverseOperation((Vector) vector).toBlockVector();
    }

    /**
     * Performs a rotation operation on the given vector
     *
     * @param vector    <p>The vector to rotate</p>
     * @param blockFace <p>The block face the vector should be facing</p>
     * @return <p>A rotated copy of the given vector</p>
     */
    private Vector performOperation(Vector vector, BlockFace blockFace) {
        Vector newVector;
        switch (blockFace) {
            case EAST:
                newVector = vector.clone();
                break;
            case WEST:
                newVector = new Vector(-vector.getX(), vector.getY(), -vector.getZ());
                break;
            case SOUTH:
                newVector = new Vector(vector.getZ(), vector.getY(), -vector.getX());
                break;
            case NORTH:
                newVector = new Vector(-vector.getZ(), vector.getY(), vector.getX());
                break;
            case UP:
                newVector = new Vector(vector.getX(), -vector.getZ(), vector.getY());
                break;
            case DOWN:
                newVector = new Vector(vector.getX(), vector.getZ(), -vector.getY());
                break;
            default:
                throw new IllegalArgumentException("Unrecognized block face used for initialization");
        }

        //Flip the axis to allow a non-symmetrical design to be used both ways
        if (flipZAxis) {
            newVector.setZ(-newVector.getZ());
        }

        return newVector;
    }

}
