package org.sgrewritten.stargate.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.economy.FakeEconomyManager;
import org.sgrewritten.stargate.manager.FakeBlockLogger;

import java.util.logging.Level;

class PluginEventListenerTest {

    private @NotNull MockPlugin economy;
    private @NotNull MockPlugin blockLogger;
    private PluginEventListener listener;
    private FakeEconomyManager economyManager;
    private FakeBlockLogger blockLoggingManager;
    private @NotNull ServerMock server;

    @BeforeEach
    void setUp() {
        Stargate.setLogLevel(Level.SEVERE);
        server = MockBukkit.mock();
        economy = MockBukkit.createMockPlugin("Vault");
        blockLogger = MockBukkit.createMockPlugin("CoreProtect");
        economyManager = new FakeEconomyManager();
        blockLoggingManager = new FakeBlockLogger();
        listener = new PluginEventListener(economyManager, blockLoggingManager);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
        Stargate.setLogLevel(Level.INFO);
    }

    @Test
    void onPluginDisable_Vault() {
        //TODO Find out a way to detect the disable message is sent
        Assertions.assertDoesNotThrow(() -> listener.onPluginDisable(new PluginDisableEvent(economy)));
    }

    @Test
    void onPluginDisable_CoreProtect() {
        Assertions.assertDoesNotThrow(() -> listener.onPluginDisable(new PluginDisableEvent(blockLogger)));
    }

    @Test
    void onPluginEnable_Vault() {
        listener.onPluginEnable(new PluginEnableEvent(economy));
        Assertions.assertTrue(economyManager.hasTriggeredSetupEconomy());
    }

    @Test
    void onPluginEnable_CoreProtect() {
        listener.onPluginEnable(new PluginEnableEvent(blockLogger));
        Assertions.assertTrue(blockLoggingManager.hasTriggeredSetup());
    }
}
