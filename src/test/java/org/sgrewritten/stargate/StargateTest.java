package org.sgrewritten.stargate;

import be.seeseemelk.mockbukkit.MockBukkitInject;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.scheduler.BukkitSchedulerMock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.gate.ImplicitGateBuilder;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.PortalBuilder;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.database.TestCredential;
import org.sgrewritten.stargate.database.TestCredentialsManager;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(StargateExtension.class)
class StargateTest {

    @StargateInject
    private Stargate plugin;
    @MockBukkitInject
    private ServerMock server;
    private RealPortal portal;
    private BukkitSchedulerMock scheduler;
    private RealPortal bungeePortal;

    private static final String PORTAL2 = "name2";
    private static final String PORTAL1 = "name1";
    private WorldMock world;
    private PlayerMock player;

    @BeforeEach
    void setup() throws TranslatableException, NoFormatFoundException, GateConflictException, InvalidStructureException {
        scheduler = server.getScheduler();
        this.world = server.addSimpleWorld("world");
        this.player = server.addPlayer();

        Block signBlock1 = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));

        Network network = plugin.getNetworkManager().createNetwork("network", NetworkType.CUSTOM, StorageType.LOCAL, false);
        portal = new PortalBuilder(plugin, player, PORTAL1).setGateBuilder(new ImplicitGateBuilder(signBlock1.getLocation(), plugin.getRegistry())).setNetwork(network).build();
    }

    @Test
    void getEconomyManager() {
        assertNotNull(plugin.getEconomyManager());
    }

    @Test
    void getAbsoluteDataFolder() {
        assertNotNull(plugin.getAbsoluteDataFolder());
    }

    @Test
    void setGetConfigurationOptionValue() {
        plugin.setConfigurationOptionValue(ConfigurationOption.UPKEEP_COST, 2);
        Assertions.assertEquals(2, plugin.getConfigurationOptionValue(ConfigurationOption.UPKEEP_COST));
        plugin.reload();
        Assertions.assertEquals(2, plugin.getConfigurationOptionValue(ConfigurationOption.UPKEEP_COST));
    }

    @Test
    void reload() {
        Stargate.log(Level.FINEST, "reloading");
        plugin.reload();
        Assertions.assertTrue(plugin.isEnabled());
    }

    @Test
    void reload_StupidDefaultNetworkNameUUID() {
        Stargate.setLogLevel(Level.OFF);
        plugin.setConfigurationOptionValue(ConfigurationOption.DEFAULT_NETWORK, UUID.randomUUID().toString());
        plugin.reload();
        Stargate.setLogLevel(Level.INFO);
        Assertions.assertFalse(plugin.isEnabled());
    }

    @ParameterizedTest
    @ValueSource(strings = {"thisNameIsWayTooLong", "", "Test1\nTest2"})
    void reload_StupidDefaultNetworkName(String name) {
        Stargate.setLogLevel(Level.OFF);
        plugin.setConfigurationOptionValue(ConfigurationOption.DEFAULT_NETWORK, name);
        plugin.reload();
        Stargate.setLogLevel(Level.INFO);
        Assertions.assertFalse(plugin.isEnabled());
    }

    @Test
    void reloadInterServer() {
        setInterServerEnabled();
        Assertions.assertTrue(plugin.isEnabled());
    }

    @Test
    void reloadConfig() {
        Assertions.assertDoesNotThrow(() -> plugin.reloadConfig());
    }

    @Test
    void restart() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        plugin.setConfigurationOptionValue(ConfigurationOption.USING_BUNGEE, true);
        createBungeePortal();
        server.getScheduler().performOneTick();
        server.getPluginManager().disablePlugin(plugin);
        Assertions.assertNull(Stargate.getInstance());
        server.getScheduler().waitAsyncTasksFinished();
        server.getPluginManager().enablePlugin(plugin);
        server.getScheduler().performOneTick();
        Assertions.assertTrue(plugin.isEnabled());
        Network network = plugin.getRegistry().getNetwork(ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK), StorageType.LOCAL);
        assertNotNull(network);
        assertNotNull(network.getPortal(PORTAL2));
    }

    @Test
    void restartInterServer() {
        setInterServerEnabled();
        server.getPluginManager().disablePlugin(plugin);
        Assertions.assertNull(Stargate.getInstance());
        server.getPluginManager().enablePlugin(plugin);
        Assertions.assertTrue(plugin.isEnabled());
        assertNotNull(Stargate.getServerUUID());
    }

    @Test
    void getMaterialResolver() {
        Assertions.assertNotNull(plugin.getMaterialHandlerResolver());
    }

    @Test
    void getNetworkManager_notNull() {
        Assertions.assertNotNull(plugin.getNetworkManager());
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

    private void createBungeePortal() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        Block signBlock2 = PortalBlockGenerator.generatePortal(new Location(world, 0, 20, 0));
        Set<StargateFlag> flags = new HashSet<>();
        flags.add(StargateFlag.LEGACY_INTERSERVER);
        PortalBuilder portalBuilder = new PortalBuilder(plugin, player, PORTAL2).setGateBuilder(new ImplicitGateBuilder(signBlock2.getLocation(), plugin.getRegistry())).setFlags(flags);
        bungeePortal = portalBuilder.setDestinationServerName("server").setDestination("destination").build();
        plugin.reload();
    }
}
