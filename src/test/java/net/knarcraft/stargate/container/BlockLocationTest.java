package net.knarcraft.stargate.container;

import be.seeseemelk.mockbukkit.WorldMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BlockLocationTest {

    @Test
    public void makeRelativeBlockLocationTest() {
        WorldMock world = new WorldMock();
        BlockLocation startLocation = new BlockLocation(world, 5, 4, 3);

        //Move to some other, different location
        BlockLocation relativeLocation = startLocation.makeRelativeBlockLocation(4, 6, 8);
        Assertions.assertNotEquals(startLocation, relativeLocation);

        //Move back to make sure we can go back to where we started by going in the opposite direction
        BlockLocation sameAsStartLocation = relativeLocation.makeRelativeBlockLocation(-4, -6, -8);
        Assertions.assertEquals(startLocation, sameAsStartLocation);
    }

    @Test
    public void getRelativeLocationTest() {
        WorldMock world = new WorldMock();
        BlockLocation startLocation = new BlockLocation(world, 7, 3, 6);

        RelativeBlockVector relativeBlockVector = new RelativeBlockVector(2, 1, 3);
        BlockLocation relativeLocation1 = startLocation.getRelativeLocation(relativeBlockVector, 0);
        //With yaw = 0, going right goes in the x direction, and out goes in the z direction, while y is decremented
        BlockLocation targetLocation1 = new BlockLocation(world, 9, 2, 9);
        Assertions.assertEquals(targetLocation1, relativeLocation1);

        BlockLocation relativeLocation2 = startLocation.getRelativeLocation(relativeBlockVector, 90);
        //With yaw = 90, going right goes in the z direction, and out goes in the -x direction, while y is decremented
        BlockLocation targetLocation2 = new BlockLocation(world, 4, 2, 8);
        Assertions.assertEquals(targetLocation2, relativeLocation2);

        BlockLocation relativeLocation3 = startLocation.getRelativeLocation(relativeBlockVector, 180);
        //With yaw = 180, going right goes in the -x direction, and out goes in the -z direction, while y is decremented
        BlockLocation targetLocation3 = new BlockLocation(world, 5, 2, 3);
        Assertions.assertEquals(targetLocation3, relativeLocation3);

        BlockLocation relativeLocation4 = startLocation.getRelativeLocation(relativeBlockVector, 270);
        //With yaw = 270, going right goes in the -z direction, and out goes in the x direction, while y is decremented
        BlockLocation targetLocation4 = new BlockLocation(world, 10, 2, 4);
        Assertions.assertEquals(targetLocation4, relativeLocation4);
    }

}
