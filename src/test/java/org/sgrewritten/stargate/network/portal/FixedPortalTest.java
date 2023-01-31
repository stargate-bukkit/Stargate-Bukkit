package org.sgrewritten.stargate.network.portal;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.util.FakeStorage;
import org.sgrewritten.stargate.util.portal.GateTestHelper;

import java.util.HashSet;
import java.util.Set;

public class FixedPortalTest {

    private ServerMock server;
    private MockPlugin plugin;
    private WorldMock world;
    private RegistryAPI registry;
    private Player player;
    private Block sign;
    private Network network;
    private FixedPortal portal;

    @BeforeEach
    void setUp() throws TranslatableException, NoFormatFoundException, GateConflictException {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.createMockPlugin();
        GateTestHelper.setUpGates();
        this.world = server.addSimpleWorld("world");
        this.registry = new StargateRegistry(new FakeStorage());
        this.player = server.addPlayer();
        this.player.addAttachment(plugin, "sg.use", true);
        this.sign = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
        Set<PortalFlag> flags = new HashSet<>();
        flags.add(PortalFlag.FIXED);
        this.network = registry.createNetwork("network", NetworkType.CUSTOM, false, false);
        this.portal = (FixedPortal) FakePortalGenerator.generateFakePortal(sign, network, flags, "fixed", registry);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void open() {
        portal.open(player.getUniqueId());
        Assertions.assertTrue(portal.isOpen());
    }

    @Test
    void open_AlwaysOn() throws TranslatableException, GateConflictException, NoFormatFoundException {
        RealPortal alwaysOnPortal = getAlwaysOnPortal();
        network.updatePortals();
        Assertions.assertTrue(alwaysOnPortal.isOpen());
    }

    @Test
    void close_Force() {
        portal.open(player.getUniqueId());
        portal.close(true);
        Assertions.assertFalse(portal.isOpen());
    }

    @Test
    void close() {
        portal.open(player.getUniqueId());
        portal.close(false);
        Assertions.assertFalse(portal.isOpen());
    }

    @Test
    void open_Activator() {
        portal.open(player.getUniqueId());
        Assertions.assertEquals(player.getUniqueId(), portal.openFor);
    }

    @Test
    void activate() {
        portal.activate(player);
        Assertions.assertEquals(player.getUniqueId(), portal.activator);
    }

    @ParameterizedTest
    @EnumSource
    void setSignColor(DyeColor color) {
        Assertions.assertDoesNotThrow(() -> portal.setSignColor(color));
    }

    @Test
    void getNetwork() {
        Assertions.assertEquals(portal.getNetwork(), network);
    }

    private RealPortal getAlwaysOnPortal() throws TranslatableException, GateConflictException, NoFormatFoundException {
        Block alwaysOnSign = PortalBlockGenerator.generatePortal(new Location(world, 0, 20, 0));
        Set<PortalFlag> alwaysOnFlags = new HashSet<>();
        alwaysOnFlags.add(PortalFlag.FIXED);
        alwaysOnFlags.add(PortalFlag.ALWAYS_ON);
        return FakePortalGenerator.generateFakePortal(alwaysOnSign, network, alwaysOnFlags, "alwaysON", "fixed", "", registry);
    }
}
