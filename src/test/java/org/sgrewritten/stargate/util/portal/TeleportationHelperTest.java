package org.sgrewritten.stargate.util.portal;

import be.seeseemelk.mockbukkit.MockBukkitInject;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BlockVector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.StargateInject;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.gate.ExplicitGateBuilder;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.TestPortalBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(StargateExtension.class)
class TeleportationHelperTest {

    private static WorldMock world;
    private static StargateNetwork network;
    @MockBukkitInject
    private ServerMock server;
    private StargateAPI stargateAPI;

    @BeforeEach
    public void setUp() throws NameLengthException, InvalidNameException, UnimplementedFlagException {
        world = server.addSimpleWorld("world");
        this.stargateAPI = new StargateAPIMock();
        network = new StargateNetwork("network", NetworkType.CUSTOM, StorageType.LOCAL);
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
        Location center = new Location(world, 0, 5, 0);
        Assertions.assertTrue(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_Floating() {
        Location center = new Location(world, 0, 10, 0);
        Assertions.assertFalse(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_YBlocked() {
        Location center = new Location(world, 0, 0, 0);
        Assertions.assertFalse(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_XZBlocked() {
        Location center = new Location(world, 10, 5, 10);
        center.getBlock().setType(Material.OAK_PLANKS);
        Assertions.assertFalse(TeleportationHelper.isViableSpawnLocation(2, 2, center));
    }

    @Test
    void isViableSpawnLocationTest_ViableHole() {
        Location center = new Location(world, 0.5, 5, 0.5);
        new Location(world, 1, 5, 0).getBlock().setType(Material.OAK_PLANKS);
        new Location(world, 0, 5, 1).getBlock().setType(Material.OAK_PLANKS);
        new Location(world, -1, 5, 0).getBlock().setType(Material.OAK_PLANKS);
        new Location(world, 0, 5, -1).getBlock().setType(Material.OAK_PLANKS);
        Assertions.assertTrue(TeleportationHelper.isViableSpawnLocation(1, 1, center));
    }

    @Test
    void getDirectionalConeLayerTest() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Location topLeft = new Location(world, -0, 7, -3);
        List<Location> irisLocations = new ArrayList<>();
        RealPortal portal = generatePortal(topLeft);
        portal.getGate().getLocations(GateStructureType.IRIS).forEach(
                (blockLocation) -> irisLocations.add(blockLocation.getLocation()));

        BlockVector forward = portal.getExitFacing().getDirection().toBlockVector();
        BlockVector left = forward.clone().rotateAroundY(Math.PI / 2).toBlockVector();
        BlockVector right = forward.clone().rotateAroundY(-Math.PI / 2).toBlockVector();
        BlockVector up = new BlockVector(0, 1, 0);
        BlockVector down = new BlockVector(0, -1, 0);
        List<Location> locations = TeleportationHelper.getDirectionalConeLayer(irisLocations, forward, left, right, up, down, 0, portal.getGate().getExit());
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
    void findViableSpawnLocationTest_NotViable() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Location topLeft = new Location(world, -1, 20, -3);
        RealPortal portal = generatePortal(topLeft);
        Location location = TeleportationHelper.findViableSpawnLocation(world.spawnEntity(topLeft, EntityType.BAT), portal);
        Assertions.assertNull(location);
    }

    @Test
    void findViableSpawnLocationTest_Viable() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Location topLeft = new Location(world, -1, 5, -3);
        RealPortal portal = generatePortal(topLeft);
        Location location = TeleportationHelper.findViableSpawnLocation(world.spawnEntity(topLeft, EntityType.BAT), portal);
        assertNotNull(location);
    }

    @Test
    void findViableSpawnLocationTest_Backwards() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Location topLeft = new Location(world, -1, 5, -3);
        Set<StargateFlag> flags = new HashSet<>();
        flags.add(StargateFlag.BACKWARDS);
        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(),world).setNetwork(network);
        RealPortal portal = testPortalBuilder.setFlags(flags).build();
        Location location = TeleportationHelper.findViableSpawnLocation(world.spawnEntity(topLeft, EntityType.BAT), portal);
        assertNotNull(location);
        Assertions.assertTrue(topLeft.getX() < location.getX());
    }

    private RealPortal generatePortal(Location topLeft) throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(), world);
        ExplicitGateBuilder explicitGateBuilder = new ExplicitGateBuilder(stargateAPI.getRegistry(),topLeft, GateFormatRegistry.getFormat("nether.gate"));
        explicitGateBuilder.setFacing(BlockFace.EAST);
        testPortalBuilder.setName("aName").setGateBuilder(explicitGateBuilder);
        testPortalBuilder.setNetwork(network);
        return testPortalBuilder.build();
    }
}
