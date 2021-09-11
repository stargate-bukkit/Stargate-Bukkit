package net.knarcraft.stargate.utility;

import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DirectionHelperTest {

    @Test
    public void getYawFromLocationTest() {
        World world = new WorldMock();
        Location location1 = new Location(world, 100, 0, 100);
        Location location2 = new Location(world, 100, 0, 101);

        double yaw = DirectionHelper.getYawFromLocationDifference(location1, location2);
        Assertions.assertEquals(0, yaw);

        location2 = new Location(world, 100, 0, 99);
        yaw = DirectionHelper.getYawFromLocationDifference(location1, location2);
        Assertions.assertEquals(180, yaw);

        location2 = new Location(world, 101, 0, 100);
        yaw = DirectionHelper.getYawFromLocationDifference(location1, location2);
        Assertions.assertEquals(270, yaw);

        location2 = new Location(world, 99, 0, 100);
        yaw = DirectionHelper.getYawFromLocationDifference(location1, location2);
        Assertions.assertEquals(90, yaw);
    }

}
