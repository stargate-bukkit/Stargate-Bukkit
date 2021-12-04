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
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(vector.getZ(), vector.getY(), -vector.getX());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void rotateWestTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
            Vector rotatedVector = new Vector(-vector.getX(), vector.getY(), -vector.getZ());
            Assertions.assertEquals(rotatedVector, newVector);
        }
    }

    public void inverseOperationTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector operatedVector = operation.performOperation(vector);
            Vector inverseOperatedVector = operation.performInverseOperation(operatedVector);
            Assertions.assertEquals(inverseOperatedVector, vector);
        }
    }

    public void rotateNorthTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector newVector = operation.performOperation(vector);
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

    public void flipTest(IVectorOperation operation) {
        for (Vector vector : testVectors) {
            Vector flippedVector = new Vector(vector.getX(), vector.getY(), -vector.getZ());
            operation.setFlipZAxis(false);
            Vector operatedVector = operation.performInverseOperation(flippedVector);
            operation.setFlipZAxis(true);
            Vector inverseOperatedVector = operation.performOperation(operatedVector);
            Assertions.assertEquals(vector, inverseOperatedVector);
        }
    }
}
