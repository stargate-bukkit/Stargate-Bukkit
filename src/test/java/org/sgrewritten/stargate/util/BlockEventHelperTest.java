package org.sgrewritten.stargate.util;

import static org.junit.jupiter.api.Assertions.*;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;

class BlockEventHelperTest {
    
    private static @NotNull ServerMock server;
    private static @NotNull WorldMock world;


    @BeforeAll
    static void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }
    
    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onAnyBlockChangeEventTest() {  
       //TODO implement this
    }

}
