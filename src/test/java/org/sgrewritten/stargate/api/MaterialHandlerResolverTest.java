package org.sgrewritten.stargate.api;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.PortalBuilder;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryMock;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.TestPortalBuilder;
import org.sgrewritten.stargate.util.StargateTestHelper;

import java.util.List;
import java.util.Set;

class MaterialHandlerResolverTest {

    private ServerMock server;
    private BlockHandlerResolver blockHandlerResolver;
    private RegistryMock registry;
    private StorageMock storage;
    private PlayerMock player;
    private WorldMock world;
    private Network network;
    private NetworkManager networkManager;
    private StargateAPIMock stargateAPI;

    @BeforeEach
    void setUp() throws InvalidNameException, UnimplementedFlagException, NameLengthException, NameConflictException {
        this.server = StargateTestHelper.setup();
        this.storage = new StorageMock();
        this.blockHandlerResolver = new BlockHandlerResolver(storage);
        this.registry = new RegistryMock(storage, blockHandlerResolver);
        this.stargateAPI = new StargateAPIMock(blockHandlerResolver, storage,registry);
        this.networkManager = stargateAPI.getNetworkManager();
        this.player = server.addPlayer();
        this.world = server.addSimpleWorld("world");
        this.network = networkManager.createNetwork("network", NetworkType.CUSTOM, StorageType.LOCAL, false);
    }

    @AfterEach
    void tearDown() {
        StargateTestHelper.tearDown();
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacementUnregisterPlacement_matchingMaterial(Priority priority)
            throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Material testMaterial = Material.END_GATEWAY;
        Character testFlag = 'C';
        blockHandlerResolver.registerCustomFlag(testFlag);
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location location = new Location(world, 0, 0, 0);
        Set<Character> flags = Set.of(testFlag);

        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(),world);
        testPortalBuilder.setNetwork(network).setUnrecognisedFlags(flags).setName("test");
        RealPortal portal = testPortalBuilder.build();
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, testMaterial,
                plugin, priority, testFlag);
        BlockVector positionVector = portal.getGate().getRelativeVector(location).toBlockVector();
        blockHandlerResolver.addBlockHandlerInterface(blockHandler);
        blockHandlerResolver.registerPlacement(registry, location, List.of(portal), testMaterial, player);
        StargateTestHelper.runAllTasks();
        Assertions.assertTrue(blockHandler.blockIsRegistered(location, player, portal));
        Assertions.assertNotNull(registry.getNextRegisteredPortalPosition());
        Assertions.assertEquals(storage.getNextAddedPortalPosition().getThirdValue().getRelativePositionLocation(), positionVector);
        blockHandlerResolver.registerRemoval(registry, location, portal);
        StargateTestHelper.runAllTasks();
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
        Assertions.assertEquals(registry.getNextRemovedPortalPosition(), new BlockLocation(location));
        Assertions.assertEquals(storage.getNextRemovedPortalPosition().getThirdValue().getRelativePositionLocation(), positionVector);
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacement_materialMismatch(Priority priority)
            throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Material handlerMaterial = Material.END_GATEWAY;
        Material placedMaterial = Material.DIRT;
        char testFlag = 'C';
        blockHandlerResolver.registerCustomFlag(testFlag);
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location location = new Location(world, 0, 0, 0);
        Set<Character> flags = Set.of(testFlag);
        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(),world);
        testPortalBuilder.setNetwork(network).setUnrecognisedFlags(flags).setName("test");
        RealPortal portal = testPortalBuilder.build();
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, handlerMaterial,
                plugin, priority, testFlag);
        blockHandlerResolver.addBlockHandlerInterface(blockHandler);
        blockHandlerResolver.registerPlacement(registry, location, List.of(portal), placedMaterial, player);
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
        Assertions.assertNull(registry.getNextRegisteredPortalPosition());
        Assertions.assertNull(storage.getNextAddedPortalPosition());
    }

    @Test
    void registerPlacement_priorityCheck()
            throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Material placedMaterial = Material.END_GATEWAY;
        char testFlag = 'C';
        blockHandlerResolver.registerCustomFlag(testFlag);
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location location = new Location(world, 0, 0, 0);
        Set<Character> flags = Set.of(testFlag);
        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(), world);
        testPortalBuilder.setName("test").setNetwork(network).setUnrecognisedFlags(flags);
        RealPortal portal = testPortalBuilder.build();
        BlockHandlerInterfaceMock highPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.HIGH, testFlag);
        BlockHandlerInterfaceMock lowPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.LOWEST, testFlag);
        blockHandlerResolver.addBlockHandlerInterface(highPriority);
        blockHandlerResolver.addBlockHandlerInterface(lowPriority);
        blockHandlerResolver.registerPlacement(registry, location, List.of(portal), placedMaterial, player);
        Assertions.assertTrue(highPriority.blockIsRegistered(location, player, portal));
        Assertions.assertFalse(lowPriority.blockIsRegistered(location, player, portal));
        Assertions.assertNotNull(registry.getNextRegisteredPortalPosition());
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacement_wrongFlag(Priority priority)
            throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Material handlerMaterial = Material.END_GATEWAY;
        char testFlag = 'C';
        blockHandlerResolver.registerCustomFlag(testFlag);
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location location = new Location(world, 0, 0, 0);
        Set<Character> flags = Set.of('D');
        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(), world);
        testPortalBuilder.setNetwork(network).setUnrecognisedFlags(flags).setName("test");
        RealPortal portal = testPortalBuilder.build();
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, handlerMaterial,
                plugin, priority, testFlag);
        BlockVector positionVector = portal.getGate().getRelativeVector(location).toBlockVector();
        blockHandlerResolver.addBlockHandlerInterface(blockHandler);
        blockHandlerResolver.registerPlacement(registry, location, List.of(portal), handlerMaterial, player);
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
        Assertions.assertNull(registry.getNextRegisteredPortalPosition());
        Assertions.assertNull(storage.getNextAddedPortalPosition());
    }

    @Test
    void registerPlacement_rejected() throws InvalidStructureException, TranslatableException, GateConflictException, NoFormatFoundException {
        Material placedMaterial = Material.END_GATEWAY;
        Character testFlag = 'C';
        blockHandlerResolver.registerCustomFlag(testFlag);
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location location = new Location(world, 0, 0, 0);
        Set<Character> flags = Set.of(testFlag);
        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(), world);
        testPortalBuilder.setNetwork(network).setUnrecognisedFlags(flags);
        RealPortal portal = testPortalBuilder.build();
        BlockHandlerInterfaceMock highPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.HIGH, testFlag);
        BlockHandlerInterfaceMock lowPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.LOWEST, testFlag);
        BlockVector positionVector = portal.getGate().getRelativeVector(location).toBlockVector();
        blockHandlerResolver.addBlockHandlerInterface(highPriority);
        blockHandlerResolver.addBlockHandlerInterface(lowPriority);
        highPriority.setRegisterPlacedBlock(false);
        blockHandlerResolver.registerPlacement(registry, location, List.of(portal), placedMaterial, player);
        StargateTestHelper.runAllTasks();
        Assertions.assertFalse(highPriority.blockIsRegistered(location, player, portal));
        Assertions.assertTrue(lowPriority.blockIsRegistered(location, player, portal));
        Assertions.assertNotNull(registry.getNextRegisteredPortalPosition());
        Assertions.assertEquals(storage.getNextAddedPortalPosition().getThirdValue().getRelativePositionLocation(), positionVector);
    }
}
