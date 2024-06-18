package org.sgrewritten.stargate.listener;

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
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.manager.BlockLoggerMock;
import org.sgrewritten.stargate.manager.StargateBungeeManager;
import org.sgrewritten.stargate.network.RegistryMock;
import org.sgrewritten.stargate.network.StargateNetworkManager;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.util.LanguageManagerMock;
import org.sgrewritten.stargate.util.StargateTestHelper;


class PlayerEventListenerTest {

    private PlayerEventListener listener;
    private Block signBlock;
    private @NotNull PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = StargateTestHelper.setup();
        WorldMock world = server.addSimpleWorld("world");
        player = server.addPlayer();
        RegistryAPI registry = new RegistryMock();
        listener = new PlayerEventListener(new LanguageManagerMock(), registry, new StargateBungeeManager(registry, new LanguageManagerMock(), new StargateNetworkManager(registry, new StorageMock())), new BlockLoggerMock(), new StorageMock());
        signBlock = PortalBlockGenerator.generatePortal(new Location(world, 0, 10, 0));
    }

    @AfterEach
    void tearDown() {
        StargateTestHelper.tearDown();
    }

    @ParameterizedTest
    @EnumSource
    void onPlayerInteractTestSign(Action type) {

        Assertions.assertDoesNotThrow(() -> listener.onPlayerInteract(new PlayerInteractEvent(player, type, null,
                signBlock, ((Directional) signBlock.getBlockData()).getFacing())));
    }

    @ParameterizedTest
    @EnumSource
    void onPlayerInteractTestSignSneaking(Action type) {
        player.setSneaking(true);
        Assertions.assertDoesNotThrow(() -> listener.onPlayerInteract(new PlayerInteractEvent(player, type, null,
                signBlock, ((Directional) signBlock.getBlockData()).getFacing())));
    }
}
