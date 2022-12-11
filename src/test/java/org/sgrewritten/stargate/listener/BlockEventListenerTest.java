package org.sgrewritten.stargate.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargate;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeRegistry;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

class BlockEventListenerTest {

    private static ServerMock server;
    private static FakeStargate plugin;
    private static RegistryAPI registry;
    private static PlayerMock player;
    private static WorldMock world;
    private static BlockEventListener blockBreakEventListener;
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    @BeforeAll
    public static void setUp() throws NameLengthException, InvalidStructureException, InvalidNameException, FileNotFoundException, IOException, InvalidConfigurationException{
        BlockEventListenerTest.server = MockBukkit.mock();

        plugin = (FakeStargate) MockBukkit.load(FakeStargate.class);
        player = server.addPlayer("player");
        world = new WorldMock(Material.OBSIDIAN, 2);
        server.addWorld(world);
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR, new FakeStargateLogger())));
        registry = new FakeRegistry(world);
        Stargate.setServerUUID(UUID.randomUUID());
        blockBreakEventListener = new BlockEventListener(registry, new FakeLanguageManager());
    }
    
    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    public void onBlockBreakTest() {
        player.setOp(true);
        blockBreakEventListener.onBlockBreak(new BlockBreakEvent(new Location(world, 0, 0, 0).getBlock(),player));
    }

}
