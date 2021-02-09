package net.knarcraft.stargate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Material;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BlockLocationTest {
    private WorldMock mockWorld;

    @Before
    public void setUp() {
        mockWorld = new WorldMock(Material.DIRT, 5);
    }

    @After
    public void tearDown()
    {
        MockBukkit.unmock();
    }

    @Test
    public void equalsTest() {
        BlockLocation location1 = new BlockLocation(mockWorld, 1, 3, 4);
        BlockLocation location2 = new BlockLocation(mockWorld, 1, 3, 4);
        assertEquals(location1, location2);
    }

    @Test
    public void notEqualsTest1() {
        BlockLocation location1 = new BlockLocation(mockWorld, 1, 3, 4);
        BlockLocation location2 = new BlockLocation(mockWorld, 2, 3, 4);
        assertNotEquals(location1, location2);
    }

    @Test
    public void notEqualsTest2() {
        BlockLocation location1 = new BlockLocation(mockWorld, 1, 3, 4);
        BlockLocation location2 = new BlockLocation(mockWorld, 1, 5, 4);
        assertNotEquals(location1, location2);
    }

    @Test
    public void notEqualsTest3() {
        BlockLocation location1 = new BlockLocation(mockWorld, 1, 3, 4);
        BlockLocation location2 = new BlockLocation(mockWorld, 1, 3, 7);
        assertNotEquals(location1, location2);
    }

    @Test
    public void notEqualsTest4() {
        BlockLocation location1 = new BlockLocation(mockWorld, 1, 3, 4);
        BlockLocation location2 = new BlockLocation(new WorldMock(Material.DIRT, 4), 1, 3, 4);
        assertNotEquals(location1, location2);
    }

}
