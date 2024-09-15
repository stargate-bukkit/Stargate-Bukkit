package org.sgrewritten.stargate.network.portal;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.HorseMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.entity.PoweredMinecartMock;
import be.seeseemelk.mockbukkit.scheduler.BukkitSchedulerMock;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.api.gate.ExplicitGateBuilder;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.economy.StargateEconomyManagerMock;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateNetwork;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.thread.SynchronousPopulator;
import org.sgrewritten.stargate.util.LanguageManagerMock;
import org.sgrewritten.stargate.util.StargateTestHelper;


class TeleporterTest {

    private HorseMock horse;
    private static Teleporter teleporter;
    private static SynchronousPopulator populator;
    private static PoweredMinecartMock furnaceMinecart;
    private ServerMock server;
    private BukkitSchedulerMock scheduler;
    private StargateAPIMock stargateAPI;

    @BeforeEach
    public void setup() throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        this.server = StargateTestHelper.setup();
        this.scheduler = server.getScheduler();
        WorldMock world = server.addSimpleWorld("world");
        PlayerMock player = server.addPlayer();
        this.stargateAPI = new StargateAPIMock();


        horse = (HorseMock) world.spawnEntity(new Location(world, 0, 0, 0), EntityType.HORSE);
        horse.addPassenger(player);
        Network network = new StargateNetwork("custom", NetworkType.CUSTOM, StorageType.LOCAL);
        RealPortal origin = generatePortal(network, "origin", new Location(world, 0, 10, 0));
        RealPortal destination = generatePortal(network, "destination", new Location(world, 0, 20, 0));
        populator = new SynchronousPopulator();
        teleporter = new Teleporter(destination, origin, destination.getGate().getFacing(),
                origin.getGate().getFacing(), 0, "empty", new LanguageManagerMock(), new StargateEconomyManagerMock());
        furnaceMinecart = (PoweredMinecartMock) world.spawnEntity(new Location(world, 0, 0, 0), EntityType.FURNACE_MINECART);

    }

    private RealPortal generatePortal(Network network, String name, Location location) throws TranslatableException, InvalidStructureException, GateConflictException, NoFormatFoundException {
        TestPortalBuilder testPortalBuilder = new TestPortalBuilder(stargateAPI.getRegistry(), location.getWorld());
        testPortalBuilder.setGateBuilder(new ExplicitGateBuilder(stargateAPI.getRegistry(), location, GateFormatRegistry.getFormat("nether.gate")));
        testPortalBuilder.setName(name).setNetwork(network);
        return testPortalBuilder.build();
    }

    @AfterEach
    public void tearDown() {
        StargateTestHelper.tearDown();
    }

    @Test
    void teleport() {
        teleporter.teleport(horse);
        StargateTestHelper.runAllTasks();
        Assertions.assertTrue(horse.hasTeleported());
    }

    @Test
    void teleport_FurnaceMinecart() {
        teleporter.teleport(furnaceMinecart);
        StargateTestHelper.runAllTasks();
        Assertions.assertTrue(furnaceMinecart.hasTeleported());
    }
}
