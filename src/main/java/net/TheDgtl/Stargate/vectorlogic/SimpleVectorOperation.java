package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.exception.InvalidStructureException;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simpler version of the vector operation class, but with the same functionality
 *
 * @author Kristian
 */
public class SimpleVectorOperation implements IVectorOperation {

    private static final Map<BlockFace, List<VectorAction>> storedOperations = new HashMap<>();
    private static final Map<BlockFace, List<VectorAction>> storedReverseOperations = new HashMap<>();

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
        this.inverseOperation = new ArrayList<>(storedReverseOperations.get(signFace));
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
        if (flipZAxis != this.flipZAxis) {
            int lastIndex = this.operation.size() - 1;
            if (lastIndex >= 0 && this.operation.get(lastIndex).equals(VectorAction.NEGATE_Z)) {
                this.operation.remove(lastIndex);
            } else {
                this.operation.add(VectorAction.NEGATE_Z);
            }
            int inverseSize = inverseOperation.size();
            if (inverseSize > 0 && this.inverseOperation.get(0).equals(VectorAction.NEGATE_Z)) {
                this.inverseOperation.remove(0);
            } else {
                this.inverseOperation.add(0, VectorAction.NEGATE_Z);
            }
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
            return runOperation(vector, this.inverseOperation);
        } else {
            return runOperation(vector, this.operation);
        }
    }

    /**
     * Gets the operation corresponding to the given block face
     *
     * @param blockFace <p>The block face to rotate vector towards</p>
     * @return <p>The corresponding operation</p>
     */
    private static List<VectorAction> getOperation(BlockFace blockFace) {
        List<VectorAction> storedOperation = storedOperations.get(blockFace);
        if (storedOperation != null) {
            return new ArrayList<>(storedOperation);
        }

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
    private Vector runOperation(Vector inputVector, List<VectorAction> actionsToRun) {
        optimizeOperation(actionsToRun);

        for (VectorAction action : actionsToRun) {
            inputVector = runAction(inputVector, action);
        }
        return inputVector;
    }

    /**
     * Combines two actions into a single action
     *
     * @param action1 <p>The first action to combine</p>
     * @param action2 <p>The second action to combine</p>
     * @return <p>An action doing the same as doing the input actions in succession</p>
     */
    private static VectorAction combineActions(VectorAction action1, VectorAction action2) {
        if (action1 == VectorAction.NEGATE_X && action2 == VectorAction.NEGATE_Z ||
                action1 == VectorAction.NEGATE_Z && action2 == VectorAction.NEGATE_X) {
            return VectorAction.NEGATE_X_NEGATE_Z;
        } else if (action1 == VectorAction.NEGATE_Z && action2 == VectorAction.SWAP_X_Z) {
            return VectorAction.NEGATE_Z_SWAP_X_Z;
        } else if (action1 == VectorAction.NEGATE_X && action2 == VectorAction.SWAP_X_Z) {
            return VectorAction.NEGATE_X_SWAP_X_Z;
        } else if (action1 == VectorAction.SWAP_X_Z && action2 == VectorAction.NEGATE_Z) {
            return VectorAction.SWAP_X_Z_NEGATE_Z;
        } else if (action1 == VectorAction.SWAP_X_Z && action2 == VectorAction.NEGATE_X) {
            return VectorAction.SWAP_X_Z_NEGATE_X;
        } else if (action1 == VectorAction.NEGATE_Z_SWAP_X_Z && action2 == VectorAction.NEGATE_Z) {
            return VectorAction.SWAP_X_Z_NEGATE_X_Z;
        } else if (action1 == VectorAction.NEGATE_X_SWAP_X_Z && action2 == VectorAction.NEGATE_Z) {
            return VectorAction.SWAP_X_Z;
        } else if (action1 == VectorAction.NEGATE_Z && action2 == VectorAction.SWAP_Y_Z) {
            return VectorAction.NEGATE_Z_SWAP_Y_Z;
        } else if (action1 == VectorAction.NEGATE_Y && action2 == VectorAction.SWAP_Y_Z) {
            return VectorAction.NEGATE_Y_SWAP_Y_Z;
        }

        throw new IllegalArgumentException("No known combination of " + action1 + " and " + action2);
    }

    /**
     * Runs one action on the given vector
     *
     * @param inputVector <p>The vector to run the action on</p>
     * @param actionToRun <p>The action to run</p>
     * @return <p>The resulting vector</p>
     */
    private static Vector runAction(Vector inputVector, VectorAction actionToRun) {
        switch (actionToRun) {
            case NEGATE_Z:
                return new Vector(inputVector.getX(), inputVector.getY(), -inputVector.getZ());
            case NEGATE_X:
                return new Vector(-inputVector.getX(), inputVector.getY(), inputVector.getZ());
            case NEGATE_Y:
                return new Vector(inputVector.getX(), -inputVector.getY(), inputVector.getZ());
            case SWAP_X_Z:
                return new Vector(inputVector.getZ(), inputVector.getY(), inputVector.getX());
            case SWAP_Y_Z:
                return new Vector(inputVector.getX(), inputVector.getZ(), inputVector.getY());
            case NEGATE_X_NEGATE_Z:
                return new Vector(-inputVector.getX(), inputVector.getY(), -inputVector.getZ());
            case NEGATE_X_SWAP_X_Z:
            case SWAP_X_Z_NEGATE_Z:
                return new Vector(inputVector.getZ(), inputVector.getY(), -inputVector.getX());
            case NEGATE_Y_SWAP_Y_Z:
            case SWAP_Y_Z_NEGATE_Z:
                return new Vector(inputVector.getX(), inputVector.getZ(), -inputVector.getY());
            case NEGATE_Z_SWAP_X_Z:
            case SWAP_X_Z_NEGATE_X:
                return new Vector(-inputVector.getZ(), inputVector.getY(), inputVector.getX());
            case NEGATE_Z_SWAP_Y_Z:
            case SWAP_Y_Z_NEGATE_Y:
                return new Vector(inputVector.getX(), -inputVector.getZ(), inputVector.getY());
            case SWAP_X_Z_NEGATE_X_Z:
                return new Vector(-inputVector.getZ(), inputVector.getY(), -inputVector.getX());
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
        List<VectorAction> actions = new ArrayList<>();
        storeOperation(actions, BlockFace.EAST);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face west
     *
     * @return <p>The required operation for rotating a vector to face west</p>
     */
    private static List<VectorAction> getWestOperation() {
        List<VectorAction> actions = new ArrayList<>();
        actions.add(VectorAction.NEGATE_X);
        actions.add(VectorAction.NEGATE_Z);
        storeOperation(actions, BlockFace.WEST);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face south
     *
     * @return <p>The required operation for rotating a vector to face south</p>
     */
    private static List<VectorAction> getSouthOperation() {
        List<VectorAction> actions = new ArrayList<>();
        actions.add(VectorAction.NEGATE_X);
        actions.add(VectorAction.SWAP_X_Z);
        storeOperation(actions, BlockFace.SOUTH);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face north
     *
     * @return <p>The required operation for rotating a vector to face north</p>
     */
    private static List<VectorAction> getNorthOperation() {
        List<VectorAction> actions = new ArrayList<>();
        actions.add(VectorAction.NEGATE_Z);
        actions.add(VectorAction.SWAP_X_Z);
        storeOperation(actions, BlockFace.NORTH);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face upwards
     *
     * @return <p>The required operation for rotating a vector to face upwards</p>
     */
    private static List<VectorAction> getUpOperation() {
        List<VectorAction> actions = new ArrayList<>();
        actions.add(VectorAction.NEGATE_Z);
        actions.add(VectorAction.SWAP_Y_Z);
        storeOperation(actions, BlockFace.UP);
        return actions;
    }

    /**
     * Gets the required operation for rotating a vector to face downwards
     *
     * @return <p>The required operation for rotating a vector to face downwards</p>
     */
    private static List<VectorAction> getDownOperation() {
        List<VectorAction> actions = new ArrayList<>();
        actions.add(VectorAction.NEGATE_Y);
        actions.add(VectorAction.SWAP_Y_Z);
        storeOperation(actions, BlockFace.DOWN);
        return actions;
    }

    /**
     * Stores the given actions as the operation for the given block face
     *
     * @param actions   <p>The actions part of the operation</p>
     * @param blockFace <p>The block face the operation rotates vectors towards</p>
     */
    private static void storeOperation(List<VectorAction> actions, BlockFace blockFace) {
        List<VectorAction> operation = new ArrayList<>(actions);
        storedOperations.put(blockFace, operation);

        List<VectorAction> reverseOperation = new ArrayList<>(actions);
        Collections.reverse(reverseOperation);
        storedReverseOperations.put(blockFace, reverseOperation);
    }

    /**
     * Optimizes an operation to just run a single action
     *
     * @param actionsToRun <p>The actions to run as part of the operation</p>
     */
    private static void optimizeOperation(List<VectorAction> actionsToRun) {
        while (actionsToRun.size() >= 2) {
            VectorAction combinedAction = combineActions(actionsToRun.get(0), actionsToRun.get(1));
            actionsToRun.remove(0);
            actionsToRun.remove(0);
            actionsToRun.add(0, combinedAction);
        }
    }

}
