package org.sgrewritten.stargate.network.portal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FakeStorage;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

class TeleporterTest {

    private @NotNull
    static ServerMock server;
    private @NotNull
    static PlayerMock player;
    private @NotNull
    static WorldMock world;
    private static Location destination;
    private @NotNull
    static Entity vehicle;
    private static Teleporter teleporter;
    private static FakePortalGenerator fakePortalGenerator;
    private static StargateRegistry registry;
    private static final File testGatesDir = new File("src/test/resources/gates");
    
    @BeforeAll
    public static void setup() throws NameLengthException, InvalidNameException, InvalidStructureException{
        server = MockBukkit.mock();
        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(testGatesDir, new FakeStargateLogger())));
        world = server.addSimpleWorld("world");
        player = server.addPlayer();
        destination = new Location(world,10,10,10);
        fakePortalGenerator = new FakePortalGenerator("Portal","iPortal");
        registry = new StargateRegistry(new FakeStorage());
        
        
        vehicle = world.spawnEntity(new Location(world,0,0,0), EntityType.HORSE);
        vehicle.addPassenger(player);
        Network network = new LocalNetwork("custom",NetworkType.CUSTOM);
        RealPortal origin = fakePortalGenerator.generateFakePortal(world, network, "origin", false);
        RealPortal destination = fakePortalGenerator.generateFakePortal(world, network, "destination", false);
        teleporter = new Teleporter(destination, origin, destination.getGate().getFacing(), origin.getGate().getFacing(), 0, "empty", new FakeLanguageManager());
    }
    
    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void teleport() {
        teleporter.teleport(player);
    }
}
