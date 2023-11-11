package org.sgrewritten.stargate.manager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.RegistryMock;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StargateNetworkManager;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.PortalFactory;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;
import org.sgrewritten.stargate.util.LanguageManagerMock;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StargateBungeeManagerTest {

    private ServerMock server;
    private StargateRegistry registry;
    private WorldMock world;
    private StargateBungeeManager bungeeManager;
    private RealPortal realPortal;
    private RealPortal bungeePortal;
    private static final File testGatesDir = new File("src/test/resources/gates");

    private static final String SERVER = "server";
    private static final String NETWORK = "network1";
    private static final String NETWORK2 = "network2";
    private static final String PORTAL = "portal";
    private static final String PORTAL2 = "portal2";
    private static final String PLAYER = "player";
    private static final String REGISTERED_PORTAL = "rPortal";
    private StargateNetworkManager networkManager;

    @BeforeEach
    void setUp() throws TranslatableException, InvalidStructureException {
        server = MockBukkit.mock();
        GateFormatRegistry.setFormats(
                Objects.requireNonNull(GateFormatHandler.loadGateFormats(testGatesDir)));
        Stargate.setServerName(SERVER);
        registry = new RegistryMock();
        this.networkManager = new StargateNetworkManager(registry, new StorageMock());
        world = server.addSimpleWorld("world");
        Network network2 = networkManager.createNetwork(NETWORK2, NetworkType.CUSTOM, true, false);
        realPortal = PortalFactory.generateFakePortal(world, network2, REGISTERED_PORTAL, true);
        network2.addPortal(realPortal);

        Network bungeeNetwork = networkManager.createNetwork(BungeePortal.getLegacyNetworkName(), NetworkType.CUSTOM, false,
                false);
        Set<PortalFlag> bungeePortalFlags = new HashSet<>();
        bungeePortal = PortalFactory.generateFakePortal(new Location(world, 0, 10, 0), bungeeNetwork,
                NETWORK, false, bungeePortalFlags, new HashSet<>(), registry);
        bungeeNetwork.addPortal(bungeePortal);

        bungeeManager = new StargateBungeeManager(registry, new LanguageManagerMock(),networkManager);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void updateNetwork() throws TranslatableException, InvalidStructureException {
        //A network not assigned to a registry
        Network network = new StargateNetwork(NETWORK, NetworkType.CUSTOM, StorageType.INTER_SERVER);
        RealPortal portal = PortalFactory.generateFakePortal(world, network, PORTAL, true);
        RealPortal portal2 = PortalFactory.generateFakePortal(world, network, PORTAL2, true);

        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal, StargateProtocolRequestType.PORTAL_ADD));
        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal2, StargateProtocolRequestType.PORTAL_ADD));
        Network network1 = registry.getNetwork(NETWORK, true);
        Assertions.assertNotNull(network1);
        Assertions.assertNotNull(network1.getPortal(PORTAL));
        Assertions.assertNotNull(network1.getPortal(PORTAL2));
    }

    @Test
    void playerConnectOnline() {
        PlayerMock player = server.addPlayer(PLAYER);

        bungeeManager.playerConnect(BungeeHelper.generateTeleportJsonMessage(PLAYER, realPortal));
        Component componentMessage = player.nextComponentMessage();
        Assertions.assertFalse(componentMessage != null && componentMessage.toString().contains("[ERROR]"), "A error message was sent to the player '" + componentMessage + "'");
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
        Assertions.assertFalse(componentMessage != null && componentMessage.toString().contains("[ERROR]"), "A error message was sent to the player '" + componentMessage + "'");
    }

    @Test
    void legacyPlayerConnectOffline() {
        bungeeManager.legacyPlayerConnect(BungeeHelper.generateLegacyTeleportMessage(PLAYER, bungeePortal));
        Portal pulledPortal = bungeeManager.pullFromQueue(PLAYER);
        Assertions.assertEquals(bungeePortal, pulledPortal);
    }

}
