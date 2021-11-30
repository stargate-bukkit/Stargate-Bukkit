package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.Portal;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.NotNull;

public class MoveEventListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityPortalTeleport(@NotNull EntityPortalEvent event) {
        if (!(event.getEntity() instanceof Vehicle))
            return;

        if (Network.isNextToPortal(event.getFrom(), GateStructureType.IRIS)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        // cancel portal and end-gateway teleportation if it's from a Stargate entrance
        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        /*
         * A refactor of the legacy version, I don't know the exact purpose of the
         * end_gateway logic, but it's there now, This is done to avoid players from
         * teleporting in the vanilla way:
         *
         * Check if the cause is one of the critical scenarios, if not the case return.
         */

        switch (cause) {
            case END_GATEWAY:
                if ((World.Environment.THE_END == event.getFrom().getWorld().getEnvironment())) {
                    break;
                }
                return;
            case NETHER_PORTAL:
                break;
            default:
                return;
        }
        if (Network.isNextToPortal(event.getFrom(), GateStructureType.IRIS)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        onAnyMove(event.getPlayer(), event.getTo(), event.getFrom());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        //TODO: not currently implemented
        if (event.getVehicle() instanceof PoweredMinecart)
            return;
        onAnyMove(event.getVehicle(), event.getTo(), event.getFrom());
    }

    private void onAnyMove(Entity target, Location to, Location from) {
        // Check if entity moved one block (its only possible to have entered a portal if that's the case)
        if (to == null || from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Portal portal = Network.getPortal(to, GateStructureType.IRIS);
        if (portal == null || !portal.isOpen())
            return;

        /*
         * Real velocity does not seem to work
         */
        target.setVelocity(to.toVector().subtract(from.toVector()));
        portal.onIrisEntrance(target);
    }
}
