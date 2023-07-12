package org.sgrewritten.stargate.util;

import be.seeseemelk.mockbukkit.MockBukkit;
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
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.api.structure.GateStructureType;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.property.BlockEventType;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;

class BlockEventHelperTest {

    private static RegistryAPI registry;
    private static RealPortal portal;
    private static Block signBlock;
    private static PlayerMock player;

    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    @BeforeAll
    static void setUp() throws TranslatableException, NoFormatFoundException, GateConflictException {
        ServerMock server = MockBukkit.mock();
        player = server.addPlayer();
        WorldMock world = server.addSimpleWorld("world");

        signBlock = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
        registry = new StargateRegistry(new StorageMock());
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR, new FakeStargateLogger())));
        Network network = registry.createNetwork("network", NetworkType.CUSTOM, false, false);

        portal = FakePortalGenerator.generateFakePortal(signBlock, network, new HashSet<>(), "name", registry);

    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @EnumSource
    void onAnyBlockChangeEventTest_SignLocation(BlockEventType type) {
        Cancellable event = new BlockBreakEvent(signBlock, player);
        BlockEventHelper.onAnyBlockChangeEvent(event, type, signBlock.getLocation(), registry);
        Assertions.assertTrue(event.isCancelled());
    }

    @ParameterizedTest
    @EnumSource
    void onAnyBlockChangeEventTest_FrameLocation(BlockEventType type) {
        Location frame = portal.getGate().getLocations(GateStructureType.FRAME).get(0).getLocation();
        Cancellable event = new BlockBreakEvent(frame.getBlock(), player);
        BlockEventHelper.onAnyBlockChangeEvent(event, type, frame, registry);
        Assertions.assertTrue(event.isCancelled());
    }

    @ParameterizedTest
    @EnumSource
    void onAnyBlockChangeEventTest_IrisLocation(BlockEventType type) {
        Location iris = portal.getGate().getLocations(GateStructureType.IRIS).get(0).getLocation();
        Cancellable event = new BlockBreakEvent(iris.getBlock(), player);
        BlockEventHelper.onAnyBlockChangeEvent(event, type, iris, registry);
        Assertions.assertTrue(event.isCancelled());
    }
}
