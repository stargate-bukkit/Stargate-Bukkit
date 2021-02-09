package net.knarcraft.stargate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Material;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BlockLocationTest {
    private WorldMock mockWorld;

    @BeforeEach
    public void setUp() {
        mockWorld = new WorldMock(Material.DIRT, 5);
    }

    @After
    public void tearDown() {
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

    @Test
    public void makeRelativeTest() {
        BlockLocation location = new BlockLocation(mockWorld, 3, 7, 19);
        BlockLocation newLocation = location.makeRelative(34, 65, 75);
        assertEquals(37, newLocation.getBlockX());
        assertEquals(72, newLocation.getBlockY());
        assertEquals(94, newLocation.getBlockZ());
    }

    @Test
    public void materialTest() {
        BlockLocation location = new BlockLocation(mockWorld, 0, 0, 0);
        assertNotEquals(Material.BOOKSHELF, location.getType());
        location.setType(Material.BOOKSHELF);
        assertEquals(Material.BOOKSHELF, location.getType());
    }

    @Test
    public void toStringTest() {
        BlockLocation location = new BlockLocation(mockWorld, 56, 87, 34);
        assertEquals("56,87,34", location.toString());
    }

}
