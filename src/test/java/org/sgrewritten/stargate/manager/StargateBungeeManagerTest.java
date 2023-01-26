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
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.InterServerNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.BungeePortal;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.BungeeHelper;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;

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

    @BeforeEach
    void setUp() throws TranslatableException, InvalidStructureException {
        server = MockBukkit.mock();
        GateFormatHandler.setFormats(
                Objects.requireNonNull(GateFormatHandler.loadGateFormats(testGatesDir, new FakeStargateLogger())));
        Stargate.setServerName(SERVER);
        registry = new StargateRegistry(new FakeStorage());
        world = server.addSimpleWorld("world");
        Network network2 = registry.createNetwork(NETWORK2, NetworkType.CUSTOM, true, false);
        realPortal = new FakePortalGenerator().generateFakePortal(world, network2, REGISTERED_PORTAL, true);
        network2.addPortal(realPortal, false);

        Network bungeeNetwork = registry.createNetwork(BungeePortal.getLegacyNetworkName(), NetworkType.CUSTOM, false,
                false);
        Set<PortalFlag> bungeePortalFlags = new HashSet<>();
        bungeePortal = new FakePortalGenerator().generateFakePortal(new Location(world, 0, 10, 0), bungeeNetwork,
                NETWORK, false, bungeePortalFlags, registry);
        bungeeNetwork.addPortal(bungeePortal, false);

        bungeeManager = new StargateBungeeManager(registry, new FakeLanguageManager());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void updateNetwork() throws TranslatableException, InvalidStructureException {
        //A network not assigned to a registry
        Network network = new InterServerNetwork(NETWORK, NetworkType.CUSTOM);
        RealPortal portal = new FakePortalGenerator().generateFakePortal(world, network, PORTAL, true);
        RealPortal portal2 = new FakePortalGenerator().generateFakePortal(world, network, PORTAL2, true);

        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal, StargateProtocolRequestType.PORTAL_ADD));
        bungeeManager.updateNetwork(BungeeHelper.generateJsonMessage(portal2, StargateProtocolRequestType.PORTAL_ADD));
        Network network1 = registry.getNetwork(NETWORK, true);
        Assertions.assertNotNull(network1);
        Assertions.assertNotNull(network1.getPortal(PORTAL));
        Assertions.assertNotNull(network1.getPortal(PORTAL2));
    }

    @Test
    void playerConnect_Online() {
        PlayerMock player = server.addPlayer(PLAYER);

        bungeeManager.playerConnect(BungeeHelper.generateTeleportJsonMessage(PLAYER, realPortal));
        Component componentMessage = player.nextComponentMessage();
        Assertions.assertFalse(componentMessage != null && componentMessage.toString().contains("[ERROR]"), "A error message was sent to the player '" + componentMessage + "'");
    }

    @Test
    void playerConnect_Offline() {
        bungeeManager.playerConnect(BungeeHelper.generateTeleportJsonMessage(PLAYER, realPortal));
        Portal pulledPortal = bungeeManager.pullFromQueue(PLAYER);
        Assertions.assertEquals(realPortal.getName(), pulledPortal.getName());
        Assertions.assertEquals(realPortal.getNetwork().getId(), pulledPortal.getNetwork().getId());
    }

    @Test
    void legacyPlayerConnect_Online() {
        PlayerMock player = server.addPlayer(PLAYER);

        bungeeManager.legacyPlayerConnect(BungeeHelper.generateLegacyTeleportMessage(PLAYER, bungeePortal));
        Component componentMessage = player.nextComponentMessage();
        Assertions.assertFalse(componentMessage != null && componentMessage.toString().contains("[ERROR]"), "A error message was sent to the player '" + componentMessage + "'");
    }

    @Test
    void legacyPlayerConnect_Offline() {
        bungeeManager.legacyPlayerConnect(BungeeHelper.generateLegacyTeleportMessage(PLAYER, bungeePortal));
        Portal pulledPortal = bungeeManager.pullFromQueue(PLAYER);
        Assertions.assertEquals(bungeePortal, pulledPortal);
    }
}
