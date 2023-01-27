package org.sgrewritten.stargate.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargate;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.economy.FakeEconomyManager;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

class BlockEventListenerTest {

    private static ServerMock server;
    private static RegistryAPI registry;
    private static PlayerMock player;
    private static WorldMock world;
    private static BlockEventListener blockEventListener;
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");
    private static final String PLAYER_NAME = "player";
    private static final String CUSTOM_NETNAME = "custom";

    @BeforeEach
    public void setUp() {
        BlockEventListenerTest.server = MockBukkit.mock();

        MockBukkit.load(FakeStargate.class);
        player = server.addPlayer(PLAYER_NAME);

        world = new WorldMock(Material.GRASS, 0);
        server.addWorld(world);
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR, new FakeStargateLogger())));
        registry = new StargateRegistry(new FakeStorage());
        Stargate.setServerUUID(UUID.randomUUID());
        blockEventListener = new BlockEventListener(registry, new FakeLanguageManager(), new FakeEconomyManager());

        Assertions.assertInstanceOf(WallSign.class, BlockDataMock.mock(Material.ACACIA_WALL_SIGN), " Too old mockbukkit version, requires at least v1.19:1.141.0");

        player.setOp(true);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void portalCreationDestuctionTest() {
        Location bottomLeft = new Location(world, 0, 1, 0);
        Location insidePortal = new Location(world, 0, 2, 0);
        Block signBlock = PortalBlockGenerator.generatePortal(bottomLeft);


        String[] networkNames = {"", CUSTOM_NETNAME, player.getName()};
        for (String networkName : networkNames) {
            blockEventListener.onSignChange(new SignChangeEvent(signBlock, player, new String[]{"test", "", networkName,
                    ""}));


            String netId = switch (networkName) {
                case "" -> LocalNetwork.DEFAULT_NETWORK_ID;
                case CUSTOM_NETNAME -> CUSTOM_NETNAME;
                case PLAYER_NAME -> player.getUniqueId().toString();
                default -> null;
            };


            ((Directional) signBlock.getBlockData()).setFacing(BlockFace.SOUTH); //TODO Why does this need to be done?
            Network network = registry.getNetwork(netId, false);
            Assertions.assertNotNull(network);
            Assertions.assertNotNull(network.getPortal("test"));
            Assertions.assertNotNull(registry.getPortal(insidePortal));
            blockEventListener.onBlockBreak(new BlockBreakEvent(insidePortal.getBlock(), player));
            Assertions.assertNull(network.getPortal("test"));
            Assertions.assertNull(registry.getPortal(insidePortal));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void portalInvalidBlockPlaceTest() {
        Location bottomLeft = new Location(world, 0, 7, 0);
        Location insidePortal = new Location(world, 0, 9, 0);
        Block signBlock = PortalBlockGenerator.generatePortal(bottomLeft);
        Block irisBlock = new Location(world, 1, 9, 0).getBlock();


        ((Directional) signBlock.getBlockData()).setFacing(BlockFace.SOUTH);
        blockEventListener
                .onSignChange(new SignChangeEvent(signBlock, player, new String[]{"test", "", CUSTOM_NETNAME, ""}));

        BlockPlaceEvent event = new BlockPlaceEvent(irisBlock, irisBlock.getState(), irisBlock,
                new ItemStack(Material.ANDESITE), player, false);
        blockEventListener.onBlockPlace(event);
        blockEventListener.onBlockBreak(new BlockBreakEvent(insidePortal.getBlock(), player));
        Assertions.assertTrue(event.isCancelled());
    }

    @Test
    public void cancelBlockBreakTest() {

        Location bottomLeft = new Location(world, 0, 14, 0);
        Block signBlock = PortalBlockGenerator.generatePortal(bottomLeft);
        Block irisBlock = new Location(world, 1, 16, 0).getBlock();
        blockEventListener.onSignChange(new SignChangeEvent(signBlock, player, new String[]{"test", "", CUSTOM_NETNAME,
                ""}));

        BlockBreakEvent controlBreakEvent = new BlockBreakEvent(signBlock, player);
        blockEventListener.onBlockBreak(controlBreakEvent);
        Assertions.assertTrue(controlBreakEvent.isCancelled());

        BlockBreakEvent irisBreakEvent = new BlockBreakEvent(irisBlock, player);
        blockEventListener.onBlockBreak(irisBreakEvent);
        Assertions.assertTrue(irisBreakEvent.isCancelled());

    }
}


