package org.sgrewritten.stargate;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkType;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.name.BungeeNameException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.scheduler.BukkitSchedulerMock;

class StargateTest {

    private Stargate plugin;
    private Network network;
    private ServerMock server;
    private WorldMock world;
    private RealPortal portal;
    private BukkitSchedulerMock scheduler;
    private RealPortal bungeePortal;
    private Network bungeeNetwork;
    
    private static String PORTAL1 = "name1";
    private static String PORTAL2 = "name2";

    @BeforeEach
    public void setup() throws NameLengthException, NameConflictException, InvalidNameException, InvalidStructureException, BungeeNameException, NoFormatFoundException, GateConflictException {
        server = MockBukkit.mock();
        scheduler = server.getScheduler();
        world = server.addSimpleWorld("world");
        System.setProperty("bstats.relocatecheck", "false");
        plugin = MockBukkit.load(Stargate.class);
        
        Block signBlock1 = PortalBlockGenerator.generatePortal(new Location(world,0,10,0));
        Block signBlock2 = PortalBlockGenerator.generatePortal(new Location(world,0,20,0));
        
        network = plugin.getRegistry().createNetwork("network", NetworkType.CUSTOM,false, false);
        
        portal = FakePortalGenerator.generateFakePortal(signBlock1,network,new HashSet<>(),PORTAL1,plugin.getRegistry());
        Set<PortalFlag> flags = new HashSet<>();
        flags.add(PortalFlag.BUNGEE);
        bungeeNetwork = plugin.getRegistry().createNetwork(BungeePortal.getLegacyNetworkName(), NetworkType.CUSTOM, false, false);
        bungeePortal = FakePortalGenerator.generateFakePortal(signBlock2,bungeeNetwork,flags,PORTAL2,plugin.getRegistry());
    }
    
    @AfterEach
    public void tearDown() {
        portal.destroy();
        bungeePortal.destroy();
        MockBukkit.unmock();
    }
    
    @Test
    public void getEconomyManager() {
        Assertions.assertNotNull(plugin.getEconomyManager());
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
        scheduler.performOneTick();
    }
    
    @Test
    public void addSynchronousSecAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousSecAction(new SupplierAction(() -> {
            return true;
        })));
        scheduler.performOneTick();
    }
    
    @Test
    public void addSynchronousBungeeSecAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousSecAction(new SupplierAction(() -> {
            return true;
        }),true));
        scheduler.performOneTick();
    }
    
    @Test
    public void setGetConfigurationOptionValue() {
        plugin.setConfigurationOptionValue(ConfigurationOption.UPKEEP_COST,2);
        Assertions.assertEquals(2, plugin.getConfigurationOptionValue(ConfigurationOption.UPKEEP_COST));
        plugin.reload();
        Assertions.assertEquals(2, plugin.getConfigurationOptionValue(ConfigurationOption.UPKEEP_COST));
    }
    
    @Test
    public void reload() {
        Stargate.log(Level.FINEST, "reloading");
        plugin.reload();
        Assertions.assertTrue(plugin.isEnabled());
    }
    
    @Test
    public void reload_StupidDefaultnetworkNameUUID() {
        Stargate.setLogLevel(Level.OFF);
        plugin.setConfigurationOptionValue(ConfigurationOption.DEFAULT_NETWORK, UUID.randomUUID().toString());
        plugin.reload();
        Stargate.setLogLevel(Level.INFO);
        Assertions.assertFalse(plugin.isEnabled());
    }
    
    @Test
    public void reload_StupidDefaultnetworkNameTooLong() {
        Stargate.setLogLevel(Level.OFF);
        plugin.setConfigurationOptionValue(ConfigurationOption.DEFAULT_NETWORK, "thisNameIsWayTooLong");
        plugin.reload();
        Stargate.setLogLevel(Level.INFO);
        Assertions.assertFalse(plugin.isEnabled());
    }
    
    @Test
    public void reload_Interserver() {
        setInterserverEnabled();
        plugin.reload();
        Assertions.assertTrue(plugin.isEnabled());
    }
    
    @Test
    public void reloadConfig() {
        Assertions.assertDoesNotThrow(() -> plugin.reloadConfig());
    }
    
    @Test
    public void restart() {
        server.getPluginManager().disablePlugin(plugin);
        Assertions.assertNull(Stargate.getInstance());
        server.getPluginManager().enablePlugin(plugin);
        Assertions.assertTrue(plugin.isEnabled());
        Assertions.assertNotNull(plugin.getRegistry().getNetwork(BungeePortal.getLegacyNetworkName(), false).getPortal(PORTAL2));
    }
    
    @Test
    public void restart_Interserver() {
        setInterserverEnabled();
        server.getPluginManager().disablePlugin(plugin);
        Assertions.assertNull(Stargate.getInstance());
        server.getPluginManager().enablePlugin(plugin);
        Assertions.assertTrue(plugin.isEnabled());
        Assertions.assertNotNull(Stargate.getServerUUID());
    }
    
    private void setInterserverEnabled() {
        plugin.setConfigurationOptionValue(ConfigurationOption.USING_BUNGEE, true);
        plugin.setConfigurationOptionValue(ConfigurationOption.USING_REMOTE_DATABASE, true);
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_ADDRESS, "localhost");
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_PASSWORD, "root");
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_PORT, 3306);
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_USE_SSL, false);
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_DATABASE, "stargate");
    }
}
