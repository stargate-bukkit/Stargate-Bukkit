package org.sgrewritten.stargate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.RealPortal;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;

class StargateTest {

    private static Stargate plugin;
    private static Network network;
    private static ServerMock server;
    private static WorldMock world;
    private static RealPortal portal;

    @BeforeAll
    public static void setup() throws NameLengthException, NameConflictException, InvalidNameException, InvalidStructureException {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        System.setProperty("bstats.relocatecheck", "false");
        plugin = MockBukkit.load(Stargate.class);
        network = plugin.getRegistry().createNetwork("network", NetworkType.CUSTOM,false, false);
        portal = new FakePortalGenerator().generateFakePortal(world, network, "name", false);
        network.addPortal(portal, true);
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
    
    @Test
    public void reload() {
        Stargate.log(Level.FINEST, "reloading");
        plugin.reload();
        Assertions.assertTrue(plugin.isEnabled());
    }
    
    @Test
    public void reloadConfig() {
        Assertions.assertDoesNotThrow(() -> plugin.reloadConfig());
    }
}
