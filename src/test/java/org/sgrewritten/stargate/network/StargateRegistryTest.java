package org.sgrewritten.stargate.network;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockBukkitInject;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;
import org.sgrewritten.stargate.util.StargateTestHelper;
import org.sgrewritten.stargate.util.portal.PortalMock;

@ExtendWith(StargateExtension.class)
class StargateRegistryTest {

    private StargateRegistry registry;
    private Network network;
    private Network personalNetwork;
    private WorldMock world;
    private PlayerMock player;
    private StorageMock storageMock;
    private StargateNetworkManager networkManager;
    @MockBukkitInject
    ServerMock server;

    @BeforeEach
    void setUp() throws NameLengthException, NameConflictException, InvalidNameException, UnimplementedFlagException {
        this.world = server.addSimpleWorld("world");
        this.player = server.addPlayer();
        this.storageMock = new StorageMock();
        registry = new StargateRegistry(storageMock, new BlockHandlerResolver(storageMock));
        this.networkManager = new StargateNetworkManager(registry, storageMock);
        network = networkManager.createNetwork("network", NetworkType.CUSTOM, StorageType.LOCAL, false);
        personalNetwork = networkManager.createNetwork(player.getUniqueId().toString(), NetworkType.PERSONAL, StorageType.LOCAL, false);
    }

    @Test
    void getValidNewName() throws InvalidNameException {
        Assertions.assertEquals("network1", registry.getValidNewName(network));
    }

    @Test
    void rename_Personal() {
        Assertions.assertThrows(InvalidNameException.class, () -> registry.getValidNewName(personalNetwork));
    }

    @ParameterizedTest
    @EnumSource
    void registerUnregisterPosition(GateStructureType type) {
        PortalMock portal = new PortalMock();
        Location location = new Location(world, 0, 0, 0);
        BlockLocation blockLocation = new BlockLocation(location);
        registry.registerLocation(type, blockLocation, portal);
        Assertions.assertEquals(registry.getPortal(location), portal);
        registry.unRegisterLocation(type, blockLocation);
        Assertions.assertNull(registry.getPortal(location));
    }

    @ParameterizedTest
    @EnumSource
    void registerPortalPosition(PositionType type) {
        PortalPosition portalPosition = new PortalPosition(type, new BlockVector(1, 1, 1), "Stargate");
        Location location = new Location(world, 0, 0, 0);
        PortalMock portal = new PortalMock();
        this.registry.registerPortalPosition(portalPosition, location, portal);
        Assertions.assertNotNull(this.registry.getPortalPosition(location));
    }

    @ParameterizedTest
    @EnumSource
    void saveDeletePortalPosition(PositionType type) {
        Location location = new Location(world, 0, 0, 0);
        PortalMock portal = new PortalMock();
        Plugin plugin = MockBukkit.createMockPlugin("Stargate");
        PortalPosition portalPosition = registry.savePortalPosition(portal, location, type, plugin);
        registry.registerPortalPosition(portalPosition, location, portal);
        StargateTestHelper.runAllTasks();
        Assertions.assertNotNull(storageMock.getNextAddedPortalPosition());
        registry.removePortalPosition(location);
        StargateTestHelper.runAllTasks();
        Assertions.assertNotNull(storageMock.getNextRemovedPortalPosition());
    }

    @Test
    void getPortalPosition_notFound() {
        Location location = new Location(world, 0, 0, 0);
        Assertions.assertNull(registry.getPortalPosition(location));
    }

    @Test
    void removePortalPosition_doesNotExist() {
        Assertions.assertDoesNotThrow(() -> registry.removePortalPosition(new Location(world, 0, 0, 0)));
    }

    @ParameterizedTest
    @EnumSource
    void getPortalFromPortalPosition(PositionType type) {
        PortalPosition portalPosition = new PortalPosition(type, new BlockVector(1, 1, 1), "Stargate");
        Location location = new Location(world, 0, 0, 0);
        PortalMock portal = new PortalMock();
        this.registry.registerPortalPosition(portalPosition, location, portal);
        Assertions.assertEquals(portal, portalPosition.getPortal());
    }
}
