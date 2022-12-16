package org.sgrewritten.stargate.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

class BlockEventListenerTest {

    private static ServerMock server;
    private static FakeStargate plugin;
    private static RegistryAPI registry;
    private static PlayerMock player;
    private static WorldMock world;
    private static BlockEventListener blockEventListener;
    private static Block signBlock;
    private static Location bottomLeft;
    private static Location insidePortal;
    private static @NotNull Block irisBlock;
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");
    private static final String PLAYER_NAME = "player";
    private static final String CUSTOM_NETNAME = "custom";

    @BeforeAll
    public static void setUp() throws NameLengthException, InvalidStructureException, InvalidNameException, FileNotFoundException, IOException, InvalidConfigurationException{
        BlockEventListenerTest.server = MockBukkit.mock();

        plugin = (FakeStargate) MockBukkit.load(FakeStargate.class);
        player = server.addPlayer(PLAYER_NAME);
        
        world = new WorldMock(Material.GRASS, 0);
        server.addWorld(world);
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR, new FakeStargateLogger())));
        registry = new StargateRegistry(new FakeStorage());
        Stargate.setServerUUID(UUID.randomUUID());
        blockEventListener = new BlockEventListener(registry, new FakeLanguageManager());
        
        bottomLeft = new Location(world, 0, 1, 0);
        insidePortal = new Location(world, 0, 2, 0);
        signBlock = PortalBlockGenerator.generatePortal(bottomLeft);
        irisBlock = new Location(world, 0, 2, 1).getBlock();
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
        String[] netNames = { "", CUSTOM_NETNAME, player.getName() };
        for (String netName : netNames) {
            blockEventListener
                    .onSignChange(new SignChangeEvent(signBlock, player, new String[] { "test", "", netName, "" }));
            
            
            String netId = null;
            switch (netName) {
            case "":
                netId = LocalNetwork.DEFAULT_NET_ID;
                break;
            case CUSTOM_NETNAME:
                netId = CUSTOM_NETNAME;
                break;
            case PLAYER_NAME:
                netId = player.getUniqueId().toString();
                break;
            }
            

            Network network = registry.getNetwork(netId, false);
            Stargate.log(Level.FINEST, registry.getNetworkMap().keySet().toString());
            ((Directional) signBlock.getBlockData()).setFacing(BlockFace.SOUTH); //TODO Why does this need to be done?
            Assertions.assertNotNull(network.getPortal("test"));
            Assertions.assertNotNull(registry.getPortal(insidePortal));
            blockEventListener.onBlockBreak(new BlockBreakEvent(insidePortal.getBlock(), player));
            insidePortal.getBlock().setType(Material.OBSIDIAN);
            Assertions.assertNull(network.getPortal("test"));
            Assertions.assertNull(registry.getPortal(insidePortal));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void portalInvalidBlockPlaceTest() {
        blockEventListener
                .onSignChange(new SignChangeEvent(signBlock, player, new String[] { "test", "", CUSTOM_NETNAME, "" }));
        blockEventListener.onBlockPlace(new BlockPlaceEvent(irisBlock, irisBlock.getState(), irisBlock,
                Bukkit.getItemFactory().createItemStack("minecraft:oak_logs"), player, false));
    }
}
