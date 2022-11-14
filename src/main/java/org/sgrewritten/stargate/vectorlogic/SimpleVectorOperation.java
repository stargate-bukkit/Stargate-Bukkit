package org.sgrewritten.stargate.vectorlogic;

import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.exception.InvalidStructureException;

import java.util.HashMap;
import java.util.Map;

/**
 * A simpler version of the vector operation class, but with the same functionality
 *
 * @author Kristian
 */
public class SimpleVectorOperation implements VectorOperation {

    private static final Map<BlockFace, Double> rotationAngles = new HashMap<>();
    private static final Map<BlockFace, Vector> rotationAxes = new HashMap<>();
    private static final Map<BlockFace, Axis> irisNormalAxes = new HashMap<>();
    private static final BlockFace defaultDirection = BlockFace.EAST;
    private static final Axis defaultVerticalAxis = Axis.Y;

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
     */
    public SimpleVectorOperation(BlockFace signFace) throws InvalidStructureException {
        if (irisNormalAxes.isEmpty()) {
            initializeIrisNormalAxes();
            initializeOperations();
        }

        this.facing = signFace;
        this.irisNormal = irisNormalAxes.get(signFace);
        if (irisNormal == null) {
            throw new InvalidStructureException();
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
    public Vector performToAbstractSpaceOperation(@NotNull Vector vector) {
        Vector clone = vector.clone();
        clone.rotateAroundAxis(rotationAxes.get(facing), rotationAngles.get(facing));
        if (flipZAxis) {
            clone.setZ(-clone.getZ());
        }
        return clone;
    }

    @Override
    public Vector performToRealSpaceOperation(@NotNull Vector vector) {
        Vector clone = vector.clone();
        if (flipZAxis) {
            clone.setZ(-clone.getZ());
        }
        return clone.rotateAroundAxis(rotationAxes.get(facing), -rotationAngles.get(facing));
    }

    @Override
    public BlockVector performToRealSpaceOperation(@NotNull BlockVector vector) {
        return performToRealSpaceOperation((Vector) vector).toBlockVector();
    }

    /**
     * Initializes the operations used for rotating to each block-face
     */
    private static void initializeOperations() {
        Map<Axis, Vector> axisVectors = new HashMap<>();
        axisVectors.put(Axis.Y, new Vector(0, 1, 0));
        axisVectors.put(Axis.X, new Vector(1, 0, 0));
        axisVectors.put(Axis.Z, new Vector(0, 0, 1));

        //Use the cross product to find the correct axis
        for (BlockFace face : irisNormalAxes.keySet()) {
            Vector crossProduct = face.getDirection().crossProduct(defaultDirection.getDirection());
            if (face == defaultDirection || face == defaultDirection.getOppositeFace()) {
                rotationAxes.put(face, axisVectors.get(defaultVerticalAxis));
            } else if (Math.abs(crossProduct.getZ()) > 0) {
                rotationAxes.put(face, axisVectors.get(Axis.Z));
            } else if (Math.abs(crossProduct.getY()) > 0) {
                rotationAxes.put(face, axisVectors.get(Axis.Y));
            } else {
                rotationAxes.put(face, axisVectors.get(Axis.X));
            }
        }

        calculateRotations();
    }

    /**
     * Calculates the required rotations based on the default rotation
     */
    private static void calculateRotations() {
        double halfRotation = Math.PI;
        double quarterRotation = halfRotation / 2;

        Vector defaultDirectionVector = defaultDirection.getDirection();
        boolean defaultDirectionPositive = defaultDirectionVector.getX() + defaultDirectionVector.getY() +
                defaultDirectionVector.getZ() > 0;

        for (BlockFace blockFace : irisNormalAxes.keySet()) {
            if (defaultDirection == blockFace) {
                //The default direction requires no rotation
                rotationAngles.put(blockFace, 0d);
            } else if (defaultDirection.getOppositeFace() == blockFace) {
                //The opposite direction requires a half rotation
                rotationAngles.put(blockFace, halfRotation);
            } else {
                //All the other used directions require a quarter rotation
                Vector faceDirectionVector = blockFace.getDirection();
                boolean faceDirectionPositive = faceDirectionVector.getX() + faceDirectionVector.getY() +
                        faceDirectionVector.getZ() > 0;
                double rotation = defaultDirectionPositive && faceDirectionPositive ? quarterRotation : -quarterRotation;
                rotationAngles.put(blockFace, rotation);
            }
        }
    }

    /**
     * Initializes the iris normal axes corresponding to each block face
     */
    private static void initializeIrisNormalAxes() {
        irisNormalAxes.put(BlockFace.EAST, Axis.Z);
        irisNormalAxes.put(BlockFace.WEST, Axis.Z);
        irisNormalAxes.put(BlockFace.NORTH, Axis.X);
        irisNormalAxes.put(BlockFace.SOUTH, Axis.X);
        irisNormalAxes.put(BlockFace.UP, Axis.Y);
        irisNormalAxes.put(BlockFace.DOWN, Axis.Y);
    }

}
