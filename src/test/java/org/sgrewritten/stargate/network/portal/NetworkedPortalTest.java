package org.sgrewritten.stargate.network.portal;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.util.FakeStorage;
import org.sgrewritten.stargate.util.portal.GateTestHelper;

import java.util.HashSet;
import java.util.Set;

class NetworkedPortalTest {

    private @NotNull WorldMock world;
    private StargateRegistry registry;
    private NetworkedPortal portal;
    private @NotNull PlayerMock player;
    private Block sign;
    private @NotNull MockPlugin plugin;
    private Network network;

    @BeforeEach
    void setUp() throws TranslatableException, NoFormatFoundException, GateConflictException {
        @NotNull ServerMock server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("Stargate");
        GateTestHelper.setUpGates();
        world = server.addSimpleWorld("world");
        registry = new StargateRegistry(new FakeStorage());
        player = server.addPlayer();
        player.addAttachment(plugin, "sg.use", true);
        sign = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
        Set<PortalFlag> flags = new HashSet<>();
        flags.add(PortalFlag.NETWORKED);
        network = registry.createNetwork("network", NetworkType.CUSTOM, false, false);
        portal = (NetworkedPortal) FakePortalGenerator.generateFakePortal(sign, network, flags, "networked", registry);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @EnumSource
    void onSignClick_NoPerm(Action type) {
        player.addAttachment(plugin, "sg.use", false);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClick_NoPermSneaking(Action type) {
        player.setSneaking(true);
        player.addAttachment(plugin, "sg.use", false);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClick(Action type) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClick_Sneaking(Action type) {
        player.setSneaking(true);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClick_AvailableDestination(Action type) throws TranslatableException, NoFormatFoundException, GateConflictException {
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        sign = PortalBlockGenerator.generatePortal(new Location(world, 0, 20, 0));
        FakePortalGenerator.generateFakePortal(sign, network, new HashSet<>(), "destination", registry);
        Assertions.assertDoesNotThrow(() -> portal.onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClick_SneakingAvailableDestination(Action type) throws TranslatableException, NoFormatFoundException, GateConflictException {
        sign = PortalBlockGenerator.generatePortal(new Location(world, 0, 20, 0));
        FakePortalGenerator.generateFakePortal(sign, network, new HashSet<>(), "destination", registry);
        player.setSneaking(true);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.onSignClick(event));
    }

    @Test
    void activate() {
        portal.activate(player);
        Assertions.assertEquals(player.getUniqueId(), portal.activator);
    }

    @Test
    void deactivate() {
        portal.activate(player);
        portal.deactivate();
        Assertions.assertNotEquals(player.getUniqueId(), portal.activator);
    }

    @Test
    void open() {
        portal.open(player);
        Assertions.assertTrue(portal.isOpen());
    }

    @Test
    void close() {
        portal.open(player);
        portal.close(false);
        Assertions.assertFalse(portal.isOpen());
    }
}
