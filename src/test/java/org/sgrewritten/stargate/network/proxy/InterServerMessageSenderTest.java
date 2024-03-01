package org.sgrewritten.stargate.network.proxy;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.PortalBuilder;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.property.PluginChannel;
import org.sgrewritten.stargate.property.StargateProtocolProperty;
import org.sgrewritten.stargate.property.StargateProtocolRequestType;
import org.sgrewritten.stargate.util.StargateTestHelper;

class InterServerMessageSenderTest {

    private InterServerMessageSender messageSender;
    private TestPluginMessageInterface pluginMessageInterface;
    private Network network;
    private static final String NETWORK_ID = "network";
    private static final String PORTAL_NAME = "portal";
    private ServerMock server;
    private RealPortal portal;
    private StargateAPIMock stargateAPI;

    @BeforeEach
    void setUp() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        this.server = StargateTestHelper.setup();
        this.pluginMessageInterface = new TestPluginMessageInterface();
        this.messageSender = new InterServerMessageSender(pluginMessageInterface);
        this.network = new StargateNetwork(NETWORK_ID, NetworkType.CUSTOM, StorageType.INTER_SERVER);
        this.stargateAPI = new StargateAPIMock();
        World world = server.addSimpleWorld("world");
        PortalBuilder portalBuilder = new PortalBuilder(stargateAPI,server.addPlayer(),PORTAL_NAME);
        portalBuilder.setGateBuilder(new Location(world, 0, 10, 0),"nether.gate").setNetwork(network);
        this.portal = portalBuilder.build();
    }

    @AfterEach
    void tearDown() {
        StargateTestHelper.tearDown();
    }

    @Test
    void sendCreatePortal() {
        messageSender.sendCreatePortal(portal);
        TwoTuple<String, PluginChannel> sentData = pluginMessageInterface.getSentMessageFromQueue();
        Assertions.assertNotNull(sentData);
        Assertions.assertEquals(PluginChannel.NETWORK_CHANGED, sentData.getSecondValue());
        JsonObject parsedData = (JsonObject) JsonParser.parseString(sentData.getFirstValue());
        Assertions.assertEquals(StargateProtocolRequestType.PORTAL_ADD.toString(), parsedData.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString());
        Assertions.assertEquals(network.getId(), parsedData.get(StargateProtocolProperty.NETWORK.toString()).getAsString());
        Assertions.assertEquals(portal.getName(), parsedData.get(StargateProtocolProperty.PORTAL.toString()).getAsString());
        Assertions.assertEquals(Stargate.getServerName(), parsedData.get(StargateProtocolProperty.SERVER.toString()).getAsString());
    }

    @Test
    void sendDeletePortal() {
        messageSender.sendDeletePortal(portal);
        TwoTuple<String, PluginChannel> sentData = pluginMessageInterface.getSentMessageFromQueue();
        Assertions.assertNotNull(sentData);
        Assertions.assertEquals(PluginChannel.NETWORK_CHANGED, sentData.getSecondValue());
        JsonObject parsedData = (JsonObject) JsonParser.parseString(sentData.getFirstValue());
        Assertions.assertEquals(StargateProtocolRequestType.PORTAL_REMOVE.toString(), parsedData.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString());
        Assertions.assertEquals(NETWORK_ID, parsedData.get(StargateProtocolProperty.NETWORK.toString()).getAsString());
        Assertions.assertEquals(portal.getName(), parsedData.get(StargateProtocolProperty.PORTAL.toString()).getAsString());
        Assertions.assertEquals(Stargate.getServerName(), parsedData.get(StargateProtocolProperty.SERVER.toString()).getAsString());
    }

    @Test
    void sendRenameNetwork() {
        String newNetName = "new_network";
        messageSender.sendRenameNetwork(newNetName, NETWORK_ID);
        TwoTuple<String, PluginChannel> sentData = pluginMessageInterface.getSentMessageFromQueue();
        Assertions.assertNotNull(sentData);
        Assertions.assertEquals(PluginChannel.NETWORK_CHANGED, sentData.getSecondValue());
        JsonObject parsedData = (JsonObject) JsonParser.parseString(sentData.getFirstValue());
        Assertions.assertEquals(StargateProtocolRequestType.NETWORK_RENAME.toString(), parsedData.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString());
        Assertions.assertEquals(NETWORK_ID, parsedData.get(StargateProtocolProperty.NETWORK.toString()).getAsString());
        Assertions.assertEquals(newNetName, parsedData.get(StargateProtocolProperty.NEW_NETWORK_NAME.toString()).getAsString());
    }

    @Test
    void sendRenamePortal() {
        String newPortalName = "new_portal";
        messageSender.sendRenamePortal(newPortalName, PORTAL_NAME, network);
        TwoTuple<String, PluginChannel> sentData = pluginMessageInterface.getSentMessageFromQueue();
        Assertions.assertNotNull(sentData);
        Assertions.assertEquals(PluginChannel.NETWORK_CHANGED, sentData.getSecondValue());
        JsonObject parsedData = (JsonObject) JsonParser.parseString(sentData.getFirstValue());
        Assertions.assertEquals(StargateProtocolRequestType.PORTAL_RENAME.toString(), parsedData.get(StargateProtocolProperty.REQUEST_TYPE.toString()).getAsString());
        Assertions.assertEquals(NETWORK_ID, parsedData.get(StargateProtocolProperty.NETWORK.toString()).getAsString());
        Assertions.assertEquals(newPortalName, parsedData.get(StargateProtocolProperty.NEW_PORTAL_NAME.toString()).getAsString());
        Assertions.assertEquals(PORTAL_NAME, parsedData.get(StargateProtocolProperty.PORTAL.toString()).getAsString());
    }
}