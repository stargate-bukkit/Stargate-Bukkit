package org.sgrewritten.stargate.listener;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.name.BungeeNameException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.FakePortalGenerator;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;
import org.sgrewritten.stargate.util.FakeStorage;
import org.sgrewritten.stargate.util.portal.GateTestHelper;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.entity.PoweredMinecartMock;

class MoveEventListenerTest {

    private @NotNull ServerMock server;
    private Block sign;
    private StargateRegistry registry;
    private MoveEventListener listener;
    private @NotNull PlayerMock player;
    private RealPortal portal;
    private Location iris;
    private Location from;
    private @NotNull Location outsideIris;
    private Location teleportDestination;
    private @NotNull WorldMock theEnd;
    private @NotNull PoweredMinecartMock vehicle;
    private @NotNull Stargate plugin;

    @BeforeEach
    void setUp() throws NameLengthException, BungeeNameException, NameConflictException, InvalidNameException, NoFormatFoundException, GateConflictException {
        System.setProperty("bstats.relocatecheck", "false");
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Stargate.class);
        GateTestHelper.setUpGates();
        theEnd = server.addSimpleWorld("world");
        theEnd.setEnvironment(Environment.THE_END);
        from = new Location(theEnd,0,0,0);
        player = server.addPlayer();
        vehicle = (PoweredMinecartMock) theEnd.spawnEntity(from,EntityType.MINECART_FURNACE);
        sign = PortalBlockGenerator.generatePortal(new Location(theEnd,0,10,0));
        registry = new StargateRegistry(new FakeStorage());
        portal = FakePortalGenerator.generateFakePortal(sign, "network", new HashSet<>(), "portal", registry);
        listener = new MoveEventListener(registry);
        
        iris = portal.getGate().getLocations(GateStructureType.IRIS).get(0).getLocation();
        outsideIris = iris.clone().add(portal.getGate().getFacing().getDirection());
        teleportDestination = new Location(theEnd,400,0,0);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    void onPlayerTeleport_EndGateway() {
        PlayerTeleportEvent event = new PlayerTeleportEvent(player,outsideIris,iris,PlayerTeleportEvent.TeleportCause.END_GATEWAY);
        listener.onPlayerTeleport(event);
        Assertions.assertTrue(event.isCancelled());
    }
    
    @Test
    void onPlayerMove_Closed() {
        player.addAttachment(plugin,"sg.use",true);
        PlayerMoveEvent event = new PlayerMoveEvent(player,outsideIris,iris);
        listener.onPlayerMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertFalse(player.hasTeleported());
    }
    
    @Test
    void onVehicleMove_Closed() {
        VehicleMoveEvent event = new VehicleMoveEvent(vehicle,outsideIris,iris);
        listener.onVehicleMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertFalse(vehicle.hasTeleported());
    }
    
    @Test
    void onPlayerMove_Open() {
        player.addAttachment(plugin,"sg.use",true);
        portal.overrideDestination(portal);
        portal.open(player);
        PlayerMoveEvent event = new PlayerMoveEvent(player,outsideIris,iris);
        listener.onPlayerMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertTrue(player.hasTeleported());
    }
    
    @Test
    void onVehicleMove_Open() {
        VehicleMoveEvent event = new VehicleMoveEvent(vehicle,outsideIris,iris);
        portal.overrideDestination(portal);
        portal.open(player);
        listener.onVehicleMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertTrue(vehicle.hasTeleported());
    }
}
