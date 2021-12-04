package net.TheDgtl.Stargate.vectorlogic;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * Test class for shared vector operation tests to prevent duplication
 *
 * @author Kristian
 */
public class VectorOperationTester {

    private final List<Vector> testVectors;

    /**
     * Instantiates a new vector operation tester
     *
     * @param testVectors <p>A list of random vectors to use for testing</p>
     */
    public VectorOperationTester(List<Vector> testVectors) {
        this.testVectors = testVectors;
    }

    public void noRotationForEastTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Assertions.assertEquals(vector, operation.performOperation(vector));
        }
    }

    public void originalVectorNotModifiedTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector oldVector = vector.copy(vector);
            operation.performOperation(vector);
            Assertions.assertEquals(oldVector, vector);
        }
    }

    public void rotateSouthTest(IVectorOperation operation) {
        testSouthRotation(operation, false);
    }

    /**
     * Tests a southwards vector-rotation
     *
     * @param operation <p>The operation to run</p>
     * @param invert    <p>Whether to invert the operation</p>
     */
    private void testSouthRotation(IVectorOperation operation, boolean invert) {
        for (Vector vector : testVectors) {
            Vector newVector = invert ? operation.performInverseOperation(vector) : operation.performOperation(vector);
            Vector rotatedVector = new Vector(vector.getZ(), vector.getY(), -vector.getX());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void rotateWestTest(IVectorOperation operation) {
        testWestRotation(operation, false);
    }

    /**
     * Tests a westwards vector-rotation
     *
     * @param operation <p>The operation to run</p>
     * @param invert    <p>Whether to invert the operation</p>
     */
    private void testWestRotation(IVectorOperation operation, boolean invert) {
        for (Vector vector : testVectors) {
            Vector newVector = invert ? operation.performInverseOperation(vector) : operation.performOperation(vector);
            Vector rotatedVector = new Vector(-vector.getX(), vector.getY(), -vector.getZ());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void inverseOperationTest(IVectorOperation operation) {
        for (Vector vector: testVectors) {
            Vector operatedVector = operation.performOperation(vector);
            Vector inverseOperatedVector = operation.performInverseOperation(operatedVector);
            Assertions.assertEquals(inverseOperatedVector, vector);
        }
    }

    public void rotateNorthTest(IVectorOperation operation) {
        testNorthRotation(operation, false);
    }

    /**
     * Tests a northwards vector-rotation
     *
     * @param operation <p>The operation to run</p>
     * @param invert    <p>Whether to invert the operation</p>
     */
    private void testNorthRotation(IVectorOperation operation, boolean invert) {
        for (Vector vector : testVectors) {
            Vector newVector = invert ? operation.performInverseOperation(vector) : operation.performOperation(vector);
            Vector rotatedVector = new Vector(-vector.getZ(), vector.getY(), vector.getX());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void rotateUpTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(vector.getX(), -vector.getZ(), vector.getY());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void rotateDownTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(vector.getX(), vector.getZ(), -vector.getY());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void runningOperationTwiceGivesInitialTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            newVector = operation.performOperation(newVector);
            Assertions.assertEquals(vector, newVector);
        }
    }

    public void runningOperationFourTimesGivesInitialTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            newVector = operation.performOperation(newVector);
            newVector = operation.performOperation(newVector);
            newVector = operation.performOperation(newVector);
            Assertions.assertEquals(vector, newVector);
        }
    }

    public void rotateEastFlipZTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(vector.getX(), vector.getY(), -vector.getZ());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void rotateWestFlipZTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(-vector.getX(), vector.getY(), vector.getZ());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void rotateNorthFlipZTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(-vector.getZ(), vector.getY(), -vector.getX());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void rotateSouthFlipZTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(vector.getZ(), vector.getY(), vector.getX());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

}
