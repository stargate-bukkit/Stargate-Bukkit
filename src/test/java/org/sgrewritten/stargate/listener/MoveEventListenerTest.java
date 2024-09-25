package org.sgrewritten.stargate.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockBukkitInject;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.entity.PoweredMinecartMock;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.StargateInject;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.gate.ImplicitGateBuilder;
import org.sgrewritten.stargate.api.network.PortalBuilder;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.NoFormatFoundException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.network.portal.PortalBlockGenerator;

@ExtendWith(StargateExtension.class)
class MoveEventListenerTest {

    @MockBukkitInject
    private @NotNull ServerMock server;
    private MoveEventListener listener;
    private @NotNull PlayerMock player;
    private RealPortal portal;
    private Location iris;
    private @NotNull Location outsideIris;
    private @NotNull PoweredMinecartMock vehicle;
    @StargateInject
    private @NotNull Stargate plugin;

    @BeforeEach
    void setUp() throws TranslatableException, NoFormatFoundException, GateConflictException, InvalidStructureException {
        @NotNull WorldMock theEnd = server.addSimpleWorld("world");
        theEnd.setEnvironment(Environment.THE_END);
        Location from = new Location(theEnd, 0, 0, 0);
        player = server.addPlayer();
        vehicle = (PoweredMinecartMock) theEnd.spawnEntity(from, EntityType.FURNACE_MINECART);
        Block sign = PortalBlockGenerator.generatePortal(new Location(theEnd, 0, 10, 0));
        StargateAPI stargateAPI = new StargateAPIMock();
        portal = new PortalBuilder(stargateAPI, player, "portal").setGateBuilder(new ImplicitGateBuilder(sign.getLocation(), stargateAPI.getRegistry())).setNetwork("network").build();
        listener = new MoveEventListener(stargateAPI.getRegistry());

        iris = portal.getGate().getLocations(GateStructureType.IRIS).get(0).getLocation();
        outsideIris = iris.clone().add(portal.getGate().getFacing().getDirection());
    }


    @Test
    void onPlayerTeleportEndGateway() {
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, outsideIris, iris, PlayerTeleportEvent.TeleportCause.END_GATEWAY);
        listener.onPlayerTeleport(event);
        Assertions.assertTrue(event.isCancelled());
    }

    @Test
    void onPlayerMoveClosed() {
        player.addAttachment(plugin, "sg.use", true);
        PlayerMoveEvent event = new PlayerMoveEvent(player, outsideIris, iris);
        listener.onPlayerMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertFalse(player.hasTeleported());
    }

    @Test
    void onVehicleMoveClosed() {
        VehicleMoveEvent event = new VehicleMoveEvent(vehicle, outsideIris, iris);
        listener.onVehicleMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertFalse(vehicle.hasTeleported());
    }

    @Test
    void onPlayerMove_Open() {
        player.addAttachment(plugin, "sg.use", true);
        portal.overrideDestination(portal);
        portal.open(player);
        PlayerMoveEvent event = new PlayerMoveEvent(player, outsideIris, iris);
        listener.onPlayerMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertTrue(player.hasTeleported());
    }

    @Test
    void onVehicleMoveOpen() {
        VehicleMoveEvent event = new VehicleMoveEvent(vehicle, outsideIris, iris);
        portal.overrideDestination(portal);
        portal.open(player);
        listener.onVehicleMove(event);
        server.getScheduler().performOneTick();
        Assertions.assertTrue(vehicle.hasTeleported());
    }
}
