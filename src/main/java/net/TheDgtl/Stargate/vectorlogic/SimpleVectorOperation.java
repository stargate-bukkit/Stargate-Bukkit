package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.exception.InvalidStructureException;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simpler version of the vector operation class, but with the same functionality
 *
 * @author Kristian
 */
public class SimpleVectorOperation implements IVectorOperation {

    private final Axis irisNormal;
    private boolean flipZAxis = false;
    private final BlockFace facing;

    private final List<VectorAction> operation;
    private final List<VectorAction> inverseOperation;

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
        this.operation = getOperation(signFace);
        this.inverseOperation = getInverseOperation(this.operation);
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
        if (flipZAxis && !this.flipZAxis) {
            this.operation.add(VectorAction.NEGATE_Z);
            this.inverseOperation.add(0, VectorAction.NEGATE_Z);
        } else if (!flipZAxis == this.flipZAxis) {
            this.operation.remove(this.operation.size() - 1);
            this.inverseOperation.remove(0);
        }
        this.flipZAxis = flipZAxis;
    }

    @Override
    public Vector performOperation(Vector vector) {
        return performOperation(vector.clone(), false);
    }

    @Override
    public Vector performInverseOperation(Vector vector) {
        return performOperation(vector.clone(), true);
    }

    @Override
    public BlockVector performInverseOperation(BlockVector vector) {
        return performInverseOperation((Vector) vector).toBlockVector();
    }

    /**
     * Performs a rotation operation on the given vector
     *
     * @param vector <p>The vector to rotate</p>
     * @return <p>A rotated copy of the given vector</p>
     */
    private Vector performOperation(Vector vector, boolean inverse) {
        if (inverse) {
            runOperation(vector, this.inverseOperation);
        } else {
            runOperation(vector, this.operation);
        }
        return vector;
    }

    /**
     * Gets the operation corresponding to the given block face
     *
     * @param blockFace <p>The block face to rotate vector towards</p>
     * @return <p>The corresponding operation</p>
     */
    private List<VectorAction> getOperation(BlockFace blockFace) {
        switch (blockFace) {
            case EAST:
                return getEastOperation();
            case WEST:
                return getWestOperation();
            case NORTH:
                return getNorthOperation();
            case SOUTH:
                return getSouthOperation();
            case UP:
                return getUpOperation();
            case DOWN:
                return getDownOperation();
            default:
                throw new IllegalArgumentException("Unrecognized block face used for initialization");
        }
    }

    /**
     * Runs the given operation on the given vector
     *
     * @param inputVector  <p>The vector to run the operation on</p>
     * @param actionsToRun <p>The actions to run as part of the operation</p>
     */
    private void runOperation(Vector inputVector, List<VectorAction> actionsToRun) {
        for (VectorAction action : actionsToRun) {
            runAction(inputVector, action);
        }
    }

    /**
     * Gets the reverse operation of a given operation
     *
     * @param operation <p>The operation to get the reverse of</p>
     * @return <p>The reverse of the given operation</p>
     */
    private static List<VectorAction> getInverseOperation(List<VectorAction> operation) {
        List<VectorAction> reverseOperation = new ArrayList<>(operation);
        Collections.reverse(reverseOperation);
        return reverseOperation;
    }

    /**
     * Runs one action on the given vector
     *
     * @param inputVector <p>The vector to run the action on</p>
     * @param actionToRun <p>The action to run</p>
     */
    private static void runAction(Vector inputVector, VectorAction actionToRun) {
        switch (actionToRun) {
            case NEGATE_Z:
                inputVector.setZ(-inputVector.getZ());
                break;
            case NEGATE_X:
                inputVector.setX(-inputVector.getX());
                break;
            case NEGATE_Y:
                inputVector.setY(-inputVector.getY());
                break;
            case SWAP_X_Z:
                double temp = inputVector.getX();
                inputVector.setX(inputVector.getZ());
                inputVector.setZ(temp);
                break;
            case SWAP_Y_Z:
                double temp2 = inputVector.getY();
                inputVector.setY(inputVector.getZ());
                inputVector.setZ(temp2);
                break;
            default:
                throw new IllegalArgumentException("Invalid vector action encountered");
        }
    }

    /**
     * Gets the required operation for rotating a vector to face east
     *
     * @return <p>The required operation for rotating a vector to face east</p>
     */
    private static List<VectorAction> getEastOperation() {
        return new ArrayList<>();
    }

    /**
     * Gets the required operation for rotating a vector to face west
     *
     * @return <p>The required operation for rotating a vector to face west</p>
     */
    private static List<VectorAction> getWestOperation() {
        List<VectorAction> actions = new ArrayList<>(3);
        actions.add(VectorAction.NEGATE_X);
        actions.add(VectorAction.NEGATE_Z);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face south
     *
     * @return <p>The required operation for rotating a vector to face south</p>
     */
    private static List<VectorAction> getSouthOperation() {
        List<VectorAction> actions = new ArrayList<>(3);
        actions.add(VectorAction.NEGATE_X);
        actions.add(VectorAction.SWAP_X_Z);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face north
     *
     * @return <p>The required operation for rotating a vector to face north</p>
     */
    private static List<VectorAction> getNorthOperation() {
        List<VectorAction> actions = new ArrayList<>(3);
        actions.add(VectorAction.NEGATE_Z);
        actions.add(VectorAction.SWAP_X_Z);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face upwards
     *
     * @return <p>The required operation for rotating a vector to face upwards</p>
     */
    private static List<VectorAction> getUpOperation() {
        List<VectorAction> actions = new ArrayList<>(3);
        actions.add(VectorAction.NEGATE_Z);
        actions.add(VectorAction.SWAP_Y_Z);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face downwards
     *
     * @return <p>The required operation for rotating a vector to face downwards</p>
     */
    private static List<VectorAction> getDownOperation() {
        List<VectorAction> actions = new ArrayList<>(3);
        actions.add(VectorAction.NEGATE_Y);
        actions.add(VectorAction.SWAP_Y_Z);
        return actions;
    }

}
