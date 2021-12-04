package net.TheDgtl.Stargate.vectorlogic;

import net.TheDgtl.Stargate.exception.InvalidStructureException;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleVectorOperationTest {

    private static VectorOperationTester vectorOperationTester;

    @BeforeAll
    public static void setUp() {
        List<Vector> testVectors = new ArrayList<>();
        Random random = new Random();
        int maxBound = 20;
        for (int i = 0; i < 10000; i++) {
            testVectors.add(new Vector(random.nextInt(maxBound), -random.nextInt(maxBound), random.nextInt(maxBound)));
        }
        vectorOperationTester = new VectorOperationTester(testVectors);
    }

    @Test
    public void noRotationForEastTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.EAST);
        vectorOperationTester.noRotationForEastTest(operation);
    }

    @Test
    public void originalVectorNotModifiedTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.originalVectorNotModifiedTest(operation);
    }

    @Test
    public void rotateSouthTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.rotateSouthTest(operation);
    }

    @Test
    public void rotateWestTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.WEST);
        vectorOperationTester.rotateWestTest(operation);
    }

    @Test
    public void rotateNorthTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.NORTH);
        vectorOperationTester.rotateNorthTest(operation);
    }

    @Test
    public void rotateUpTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.UP);
        vectorOperationTester.rotateUpTest(operation);
    }

    @Test
    public void rotateDownTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.DOWN);
        vectorOperationTester.rotateDownTest(operation);
    }

    @Test
    public void rotateEastInverseTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.EAST);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void rotateWestInverseTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.WEST);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void rotateSouthInverseTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void rotateNorthInverseTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.NORTH);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    public void runningWestOperationTwiceGivesInitialTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.WEST);
        vectorOperationTester.runningOperationTwiceGivesInitialTest(operation);
    }

    @Test
    public void runningSouthOperationFourTimesGivesInitialTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.runningOperationFourTimesGivesInitialTest(operation);
    }

    @Test
    public void runningNorthOperationFourTimesGivesInitialTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.NORTH);
        vectorOperationTester.runningOperationFourTimesGivesInitialTest(operation);
    }

    @Test
    public void rotateEastFlipZTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.EAST);
        vectorOperationTester.flipTest(operation);
    }

    @Test
    public void rotateWestFlipZTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.WEST);
        vectorOperationTester.flipTest(operation);
    }

    @Test
    public void rotateNorthFlipZTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.NORTH);
        vectorOperationTester.flipTest(operation);
    }

    @Test
    public void rotateSouthFlipZTest() throws InvalidStructureException {
        IVectorOperation operation = new SimpleVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.flipTest(operation);
    }

}
