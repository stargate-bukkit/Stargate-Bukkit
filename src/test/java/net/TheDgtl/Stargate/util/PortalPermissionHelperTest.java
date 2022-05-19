package net.TheDgtl.Stargate.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

class PortalPermissionHelperTest {
    static MockPlugin plugin;
    static ServerMock server;

    @BeforeAll
    public static void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        
    }
    
    
    
    
    
    @Test
    public void test() {
        PlayerMock entity = server.addPlayer();
        entity.addAttachment(plugin, "test", true);
        Assertions.assertTrue(entity.hasPermission("test"));
    }

    
    
}
