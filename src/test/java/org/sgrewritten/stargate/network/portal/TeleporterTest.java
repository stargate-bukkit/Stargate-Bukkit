package org.sgrewritten.stargate.network.portal;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.HorseMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.entity.PoweredMinecartMock;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.economy.FakeEconomyManager;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.thread.SynchronousPopulator;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;

import java.io.File;
import java.util.Objects;

class TeleporterTest {
    
    static HorseMock horse;
    private static Teleporter teleporter;
    private static SynchronousPopulator populator;
    private static PoweredMinecartMock furnaceMinecart;
    private static final File testGatesDir = new File("src/test/resources/gates");

    @BeforeAll
    public static void setup() throws NameLengthException, InvalidNameException, InvalidStructureException, UnimplementedFlagException {
        @NotNull ServerMock server = MockBukkit.mock();
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(testGatesDir, new FakeStargateLogger())));
        @NotNull WorldMock world = server.addSimpleWorld("world");
        @NotNull PlayerMock player = server.addPlayer();
        Location destination1 = new Location(world, 10, 10, 10);
        FakePortalGenerator fakePortalGenerator = new FakePortalGenerator("Portal", "iPortal");
        StargateRegistry registry = new StargateRegistry(new FakeStorage());


        horse = (HorseMock) world.spawnEntity(new Location(world, 0, 0, 0), EntityType.HORSE);
        horse.addPassenger(player);
        Network network = new LocalNetwork("custom", NetworkType.CUSTOM);
        RealPortal origin = fakePortalGenerator.generateFakePortal(world, network, "origin", false);
        RealPortal destination = fakePortalGenerator.generateFakePortal(world, network, "destination", false);
        populator = new SynchronousPopulator();
        teleporter = new Teleporter(destination, origin, destination.getGate().getFacing(),
                origin.getGate().getFacing(), 0, "empty", new FakeLanguageManager(), new FakeEconomyManager(),
                (action) -> populator.addAction(action));
        furnaceMinecart = (PoweredMinecartMock) world.spawnEntity(new Location(world, 0, 0, 0), EntityType.MINECART_FURNACE);

    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void teleport() {
        teleporter.teleport(horse);
        while (!populator.hasCompletedAllTasks()) {
            populator.run();
        }
        Assertions.assertTrue(horse.hasTeleported());
    }

    @Test
    public void teleport_FurnaceMinecart() {
        teleporter.teleport(furnaceMinecart);
        while (!populator.hasCompletedAllTasks()) {
            populator.run();
        }
        Assertions.assertTrue(furnaceMinecart.hasTeleported());
    }
}
