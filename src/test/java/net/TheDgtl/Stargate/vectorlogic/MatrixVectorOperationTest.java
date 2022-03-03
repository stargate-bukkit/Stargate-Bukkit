package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.FakeStargate;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatrixVectorOperationTest {

    private static VectorOperationTester vectorOperationTester;

    @BeforeAll
    public static void setUp() {
        List<Vector> testVectors = new ArrayList<>();
        Random random = new Random();
        int maxBound = 2000;
        for (int i = 0; i < 10000; i++) {
            testVectors.add(new Vector(random.nextInt(maxBound), -random.nextInt(maxBound), random.nextInt(maxBound)));
        }
        vectorOperationTester = new VectorOperationTester(testVectors);
    }

    @Test
    public void noRotationForEastTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.EAST, new FakeStargate());
        vectorOperationTester.noRotationForEastTest(operation);
    }

    @Test
    public void originalVectorNotModifiedTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH, new FakeStargate());
        vectorOperationTester.originalVectorNotModifiedTest(operation);
    }

    @Test
    public void rotateSouthTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH, new FakeStargate());
        vectorOperationTester.rotateSouthTest(operation);
    }

    @Test
    public void rotateWestTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST, new FakeStargate());
        vectorOperationTester.rotateWestTest(operation);
    }

    @Test
    public void rotateNorthTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH, new FakeStargate());
        vectorOperationTester.rotateNorthTest(operation);
    }

    @Test
    public void rotateEastInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.EAST, new FakeStargate());
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void rotateWestInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST, new FakeStargate());
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void rotateSouthInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH, new FakeStargate());
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void rotateNorthInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH, new FakeStargate());
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void runningWestOperationTwiceGivesInitialTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST, new FakeStargate());
        vectorOperationTester.runningOperationTwiceGivesInitialTest(operation);
    }

    @Test
    public void runningSouthOperationFourTimesGivesInitialTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH, new FakeStargate());
        vectorOperationTester.runningOperationFourTimesGivesInitialTest(operation);
    }

    @Test
    public void runningNorthOperationFourTimesGivesInitialTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH, new FakeStargate());
        vectorOperationTester.runningOperationFourTimesGivesInitialTest(operation);
    }

    @Test
    public void rotateEastFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.EAST, new FakeStargate());
        vectorOperationTester.flipTest(operation);
    }

    @Test
    public void rotateWestFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST, new FakeStargate());
        vectorOperationTester.flipTest(operation);
    }

    @Test
    public void rotateNorthFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH, new FakeStargate());
        vectorOperationTester.flipTest(operation);
    }

    @Test
    public void rotateSouthFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH, new FakeStargate());
        vectorOperationTester.flipTest(operation);
    }

}
