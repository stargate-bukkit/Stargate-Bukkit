package org.sgrewritten.stargate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.scheduler.BukkitSchedulerMock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.database.TestCredential;
import org.sgrewritten.stargate.database.TestCredentialsManager;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StargateTest {

    private Stargate plugin;
    private ServerMock server;
    private RealPortal portal;
    private BukkitSchedulerMock scheduler;
    private RealPortal bungeePortal;

    private static final String PORTAL2 = "name2";

    @BeforeEach
    public void setup() throws TranslatableException, NoFormatFoundException, GateConflictException {
        server = MockBukkit.mock();
        scheduler = server.getScheduler();
        WorldMock world = server.addSimpleWorld("world");
        System.setProperty("bstats.relocatecheck", "false");
        plugin = MockBukkit.load(Stargate.class);

        Block signBlock1 = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
        Block signBlock2 = PortalBlockGenerator.generatePortal(new Location(world, 0, 20, 0));

        Network network = plugin.getRegistry().createNetwork("network", NetworkType.CUSTOM, false, false);

        String PORTAL1 = "name1";
        portal = FakePortalGenerator.generateFakePortal(signBlock1, network, new HashSet<>(), PORTAL1, plugin.getRegistry());
        Set<PortalFlag> flags = new HashSet<>();
        flags.add(PortalFlag.BUNGEE);
        Network bungeeNetwork = plugin.getRegistry().createNetwork(BungeePortal.getLegacyNetworkName(), NetworkType.CUSTOM, false, false);
        bungeePortal = FakePortalGenerator.generateFakePortal(signBlock2, bungeeNetwork, flags, PORTAL2, plugin.getRegistry());
    }

    @AfterEach
    public void tearDown() {
        portal.destroy();
        bungeePortal.destroy();
        MockBukkit.unmock();
    }

    @Test
    public void getEconomyManager() {
        assertNotNull(plugin.getEconomyManager());
    }

    @Test
    public void getCurrentConfigVersion() {
        Assertions.assertNotEquals(Stargate.getCurrentConfigVersion(), 0);
    }

    @Test
    public void getAbsoluteDataFolder() {
        assertNotNull(plugin.getAbsoluteDataFolder());
    }

    @Test
    public void getGateFolder() {
        assertNotNull(plugin.getGateFolder());
    }

    @Test
    public void addSynchronousTickAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousTickAction(new SupplierAction(() -> true)));
        scheduler.performOneTick();
    }

    @Test
    public void addSynchronousSecAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousSecAction(new SupplierAction(() -> true)));
        scheduler.performOneTick();
    }

    @Test
    public void addSynchronousBungeeSecAction() {
        Assertions.assertDoesNotThrow(() -> Stargate.addSynchronousSecAction(new SupplierAction(() -> true), true));
        scheduler.performOneTick();
    }

    @Test
    public void setGetConfigurationOptionValue() {
        plugin.setConfigurationOptionValue(ConfigurationOption.UPKEEP_COST, 2);
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
    public void reloadStupidDefaultNetworkNameUUID() {
        Stargate.setLogLevel(Level.OFF);
        plugin.setConfigurationOptionValue(ConfigurationOption.DEFAULT_NETWORK, UUID.randomUUID().toString());
        plugin.reload();
        Stargate.setLogLevel(Level.INFO);
        Assertions.assertFalse(plugin.isEnabled());
    }

    @Test
    public void reload_StupidDefaultNetworkNameTooLong() {
        Stargate.setLogLevel(Level.OFF);
        plugin.setConfigurationOptionValue(ConfigurationOption.DEFAULT_NETWORK, "thisNameIsWayTooLong");
        plugin.reload();
        Stargate.setLogLevel(Level.INFO);
        Assertions.assertFalse(plugin.isEnabled());
    }

    @Test
    public void reloadInterServer() {
        setInterServerEnabled();
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
        Network network = plugin.getRegistry().getNetwork(BungeePortal.getLegacyNetworkName(), false);
        assertNotNull(network);
        assertNotNull(network.getPortal(PORTAL2));
    }

    @Test
    public void restartInterServer() {
        setInterServerEnabled();
        server.getPluginManager().disablePlugin(plugin);
        Assertions.assertNull(Stargate.getInstance());
        server.getPluginManager().enablePlugin(plugin);
        Assertions.assertTrue(plugin.isEnabled());
        assertNotNull(Stargate.getServerUUID());
    }

    private void setInterServerEnabled() {
        plugin.setConfigurationOptionValue(ConfigurationOption.USING_BUNGEE, true);
        plugin.setConfigurationOptionValue(ConfigurationOption.USING_REMOTE_DATABASE, true);
        TestCredentialsManager credentialsManager = new TestCredentialsManager("mysql_credentials.secret");
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_ADDRESS, credentialsManager.getCredentialString(TestCredential.MYSQL_DB_ADDRESS, "localhost"));
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_USERNAME, credentialsManager.getCredentialString(TestCredential.MYSQL_DB_USER, "root"));
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_PASSWORD, credentialsManager.getCredentialString(TestCredential.MYSQL_DB_PASSWORD, "root"));
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_PORT, credentialsManager.getCredentialInt(TestCredential.MYSQL_DB_PORT, 3306));
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_USE_SSL, false);
        plugin.setConfigurationOptionValue(ConfigurationOption.BUNGEE_DATABASE, credentialsManager.getCredentialString(TestCredential.MYSQL_DB_NAME, "Stargate"));
    }
}
