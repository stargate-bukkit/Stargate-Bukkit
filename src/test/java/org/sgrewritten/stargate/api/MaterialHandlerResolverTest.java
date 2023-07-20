package org.sgrewritten.stargate.api;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.PortalFactory;
import org.sgrewritten.stargate.database.StorageMock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialHandlerResolverTest {

    private ServerMock server;
    private MaterialHandlerResolver materialHandlerResolver;
    private RegistryAPI registry;
    private StorageMock storage;
    private PlayerMock player;
    private WorldMock world;
    private Network network;

    @BeforeEach
    void setUp() throws InvalidNameException, UnimplementedFlagException, NameLengthException, NameConflictException {
        this.server = MockBukkit.mock();
        this.storage = new StorageMock();
        this.registry = new StargateRegistry(storage);
        this.materialHandlerResolver = new MaterialHandlerResolver(registry,storage);
        this.player = server.addPlayer();
        this.world = server.addSimpleWorld("world");
        this.network = registry.createNetwork("network", NetworkType.CUSTOM,false,false);
    }

    @AfterEach
    void tearDown(){
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacementUnregisterPlacement_matchingMaterial(Priority priority)
            throws TranslatableException, InvalidStructureException {
        Material testMaterial = Material.END_GATEWAY;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location locaton = new Location(world,0,0,0);
        Set<Character> flags = new HashSet<>();
        flags.add(testFlag);
        RealPortal portal = PortalFactory.generateFakePortal(locaton, network, "test", true, new HashSet<>(),flags, registry);
        // TODO add testflag to portal
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, testMaterial,
                plugin, priority, testFlag);
        materialHandlerResolver.addBlockHandlerInterface(blockHandler);
        materialHandlerResolver.registerPlacement(location, List.of(portal), testMaterial, player);
        Assertions.assertTrue(blockHandler.blockIsRegistered(location, player, portal));
        materialHandlerResolver.registerRemoval(location, portal, testMaterial, player);
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacement_materialMismatch(Priority priority)
            throws TranslatableException, InvalidStructureException {
        Material handlerMaterial = Material.END_GATEWAY;
        Material placedMaterial = Material.DIRT;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location locaton = new Location(world,0,0,0);
        Set<Character> flags = new HashSet<>();
        flags.add(testFlag);
        RealPortal portal = PortalFactory.generateFakePortal(locaton, network, "test", true, new HashSet<>(),flags, registry);
        // TODO add testflag to portal
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, handlerMaterial,
                plugin, priority, testFlag);
        materialHandlerResolver.addBlockHandlerInterface(blockHandler);
        materialHandlerResolver.registerPlacement(location, List.of(portal), placedMaterial, player);
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
    }

    @Test
    void registerPlacement_priorityCheck()
            throws TranslatableException, InvalidStructureException {
        Material placedMaterial = Material.END_GATEWAY;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location locaton = new Location(world,0,0,0);
        Set<Character> flags = new HashSet<>();
        flags.add(testFlag);
        RealPortal portal = PortalFactory.generateFakePortal(locaton, network, "test", true, new HashSet<>(), flags, registry);
        // TODO add testflag to portal
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock highPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.HIGH, testFlag);
        BlockHandlerInterfaceMock lowPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.LOWEST, testFlag);
        materialHandlerResolver.addBlockHandlerInterface(highPriority);
        materialHandlerResolver.addBlockHandlerInterface(lowPriority);
        materialHandlerResolver.registerPlacement(location, List.of(portal), placedMaterial, player);
        Assertions.assertTrue(highPriority.blockIsRegistered(location, player, portal));
        Assertions.assertFalse(lowPriority.blockIsRegistered(location, player, portal));
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacement_wrongFlag(Priority priority)
            throws TranslatableException, InvalidStructureException {
        Material handlerMaterial = Material.END_GATEWAY;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location locaton = new Location(world,0,0,0);
        Set<Character> flags = new HashSet<>();
        flags.add('d');
        RealPortal portal = PortalFactory.generateFakePortal(locaton, network, "test", true, new HashSet<>(), flags, registry);
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, handlerMaterial,
                plugin, priority, testFlag);
        materialHandlerResolver.addBlockHandlerInterface(blockHandler);
        materialHandlerResolver.registerPlacement(location, List.of(portal), handlerMaterial, player);
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
    }

    void registerPlacement_rejected() throws InvalidStructureException, NameLengthException {
        Material placedMaterial = Material.END_GATEWAY;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        Location locaton = new Location(world,0,0,0);
        Set<Character> flags = new HashSet<>();
        flags.add(testFlag);
        RealPortal portal = PortalFactory.generateFakePortal(locaton, network, "test", true, new HashSet<>(), flags, registry);
        // TODO add testflag to portal
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock highPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.HIGH, testFlag);
        BlockHandlerInterfaceMock lowPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.LOWEST, testFlag);
        materialHandlerResolver.addBlockHandlerInterface(highPriority);
        materialHandlerResolver.addBlockHandlerInterface(lowPriority);
        highPriority.setRegisterPlacedBlock(false);
        materialHandlerResolver.registerPlacement(location, List.of(portal), placedMaterial, player);
        Assertions.assertFalse(highPriority.blockIsRegistered(location, player, portal));
        Assertions.assertTrue(lowPriority.blockIsRegistered(location, player, portal));
    }
}
