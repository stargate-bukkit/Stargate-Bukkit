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
import org.junit.jupiter.api.extension.ExtendWith;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.economy.StargateEconomyManagerMock;
import org.sgrewritten.stargate.manager.BlockLoggerMock;

import java.util.logging.Level;

@ExtendWith(StargateExtension.class)
class PluginEventListenerTest {

    private @NotNull MockPlugin economy;
    private @NotNull MockPlugin blockLogger;
    private PluginEventListener listener;
    private StargateEconomyManagerMock economyManager;
    private BlockLoggerMock blockLoggingManager;

    @BeforeEach
    void setUp() {
        Stargate.setLogLevel(Level.SEVERE);
        economy = MockBukkit.createMockPlugin("Vault");
        blockLogger = MockBukkit.createMockPlugin("CoreProtect");
        economyManager = new StargateEconomyManagerMock();
        blockLoggingManager = new BlockLoggerMock();
        listener = new PluginEventListener(economyManager, blockLoggingManager);
    }

    @AfterEach
    void tearDown() {
        Stargate.setLogLevel(Level.INFO);
    }

    @Test
    void onPluginDisableVault() {
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
