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
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.BlockHandlerInterfaceMock;
import org.sgrewritten.stargate.api.Priority;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.network.portal.PortalFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

class BlockEventListenerTest {

    private ServerMock server;
    private RegistryAPI registry;
    private PlayerMock player;
    private WorldMock world;
    private BlockEventListener blockEventListener;
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");
    private static final String PLAYER_NAME = "player";
    private static final String CUSTOM_NETNAME = "custom";
    private StargateAPIMock stargateAPI;
    private NetworkManager networkManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        player = server.addPlayer(PLAYER_NAME);

        world = new WorldMock(Material.GRASS_BLOCK, 0);
        server.addWorld(world);
        GateFormatRegistry.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR)));
        this.stargateAPI = new StargateAPIMock();
        this.registry = stargateAPI.getRegistry();
        this.networkManager = stargateAPI.getNetworkManager();
        Stargate.setServerUUID(UUID.randomUUID());
        blockEventListener = new BlockEventListener(stargateAPI);

        Assertions.assertInstanceOf(WallSign.class, BlockDataMock.mock(Material.ACACIA_WALL_SIGN), " Too old mockbukkit version, requires at least v1.19:1.141.0");

        player.setOp(true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", CUSTOM_NETNAME, PLAYER_NAME})
    void portalCreationDestuctionTest(String networkName) {
        Location bottomLeft = new Location(world, 0, 1, 0);
        Location insidePortal = new Location(world, 0, 2, 0);
        Block signBlock = PortalBlockGenerator.generatePortal(bottomLeft);
        blockEventListener.onSignChange(new SignChangeEvent(signBlock, player, new String[]{"test", "", networkName,
                ""}));


        String netId = switch (networkName) {
            case "" -> StargateNetwork.DEFAULT_NETWORK_ID;
            case CUSTOM_NETNAME -> CUSTOM_NETNAME;
            case PLAYER_NAME -> player.getUniqueId().toString();
            default -> null;
        };

        Network network = registry.getNetwork(netId, false);
        Assertions.assertNotNull(network);
        Assertions.assertNotNull(network.getPortal("test"));
        Assertions.assertNotNull(registry.getPortal(insidePortal));
        blockEventListener.onBlockBreak(new BlockBreakEvent(insidePortal.getBlock(), player));
        Assertions.assertNull(network.getPortal("test"));
        Assertions.assertNull(registry.getPortal(insidePortal));
    }

    @SuppressWarnings("deprecation")
    @Test
    void portalInvalidBlockPlaceTest() {
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
    void cancelBlockBreakTest() {

        Location bottomLeft = new Location(world, 0, 14, 0);
        // Generate blocks for portal
        Block signBlock = PortalBlockGenerator.generatePortal(bottomLeft);
        Block irisBlock = new Location(world, 1, 16, 0).getBlock();
        // generate the portal by modifying the sign.
        blockEventListener.onSignChange(new SignChangeEvent(signBlock, player, new String[]{"test", "", CUSTOM_NETNAME,
                ""}));
        Assertions.assertNotNull(registry.getPortalPosition(signBlock.getLocation()));
        BlockBreakEvent controlBreakEvent = new BlockBreakEvent(signBlock, player);
        blockEventListener.onBlockBreak(controlBreakEvent);
        Assertions.assertTrue(controlBreakEvent.isCancelled());

        BlockBreakEvent irisBreakEvent = new BlockBreakEvent(irisBlock, player);
        blockEventListener.onBlockBreak(irisBreakEvent);
        Assertions.assertTrue(irisBreakEvent.isCancelled());

    }

    @Test
    void blockPlace_registerBlockPosition() throws NameLengthException, InvalidNameException, NameConflictException,
            UnimplementedFlagException, InvalidStructureException {
        Material placedMaterial = Material.DIRT;
        Character flag = 'g';
        BlockHandlerInterfaceMock blockHandler = new BlockHandlerInterfaceMock(PositionType.BUTTON, placedMaterial,
                MockBukkit.createMockPlugin(), Priority.HIGH, flag);
        stargateAPI.getMaterialHandlerResolver().addBlockHandlerInterface(blockHandler);
        Location location = new Location(world, 0, 5, 0);
        Network network = networkManager.createNetwork(CUSTOM_NETNAME, NetworkType.CUSTOM, false, false);
        RealPortal portal = PortalFactory.generateFakePortal(location,
                network, "test", true, new HashSet<>(), Set.of(flag),
                registry);
        network.addPortal(portal);
        Assertions.assertNotNull(registry.getPortal(portal.getGate().getLocations(GateStructureType.FRAME).get(0).getLocation()), "Portal not assigned to registry");

        Location locationNextToPortal = portal.getGate().getLocation(new BlockVector(1, -3, 0));
        Block block = locationNextToPortal.getBlock();
        BlockState replacedBlock = block.getState();
        block.setType(placedMaterial);
        BlockPlaceEvent event = new BlockPlaceEvent(block, replacedBlock, block.getRelative(BlockFace.DOWN), new ItemStack(placedMaterial), player, true);
        blockEventListener.onBlockPlace(event);
        Assertions.assertFalse(event.isCancelled(), "Event should not be cancelled");
        Assertions.assertTrue(blockHandler.blockIsRegistered(locationNextToPortal, player, portal), "Block was not registered to blockhandler");
    }

}


