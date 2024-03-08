package org.sgrewritten.stargate.manager;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StargateNetworkManager;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.TestPortalBuilder;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;
import org.sgrewritten.stargate.util.LanguageManagerMock;
import org.sgrewritten.stargate.util.StargateTestHelper;

import java.util.Set;

class StargateBungeeManagerTest {

    private ServerMock server;
    private RegistryAPI registry;
    private WorldMock world;
    private StargateBungeeManager bungeeManager;
    private RealPortal realPortal;
    private RealPortal bungeePortal;

    private static final String SERVER = "server";
    private static final String NETWORK = "network1";
    private static final String NETWORK2 = "network2";
    private static final String PORTAL = "portal";
    private static final String PORTAL2 = "portal2";
    private static final String PLAYER = "player";
    private static final String REGISTERED_PORTAL = "rPortal";
    private StargateNetworkManager networkManager;
    private StargateAPI stargateAPI;
    private int count = 0;
    private TestPortalBuilder testPortalBuilder;

    @BeforeEach
    void setUp() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        server = StargateTestHelper.setup();
        Stargate.setServerName(SERVER);
        stargateAPI = new StargateAPIMock();
        registry = stargateAPI.getRegistry();
        this.networkManager = (StargateNetworkManager) stargateAPI.getNetworkManager();
        world = server.addSimpleWorld("world");
        Network network2 = networkManager.createNetwork(NETWORK2, NetworkType.CUSTOM, StorageType.INTER_SERVER, false);

        this.testPortalBuilder = new TestPortalBuilder(registry, world);
        testPortalBuilder.setNetwork(network2).setStorageType(StorageType.INTER_SERVER).setName(REGISTERED_PORTAL);
        realPortal = testPortalBuilder.build();
        network2.addPortal(realPortal);

        Network bungeeNetwork = networkManager.createNetwork(ConfigurationHelper.getString(ConfigurationOption.LEGACY_BUNGEE_NETWORK), NetworkType.CUSTOM, StorageType.LOCAL,
                false);
        testPortalBuilder.setNetwork(bungeeNetwork).setStorageType(StorageType.LOCAL).setFlags(Set.of(PortalFlag.LEGACY_INTERSERVER));
        bungeePortal = testPortalBuilder.build();
        bungeeNetwork.addPortal(bungeePortal);

        bungeeManager = new StargateBungeeManager(registry, new LanguageManagerMock(), networkManager);
    }

    @AfterEach
    void tearDown() {
        StargateTestHelper.tearDown();
    }

    @Test
    void updateNetwork() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        //A network not assigned to a registry
        Network network = new StargateNetwork(NETWORK, NetworkType.CUSTOM, StorageType.INTER_SERVER);
        testPortalBuilder.setNetwork(network).setName(PORTAL).setStorageType(StorageType.INTER_SERVER);
        RealPortal portal = testPortalBuilder.build();
        testPortalBuilder.setName(PORTAL2);
        RealPortal portal2 = testPortalBuilder.build();

        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal, StargateProtocolRequestType.PORTAL_ADD));
        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal2, StargateProtocolRequestType.PORTAL_ADD));
        Network network1 = registry.getNetwork(NETWORK, StorageType.INTER_SERVER);
        Assertions.assertNotNull(network1);
        Assertions.assertNotNull(network1.getPortal(PORTAL));
        Assertions.assertNotNull(network1.getPortal(PORTAL2));
    }

    @Test
    void updateNetwork_renamePortal() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        //A network not assigned to a registry
        Network network = new StargateNetwork(NETWORK, NetworkType.CUSTOM, StorageType.INTER_SERVER);
        testPortalBuilder.setNetwork(network).setName(PORTAL).setStorageType(StorageType.INTER_SERVER);
        RealPortal portal = testPortalBuilder.build();
        String newName = "new_portal";
        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal, StargateProtocolRequestType.PORTAL_ADD));
        bungeeManager.updateNetwork(BungeeHelper.generateRenamePortalMessage(newName, portal.getName(), network));
        Network network1 = registry.getNetwork(NETWORK, StorageType.INTER_SERVER);
        Assertions.assertNotNull(network1);
        Assertions.assertNull(network1.getPortal(PORTAL));
        Assertions.assertNotNull(network1.getPortal(newName));
    }

    @Test
    void updateNetwork_renameNetwork() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        //A network not assigned to a registry
        Network network = new StargateNetwork(NETWORK, NetworkType.CUSTOM, StorageType.INTER_SERVER);
        testPortalBuilder.setNetwork(network).setName(PORTAL).setStorageType(StorageType.INTER_SERVER);
        RealPortal portal = testPortalBuilder.build();
        String newName = "new_network";
        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal, StargateProtocolRequestType.PORTAL_ADD));
        Network preRenameNetwork = registry.getNetwork(NETWORK, StorageType.INTER_SERVER);
        bungeeManager.updateNetwork(BungeeHelper.generateRenameNetworkMessage(newName, NETWORK));
        Network renamedNetwork = registry.getNetwork(newName, StorageType.INTER_SERVER);
        Assertions.assertEquals(preRenameNetwork, renamedNetwork);
        Assertions.assertNotNull(renamedNetwork);
        Assertions.assertNull(registry.getNetwork(NETWORK, StorageType.INTER_SERVER));
        Assertions.assertNotNull(renamedNetwork.getPortal(PORTAL));
    }


    @Test
    void playerConnectOnline() {
        PlayerMock player = server.addPlayer(PLAYER);

        bungeeManager.playerConnect(BungeeHelper.generateTeleportJsonMessage(PLAYER, realPortal));
        Component componentMessage = player.nextComponentMessage();
        Assertions.assertFalse(componentMessage != null && componentMessage.toString().contains("[ERROR]"), "An error message was sent to the player '" + componentMessage + "'");
    }

    @Test
    void playerConnectOffline() {
        bungeeManager.playerConnect(BungeeHelper.generateTeleportJsonMessage(PLAYER, realPortal));
        Portal pulledPortal = bungeeManager.pullFromQueue(PLAYER);
        Assertions.assertEquals(realPortal.getName(), pulledPortal.getName());
        Assertions.assertEquals(realPortal.getNetwork().getId(), pulledPortal.getNetwork().getId());
    }

    @Test
    void legacyPlayerConnectOnline() {
        PlayerMock player = server.addPlayer(PLAYER);

        bungeeManager.legacyPlayerConnect(BungeeHelper.generateLegacyTeleportMessage(PLAYER, bungeePortal));
        Component componentMessage = player.nextComponentMessage();
        Assertions.assertFalse(componentMessage != null && componentMessage.toString().contains("[ERROR]"), "An error message was sent to the player '" + componentMessage + "'");
    }

    @Test
    void legacyPlayerConnectOffline() {
        bungeeManager.legacyPlayerConnect(BungeeHelper.generateLegacyTeleportMessage(PLAYER, bungeePortal));
        Portal pulledPortal = bungeeManager.pullFromQueue(PLAYER);
        Assertions.assertEquals(bungeePortal, pulledPortal);
    }

}
