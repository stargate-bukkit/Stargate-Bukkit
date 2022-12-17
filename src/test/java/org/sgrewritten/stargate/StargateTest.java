package org.sgrewritten.stargate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.config.ConfigurationOption;

import be.seeseemelk.mockbukkit.MockBukkit;

class StargateTest {

    private static @NotNull Stargate plugin;

    @BeforeAll
    public static void setup() {
        MockBukkit.mock();
        System.setProperty("bstats.relocatecheck", "false");
        plugin = MockBukkit.load(Stargate.class);
    }
    
    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    public void getEconomyManager() {
        Assertions.assertNotNull(Stargate.getEconomyManager());
    }
    
    @Test
    public void getCurrentConfigVersion() {
        Assertions.assertNotEquals(Stargate.getCurrentConfigVersion(), 0);
    }
    
    @Test
    public void getAbsoluteDataFolder() {
        Assertions.assertNotNull(plugin.getAbsoluteDataFolder());
    }
    
    @Test
    public void getGateFolder() {
        Assertions.assertNotNull(plugin.getGateFolder());
    }
    
    @Test
    public void addSynchronousTickAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousTickAction(new SupplierAction(() -> {
            return true;
        })));
    }
    
    @Test
    public void addSynchronousSecAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousSecAction(new SupplierAction(() -> {
            return true;
        })));
    }
    
    @Test
    public void addSynchronousBungeeSecAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousSecAction(new SupplierAction(() -> {
            return true;
        }),true));
    }
    
    @Test
    public void setGetConfigurationOptionValue() {
        plugin.setConfigurationOptionValue(ConfigurationOption.UPKEEP_COST,2);
        Assertions.assertEquals(2, plugin.getConfigurationOptionValue(ConfigurationOption.UPKEEP_COST));
    }
    
    
}
