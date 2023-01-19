package org.sgrewritten.stargate.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.util.FakeStorage;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

class StargateRegistryTest {

    private StargateRegistry registry;
    private Network network;
    private ServerMock server;
    private PlayerMock player;
    private Network personalNetwork;

    @BeforeEach
    void setUp() throws NameLengthException, NameConflictException, InvalidNameException, StorageWriteException {
        server = MockBukkit.mock();
        player = server.addPlayer();
        registry = new StargateRegistry(new FakeStorage());
        network = registry.createNetwork("network", NetworkType.CUSTOM, false, false);
        personalNetwork = registry.createNetwork(player.getUniqueId().toString(), NetworkType.PERSONAL, false, false);
    }
    
    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void rename_Custom() throws InvalidNameException, StorageWriteException {
        registry.rename(network);
        Assertions.assertEquals("network1", network.getId());
    }
    
    @Test
    void rename_Personal(){
        Assertions.assertThrows(InvalidNameException.class, () -> registry.rename(personalNetwork));
    }
}
