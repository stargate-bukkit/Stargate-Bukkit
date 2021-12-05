package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.exception.InvalidStructureException;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * A simpler version of the vector operation class, but with the same functionality
 *
 * @author Kristian
 */
public class SimpleVectorOperation implements IVectorOperation {

    private static final Map<BlockFace, Double> rotationAngles = new HashMap<>();
    private static final Map<BlockFace, Vector> rotationAxes = new HashMap<>();

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
        if (rotationAxes.isEmpty()) {
            initializeOperations();
        }
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
    public Vector performOperation(Vector vector) {
        Vector clone = vector.clone();
        clone.rotateAroundAxis(rotationAxes.get(facing), rotationAngles.get(facing));
        if (flipZAxis) {
            clone.setZ(-clone.getZ());
        }
        return clone;
    }

    @Override
    public Vector performInverseOperation(Vector vector) {
        Vector clone = vector.clone();
        if (flipZAxis) {
            clone.setZ(-clone.getZ());
        }
        return clone.rotateAroundAxis(rotationAxes.get(facing), -rotationAngles.get(facing));
    }

    @Override
    public BlockVector performInverseOperation(BlockVector vector) {
        return performInverseOperation((Vector) vector).toBlockVector();
    }

    /**
     * Initializes the operations used for rotating to each block-face
     */
    private static void initializeOperations() {
        Vector yAxis = new Vector(0, 1, 0);
        Vector zAxis = new Vector(0, 0, 1);
        double quarterRotation = Math.PI / 2;

        rotationAxes.put(BlockFace.EAST, yAxis);
        rotationAngles.put(BlockFace.EAST, 0d);

        rotationAxes.put(BlockFace.WEST, yAxis);
        rotationAngles.put(BlockFace.WEST, Math.PI);

        rotationAxes.put(BlockFace.SOUTH, yAxis);
        rotationAngles.put(BlockFace.SOUTH, quarterRotation);

        rotationAxes.put(BlockFace.NORTH, yAxis);
        rotationAngles.put(BlockFace.NORTH, -quarterRotation);

        rotationAxes.put(BlockFace.UP, zAxis);
        rotationAngles.put(BlockFace.UP, quarterRotation);

        rotationAxes.put(BlockFace.DOWN, zAxis);
        rotationAngles.put(BlockFace.DOWN, -quarterRotation);
    }

}
