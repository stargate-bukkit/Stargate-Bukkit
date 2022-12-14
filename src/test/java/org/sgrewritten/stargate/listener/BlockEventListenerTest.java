package org.sgrewritten.stargate.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargate;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import be.seeseemelk.mockbukkit.block.data.WallSignMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

class BlockEventListenerTest {

    private static ServerMock server;
    private static FakeStargate plugin;
    private static RegistryAPI registry;
    private static PlayerMock player;
    private static WorldMock world;
    private static BlockEventListener blockEventListener;
    private static Block signBlock;
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    @BeforeAll
    public static void setUp() throws NameLengthException, InvalidStructureException, InvalidNameException, FileNotFoundException, IOException, InvalidConfigurationException{
        BlockEventListenerTest.server = MockBukkit.mock();

        plugin = (FakeStargate) MockBukkit.load(FakeStargate.class);
        player = server.addPlayer("player");
        
        world = new WorldMock(Material.GRASS, 0);
        server.addWorld(world);
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR, new FakeStargateLogger())));
        registry = new StargateRegistry(new FakeStorage());
        Stargate.setServerUUID(UUID.randomUUID());
        blockEventListener = new BlockEventListener(registry, new FakeLanguageManager());
        
        int[][] portal = {
                {1,1,1,1},
                {1,0,0,1},
                {1,0,0,1},
                {1,0,0,1},
                {1,1,1,1}};
        for(int y = 0; y < portal.length; y++) {
            for(int x = 0; x < portal[y].length; x++) {
                if(portal[y][x] == 0) {
                    continue;
                }
                Location location = new Location(world,x,y+2,0);
                location.getBlock().setType(Material.OBSIDIAN);
            }
        }
        signBlock = world.getBlockAt(new Location(world,0,4,1));
        signBlock.setType(Material.ACACIA_WALL_SIGN);
        ((WallSign)signBlock.getBlockData()).setFacing(BlockFace.SOUTH);
        Stargate.log(Level.FINEST, signBlock.getBlockData().getAsString());
        Assertions.assertInstanceOf(WallSign.class,BlockDataMock.mock(Material.ACACIA_WALL_SIGN), " Too old mockbukkit version, requires at least v1.19:1.139.0");
        
    }
    
    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    public void portalCreationDestuctionTest() {
        player.setOp(true);
        blockEventListener.onSignChange(new SignChangeEvent(signBlock,player,new String[] {"test", "", "", ""}));
        Assertions.assertNotNull(registry.getNetwork(LocalNetwork.DEFAULT_NET_ID, false).getPortal("test"));
        blockEventListener.onBlockBreak(new BlockBreakEvent(new Location(world, 0, 0, 0).getBlock(),player));
        Assertions.assertNull(registry.getNetwork(LocalNetwork.DEFAULT_NET_ID, false).getPortal("test"));
    }
}
