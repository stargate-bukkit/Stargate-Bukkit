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
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.gate.ImplicitGateBuilder;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.PortalBuilder;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.util.StargateTestHelper;

import java.util.HashSet;
import java.util.Set;

class NetworkedPortalTest {

    private @NotNull WorldMock world;
    private RegistryAPI registry;
    private RealPortal portal;
    private @NotNull PlayerMock player;
    private Block sign;
    private @NotNull MockPlugin plugin;
    private Network network;
    private StargateAPIMock stargateAPI;

    @BeforeEach
    void setUp() throws TranslatableException, NoFormatFoundException, GateConflictException, InvalidStructureException {
        ServerMock server = StargateTestHelper.setup();
        plugin = MockBukkit.createMockPlugin("Stargate");
        world = server.addSimpleWorld("world");
        this.stargateAPI = new StargateAPIMock();
        registry = stargateAPI.getRegistry();
        player = server.addPlayer();
        player.addAttachment(plugin, "sg.use", true);
        sign = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
        Set<PortalFlag> flags = new HashSet<>();
        flags.add(PortalFlag.NETWORKED);
        network = stargateAPI.getNetworkManager().createNetwork("network", NetworkType.CUSTOM, StorageType.LOCAL, false);
        PortalBuilder builder = new PortalBuilder(stargateAPI,player,"networked").setGateBuilder(new ImplicitGateBuilder(sign.getLocation(),registry));
        builder.setNetwork(network).setFlags(flags);
        portal = builder.build();
    }

    @AfterEach
    void tearDown() {
        StargateTestHelper.tearDown();
    }

    @ParameterizedTest
    @EnumSource
    void onSignClickNoPermission(Action type) {
        player.addAttachment(plugin, "sg.use", false);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.getBehavior().onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClickNoPermissionSneaking(Action type) {
        player.setSneaking(true);
        player.addAttachment(plugin, "sg.use", false);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.getBehavior().onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClick(Action type) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.getBehavior().onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClickSneaking(Action type) {
        player.setSneaking(true);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.getBehavior().onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClickAvailableDestination(Action type) throws TranslatableException, NoFormatFoundException, GateConflictException, InvalidStructureException {
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        sign = PortalBlockGenerator.generatePortal(new Location(world, 0, 20, 0));
        new PortalBuilder(stargateAPI,player,"destination").setNetwork(network).setGateBuilder(new ImplicitGateBuilder(sign.getLocation(),registry)).build();
        Assertions.assertDoesNotThrow(() -> portal.getBehavior().onSignClick(event));
    }

    @ParameterizedTest
    @EnumSource
    void onSignClickSneakingAvailableDestination(Action type) throws TranslatableException, NoFormatFoundException, GateConflictException, InvalidStructureException {
        sign = PortalBlockGenerator.generatePortal(new Location(world, 0, 20, 0));
        new PortalBuilder(stargateAPI,player,"destination").setNetwork(network).setGateBuilder(new ImplicitGateBuilder(sign.getLocation(),registry)).build();
        player.setSneaking(true);
        PlayerInteractEvent event = new PlayerInteractEvent(player, type, null, sign, ((Directional) sign.getBlockData()).getFacing());
        Assertions.assertDoesNotThrow(() -> portal.getBehavior().onSignClick(event));
    }

    @Test
    void activate() {
        portal.activate(player);
        Assertions.assertEquals(player.getUniqueId(), portal.getActivatorUUID());
    }

    @Test
    void deactivate() {
        portal.activate(player);
        portal.deactivate();
        Assertions.assertNotEquals(player.getUniqueId(), portal.getActivatorUUID());
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
