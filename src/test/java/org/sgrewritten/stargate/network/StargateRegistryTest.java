package org.sgrewritten.stargate.network;

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
import org.sgrewritten.stargate.api.BlockHandlerInterfaceMock;
import org.sgrewritten.stargate.api.PositionType;
import org.sgrewritten.stargate.api.Priority;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.util.StorageMock;
import org.sgrewritten.stargate.util.portal.GateTestHelper;

class StargateRegistryTest {

    private StargateRegistry registry;
    private Network network;
    private Network personalNetwork;
    private WorldMock world;
    private PlayerMock player;

    @BeforeEach
    void setUp() throws NameLengthException, NameConflictException, InvalidNameException, UnimplementedFlagException {
        ServerMock server = MockBukkit.mock();
        GateTestHelper.setUpGates();
        this.world = server.addSimpleWorld("world");
        this.player = server.addPlayer();
        registry = new StargateRegistry(new StorageMock());
        network = registry.createNetwork("network", NetworkType.CUSTOM, false, false);
        personalNetwork = registry.createNetwork(player.getUniqueId().toString(), NetworkType.PERSONAL, false, false);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void rename_Custom() throws InvalidNameException {
        registry.rename(network);
        Assertions.assertEquals("network1", network.getId());
    }

    @Test
    void rename_Personal() {
        Assertions.assertThrows(InvalidNameException.class, () -> registry.rename(personalNetwork));
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacementUnregisterPlacement_matchingMaterial(Priority priority)
            throws TranslatableException, InvalidStructureException {
        Material testMaterial = Material.END_GATEWAY;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        RealPortal portal = FakePortalGenerator.generateFakePortal(this.world, network, "test", true);
        // TODO add testflag to portal
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, testMaterial,
                plugin, priority, testFlag);
        registry.addBlockHandlerInterface(blockHandler);
        registry.registerPlacement(location, portal, testMaterial, player);
        Assertions.assertTrue(blockHandler.blockIsRegistered(location, player, portal));
        registry.registerRemoval(location, portal, testMaterial, player);
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
    }
    
    @ParameterizedTest
    @EnumSource(Priority.class)
    void registerPlacementUnregisterPlacement_materialMissmatch(Priority priority)
            throws TranslatableException, InvalidStructureException {
        Material handlerMaterial = Material.END_GATEWAY;
        Material placedMaterial = Material.DIRT;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        RealPortal portal = FakePortalGenerator.generateFakePortal(this.world, network, "test", true);
        // TODO add testflag to portal
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, handlerMaterial,
                plugin, priority, testFlag);
        registry.addBlockHandlerInterface(blockHandler);
        registry.registerPlacement(location, portal, placedMaterial, player);
        Assertions.assertFalse(blockHandler.blockIsRegistered(location, player, portal));
    }
    
    @Test
    void registerPlacementUnregisterPlacement_materialMissmatch()
            throws TranslatableException, InvalidStructureException {
        Material placedMaterial = Material.END_GATEWAY;
        Character testFlag = 'c';
        Plugin plugin = MockBukkit.createMockPlugin("Test");
        RealPortal portal = FakePortalGenerator.generateFakePortal(this.world, network, "test", true);
        // TODO add testflag to portal
        Location location = new Location(world, 0, 0, 0);
        BlockHandlerInterfaceMock highPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.HIGH, testFlag);
        BlockHandlerInterfaceMock lowPriority = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                plugin, Priority.LOWEST, testFlag);
        registry.addBlockHandlerInterface(highPriority);
        registry.addBlockHandlerInterface(lowPriority);
        registry.registerPlacement(location, portal, placedMaterial, player);
        Assertions.assertTrue(highPriority.blockIsRegistered(location, player, portal));
        Assertions.assertFalse(lowPriority.blockIsRegistered(location, player, portal));
    }
    
}
