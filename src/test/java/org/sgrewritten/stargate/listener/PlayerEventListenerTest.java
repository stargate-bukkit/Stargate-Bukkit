package org.sgrewritten.stargate.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.BungeeNameException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.manager.FakeBlockLogger;
import org.sgrewritten.stargate.manager.StargateBungeeManager;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.network.portal.RealPortal;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;

class PlayerEventListenerTest {

    private ServerMock server;
    private StargateRegistry registry;
    private PlayerEventListener listener;
    private WorldMock world;
    private RealPortal portal;
    private Block signBlock;
    private @NotNull PlayerMock player;
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    @BeforeEach
    void setUp() throws NameLengthException, BungeeNameException, NameConflictException, InvalidNameException, NoFormatFoundException, GateConflictException, UnimplementedFlagException {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        player = server.addPlayer();
        registry = new StargateRegistry(new FakeStorage());
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR, new FakeStargateLogger())));
        listener = new PlayerEventListener(new FakeLanguageManager(), registry, new StargateBungeeManager(registry, new FakeLanguageManager()), new FakeBlockLogger());
        signBlock = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
        portal = new FakePortalGenerator().generateFakePortal(signBlock, "network", new HashSet<>(), "name", registry);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @EnumSource
    void onPlayerInteractTest_Sign(Action type) {

        Assertions.assertDoesNotThrow(() -> listener.onPlayerInteract(new PlayerInteractEvent(player, type, null,
                signBlock, ((Directional) signBlock.getBlockData()).getFacing())));
    }

    @ParameterizedTest
    @EnumSource
    void onPlayerInteractTest_SignSneaking(Action type) {
        player.setSneaking(true);
        Assertions.assertDoesNotThrow(() -> listener.onPlayerInteract(new PlayerInteractEvent(player, type, null,
                signBlock, ((Directional) signBlock.getBlockData()).getFacing())));
    }
}
