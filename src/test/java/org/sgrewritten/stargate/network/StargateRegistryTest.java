package org.sgrewritten.stargate.network;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.structure.GateStructureType;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.gate.GateTestHelper;
import org.sgrewritten.stargate.util.portal.PortalMock;

class StargateRegistryTest {

    private StargateRegistry registry;
    private Network network;
    private Network personalNetwork;
    private WorldMock world;
    private PlayerMock player;
    private StorageMock storage;

    @BeforeEach
    void setUp() throws NameLengthException, NameConflictException, InvalidNameException, UnimplementedFlagException {
        ServerMock server = MockBukkit.mock();
        GateTestHelper.setUpGates();
        this.world = server.addSimpleWorld("world");
        this.player = server.addPlayer();
        this.storage = new StorageMock();
        registry = new StargateRegistry(storage);
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
    @EnumSource
    void registerUnregisterPosition(GateStructureType type){
        PortalMock portal = new PortalMock();
        Location location = new Location(world,0,0,0);
        BlockLocation blockLocation = new BlockLocation(location);
        registry.registerLocation(type,blockLocation, portal);
        Assertions.assertEquals(registry.getPortal(location), portal);
        registry.unRegisterLocation(type,blockLocation);
        Assertions.assertNull(registry.getPortal(location));
    }
}
