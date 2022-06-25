package net.TheDgtl.Stargate.listener;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A listener for relevant move events, such as a player entering a stargate
 */
public class MoveEventListener implements Listener {

    private static final PlayerTeleportEvent.TeleportCause[] causesToCheck = {PlayerTeleportEvent.TeleportCause.END_GATEWAY,
            PlayerTeleportEvent.TeleportCause.END_PORTAL, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL};

    /**
     * Listens for and cancels any default vehicle portal events caused by stargates
     *
     * @param event <p>The triggered entity portal event</p>
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityPortalTeleport(@NotNull EntityPortalEvent event) {
        if (Stargate.getRegistryStatic().isNextToPortal(event.getFrom(), GateStructureType.IRIS)) {
            event.setCancelled(true);
        }
    }

    /**
     * Listens for and cancels any default player teleportation events caused by stargates
     *
     * @param event <p>The triggered player teleportation event</p>
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        World world = event.getFrom().getWorld();
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY && (world == null ||
                world.getEnvironment() != World.Environment.THE_END)) {
            return;
        }
        for (PlayerTeleportEvent.TeleportCause causeToCheck : causesToCheck) {
            if (cause != causeToCheck) {
                continue;
            }
            if (Stargate.getRegistryStatic().isNextToPortal(event.getFrom(), GateStructureType.IRIS)
                    || Stargate.getRegistryStatic().getPortal(event.getFrom(), GateStructureType.IRIS) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }


    /**
     * Listens for any player movement and teleports the player if entering a stargate
     *
     * @param event <p>The triggered player move event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        onAnyMove(event.getPlayer(), event.getTo(), event.getFrom());
    }

    /**
     * Listens for any vehicle movement and teleports the vehicle if entering a stargate
     *
     * @param event <p>The triggered vehicle move event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        onAnyMove(event.getVehicle(), event.getTo(), event.getFrom());
    }

    /**
     * Checks if a move event causes the target to enter a stargate, and teleports the target if necessary
     *
     * @param target       <p>The target that moved</p>
     * @param toLocation   <p>The location the target moved to</p>
     * @param fromLocation <p>The location the target moved from</p>
     */
    private void onAnyMove(Entity target, Location toLocation, Location fromLocation) {
        RealPortal portal = null;
        if (toLocation != null && toLocation.getWorld() != null &&
                toLocation.getWorld().getEnvironment() == World.Environment.THE_END) {
            List<Location> relevantLocations = new ArrayList<>();
            Vector targetVelocity = normalizeVelocity(target.getVelocity());
            if (targetVelocity.getX() != 0 && targetVelocity.getY() != 0 && targetVelocity.getZ() != 0) {
                relevantLocations.add(toLocation.clone().add(targetVelocity));
            }
            if (targetVelocity.getX() != 0 && targetVelocity.getZ() != 0) {
                relevantLocations.add(toLocation.clone().add(new Vector(targetVelocity.getX(), 0, targetVelocity.getZ())));
            }
            if (targetVelocity.getX() != 0 && targetVelocity.getY() != 0) {
                relevantLocations.add(toLocation.clone().add(new Vector(targetVelocity.getX(), targetVelocity.getY(), 0)));
            }
            if (targetVelocity.getZ() != 0 && targetVelocity.getY() != 0) {
                relevantLocations.add(toLocation.clone().add(new Vector(0, targetVelocity.getY(), targetVelocity.getZ())));
            }
            if (targetVelocity.getX() != 0) {
                relevantLocations.add(toLocation.clone().add(new Vector(targetVelocity.getX(), 0, 0)));
            }
            if (targetVelocity.getY() != 0) {
                relevantLocations.add(toLocation.clone().add(new Vector(0, targetVelocity.getY(), 0)));
            }
            if (targetVelocity.getZ() != 0) {
                relevantLocations.add(toLocation.clone().add(new Vector(0, 0, targetVelocity.getZ())));
            }
            for (Location headingTo : relevantLocations) {
                RealPortal possiblePortal = Stargate.getRegistryStatic().getPortal(headingTo, GateStructureType.IRIS);
                if (possiblePortal != null && 
                        possiblePortal.getGate().getFormat().getIrisMaterial(true) == Material.END_PORTAL) {
                    Location middle = new Location(headingTo.getWorld(), headingTo.getBlockX() + 0.5,
                            headingTo.getBlockY() + 0.5, headingTo.getBlockZ() + 0.5);
                    if (Math.abs(middle.getX() - toLocation.getX()) < 0.6 ||
                            Math.abs(middle.getY() - toLocation.getY()) < 0.6 ||
                            Math.abs(middle.getZ() - toLocation.getZ()) < 0.6) {
                        portal = possiblePortal;
                        break;
                    }
                }
            }
        }

        // Check if entity moved one block (its only possible to have entered a portal if that's the case)
        if (toLocation == null || portal == null && fromLocation.getBlockX() == toLocation.getBlockX() &&
                fromLocation.getBlockY() == toLocation.getBlockY() &&
                fromLocation.getBlockZ() == toLocation.getBlockZ()) {
            return;
        }

        if (portal == null) {
            portal = Stargate.getRegistryStatic().getPortal(toLocation, GateStructureType.IRIS);
        }
        if (portal == null || !portal.isOpen()) {
            return;
        }

        //Real velocity does not seem to work
        Vector newVelocity = toLocation.toVector().subtract(fromLocation.toVector());
        target.setVelocity(newVelocity);
        Stargate.log(Level.FINER, "Trying to teleport entity, initial velocity: " + target.getVelocity() +
                ", new velocity: " + newVelocity);
        portal.doTeleport(target);
    }

    /**
     * Normalizes the input velocity to a vector of length 1 or 0 in any direction (though the max length of the entire vector is sqrt(2))
     */
    private Vector normalizeVelocity(Vector velocity) {
        Vector normalizedVector = new Vector(0, 0, 0);
        normalizedVector = normalizedVector.setX(velocity.getX() < 0 ? -1 : (velocity.getX() > 0 ? 1 : 0));
        normalizedVector = normalizedVector.setZ(velocity.getZ() < 0 ? -1 : (velocity.getZ() > 0 ? 1 : 0));
        normalizedVector = normalizedVector.setY(velocity.getY() < 0 ? -1 : (velocity.getY() > 0 ? 1 : 0));
        return normalizedVector;
    }

}
