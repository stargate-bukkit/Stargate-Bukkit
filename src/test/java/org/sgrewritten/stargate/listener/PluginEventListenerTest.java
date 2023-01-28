package org.sgrewritten.stargate.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
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

    @BeforeEach
    void setUp() {
        Stargate.setLogLevel(Level.SEVERE);
        MockBukkit.mock();
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
    void onPluginDisableVault() {
        //TODO Find out a way to detect the disable message is sent
        Assertions.assertDoesNotThrow(() -> listener.onPluginDisable(new PluginDisableEvent(economy)));
    }

    @Test
    void onPluginDisableCoreProtect() {
        Assertions.assertDoesNotThrow(() -> listener.onPluginDisable(new PluginDisableEvent(blockLogger)));
    }

    @Test
    void onPluginEnableVault() {
        listener.onPluginEnable(new PluginEnableEvent(economy));
        Assertions.assertTrue(economyManager.hasTriggeredSetupEconomy());
    }

    @Test
    void onPluginEnableCoreProtect() {
        listener.onPluginEnable(new PluginEnableEvent(blockLogger));
        Assertions.assertTrue(blockLoggingManager.hasTriggeredSetup());
    }

}
