package org.sgrewritten.stargate.util.portal;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BlockVector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryMock;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.PortalFactory;
import org.sgrewritten.stargate.util.StargateTestHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TeleportationHelperTest {

    private static WorldMock world;
    private static StargateNetwork network;

    @BeforeAll
    public static void setUp() throws NameLengthException, InvalidNameException, UnimplementedFlagException {
        ServerMock server = StargateTestHelper.setup();
        world = server.addSimpleWorld("world");
        network = new StargateNetwork("network", NetworkType.CUSTOM, StorageType.LOCAL);
    }

    @AfterAll
    public static void tearDown() {
        StargateTestHelper.tearDown();
    }

    @Test
    void getFloorLocationsTest() {
        Location corner = new Location(world, 0, 10, 0);
        List<Location> locations = TeleportationHelper.getFloorLocations(2, corner);
        Location[] expectedLocations = {new Location(world, 0, 9, 0), new Location(world, 1, 9, 0), new Location(world, 0, 9, 1), new Location(world, 1, 9, 1)};
        for (Location expectedLocation : expectedLocations) {
            Assertions.assertTrue(locations.contains(expectedLocation), "Missing location " + expectedLocation);
        }
        Assertions.assertEquals(4, locations.size());
    }

    @Test
    void getOccupiedLocationsTest() {
        Location corner = new Location(world, 0, 10, 0);
        List<Location> locations = TeleportationHelper.getOccupiedLocations(2, 2, corner);
        Location[] expectedLocations = {new Location(world, 0, 10, 0), new Location(world, 1, 10, 0),
                new Location(world, 0, 10, 1), new Location(world, 1, 10, 1), new Location(world, 0, 11, 0),
                new Location(world, 1, 11, 0), new Location(world, 0, 11, 1), new Location(world, 1, 11, 1)};
        for (Location expectedLocation : expectedLocations) {
            Assertions.assertTrue(locations.contains(expectedLocation), "Missing location " + expectedLocation);
        }
        Assertions.assertEquals(8, locations.size());
    }

    @Test
    void isViableSpawnLocationTest_Viable() {
        Location center = new Location(world, 0, 1, 0);
        Assertions.assertTrue(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_Floating() {
        Location center = new Location(world, 0, 3, 0);
        Assertions.assertFalse(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_YBlocked() {
        Location center = new Location(world, 0, 0, 0);
        Assertions.assertFalse(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_XZBlocked() {
        Location center = new Location(world, 10, 1, 10);
        center.getBlock().setType(Material.OAK_PLANKS);
        Assertions.assertFalse(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_ViableHole() {
        Location center = new Location(world, -9.5, 1, -9.5);
        new Location(world, -8, 1, -9).getBlock().setType(Material.OAK_PLANKS);
        new Location(world, -10, 1, -9).getBlock().setType(Material.OAK_PLANKS);
        new Location(world, -9, 1, -8).getBlock().setType(Material.OAK_PLANKS);
        new Location(world, -9, 1, -10).getBlock().setType(Material.OAK_PLANKS);
        Assertions.assertTrue(TeleportationHelper.isViableSpawnLocation(1, 1, center));
    }

    @Test
    void getDirectionalConeLayerTest() throws NameLengthException, InvalidStructureException {
        Location topLeft = new Location(world, -0, 7, -3);
        RealPortal fakePortal = PortalFactory.generateFakePortal(topLeft, network, "aName", false, new HashSet<>(), new HashSet<>(), new RegistryMock());
        List<Location> irisLocations = new ArrayList<>();
        fakePortal.getGate().getLocations(GateStructureType.IRIS).forEach(
                (blockLocation) -> irisLocations.add(blockLocation.getLocation()));

        BlockVector forward = fakePortal.getExitFacing().getDirection().toBlockVector();
        BlockVector left = forward.clone().rotateAroundY(Math.PI / 2).toBlockVector();
        BlockVector right = forward.clone().rotateAroundY(-Math.PI / 2).toBlockVector();
        BlockVector up = new BlockVector(0, 1, 0);
        BlockVector down = new BlockVector(0, -1, 0);
        List<Location> locations = TeleportationHelper.getDirectionalConeLayer(irisLocations, forward, left, right, up, down, 0, fakePortal.getGate().getExit());
        Location[] someExpectedLocations = {
                new Location(world, -1, 7, -3),
                new Location(world, -1, 3, -3),
                new Location(world, -1, 7, -6),
                new Location(world, -1, 3, -6)
        };

        for (Location irisLocation : irisLocations) {
            Assertions.assertFalse(locations.contains(irisLocation), "Unexpected location " + irisLocation);
        }
        for (Location expectedLocation : someExpectedLocations) {
            Assertions.assertTrue(locations.contains(expectedLocation), "Missing location " + expectedLocation);
        }
        Assertions.assertEquals(20, locations.size());
    }

    @Test
    void findViableSpawnLocationTest_NotViable() throws NameLengthException, InvalidStructureException {
        Location topLeft = new Location(world, -1, 20, -3);
        RealPortal fakePortal = PortalFactory.generateFakePortal(topLeft, network, "aName", false, new HashSet<>(), new HashSet<>(), new RegistryMock());
        Location location = TeleportationHelper.findViableSpawnLocation(world.spawnEntity(topLeft, EntityType.BAT), fakePortal);
        Assertions.assertNull(location);
    }

    @Test
    void findViableSpawnLocationTest_Viable() throws NameLengthException, InvalidStructureException {
        Location topLeft = new Location(world, -1, 5, -3);
        RealPortal fakePortal = PortalFactory.generateFakePortal(topLeft, network, "aName", false, new HashSet<>(), new HashSet<>(), new RegistryMock());
        Location location = TeleportationHelper.findViableSpawnLocation(world.spawnEntity(topLeft, EntityType.BAT), fakePortal);
        assertNotNull(location);
    }

    @Test
    void findViableSpawnLocationTest_Backwards() throws NameLengthException, InvalidStructureException {
        Location topLeft = new Location(world, -1, 5, -3);
        Set<PortalFlag> flags = new HashSet<>();
        flags.add(PortalFlag.BACKWARDS);
        RealPortal fakePortal = PortalFactory.generateFakePortal(topLeft, network, "aName", false, flags, new HashSet<>(), new RegistryMock());
        Location location = TeleportationHelper.findViableSpawnLocation(world.spawnEntity(topLeft, EntityType.BAT), fakePortal);
        assertNotNull(location);
        Assertions.assertTrue(topLeft.getX() < location.getX());
    }
}
