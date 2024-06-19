package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.gate.ImplicitGateBuilder;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.PortalBuilder;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.property.BlockEventType;

class BlockEventHelperTest {

    private static RegistryAPI registry;
    private static RealPortal portal;
    private static Block signBlock;
    private static PlayerMock player;
    private static StargateAPIMock stargateAPI;

    @BeforeAll
    static void setUp() throws TranslatableException, NoFormatFoundException, GateConflictException, InvalidStructureException {
        ServerMock server = StargateTestHelper.setup();
        player = server.addPlayer();
        WorldMock world = server.addSimpleWorld("world");

        signBlock = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
        stargateAPI = new StargateAPIMock();
        registry = stargateAPI.getRegistry();
        Network network = stargateAPI.getNetworkManager().createNetwork("network", NetworkType.CUSTOM, StorageType.LOCAL, false);
        portal = new PortalBuilder(stargateAPI, player, "name").setGateBuilder(new ImplicitGateBuilder(signBlock.getLocation(), registry)).setNetwork(network).build();

    }

    @AfterAll
    static void tearDown() {
        StargateTestHelper.tearDown();
    }

    @ParameterizedTest
    @EnumSource
    void onAnyBlockChangeEventTest_SignLocation(BlockEventType type) {
        Cancellable event = new BlockBreakEvent(signBlock, player);
        BlockEventHelper.onAnyBlockChangeEvent(event, type, signBlock.getLocation(), stargateAPI);
        Assertions.assertTrue(event.isCancelled());
    }

    @ParameterizedTest
    @EnumSource
    void onAnyBlockChangeEventTest_FrameLocation(BlockEventType type) {
        Location frame = portal.getGate().getLocations(GateStructureType.FRAME).get(0).getLocation();
        Cancellable event = new BlockBreakEvent(frame.getBlock(), player);
        BlockEventHelper.onAnyBlockChangeEvent(event, type, frame, stargateAPI);
        Assertions.assertTrue(event.isCancelled());
    }

    @ParameterizedTest
    @EnumSource
    void onAnyBlockChangeEventTest_IrisLocation(BlockEventType type) {
        Location iris = portal.getGate().getLocations(GateStructureType.IRIS).get(0).getLocation();
        Cancellable event = new BlockBreakEvent(iris.getBlock(), player);
        BlockEventHelper.onAnyBlockChangeEvent(event, type, iris, stargateAPI);
        Assertions.assertTrue(event.isCancelled());
    }
}
