package net.TheDgtl.Stargate.listeners;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * A listener for relevant move events, such as a player entering a stargate
 */
public class MoveEventListener implements Listener {

    /**
     * Listens for and cancels any default vehicle portal events caused by stargates
     *
     * @param event <p>The triggered entity portal event</p>
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityPortalTeleport(@NotNull EntityPortalEvent event) {
        if (Stargate.getRegistry().isNextToPortal(event.getFrom(), GateStructureType.IRIS)) {
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
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        World world = event.getFrom().getWorld();

        if (cause != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL &&
                (cause != PlayerTeleportEvent.TeleportCause.END_GATEWAY || world == null ||
                        world.getEnvironment() != World.Environment.THE_END)) {
            return;
        }

        if (Stargate.getRegistry().isNextToPortal(event.getFrom(), GateStructureType.IRIS)) {
            event.setCancelled(true);
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
        if(ConfigurationHelper.getBoolean(ConfigurationOption.HANDLE_VEHICLES))
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
        // Check if entity moved one block (its only possible to have entered a portal if that's the case)
        if (toLocation == null ||
                fromLocation.getBlockX() == toLocation.getBlockX() &&
                        fromLocation.getBlockY() == toLocation.getBlockY() &&
                        fromLocation.getBlockZ() == toLocation.getBlockZ()) {
            return;
        }

        Portal portal = Stargate.getRegistry().getPortal(toLocation, GateStructureType.IRIS);
        if (portal == null || !portal.isOpen()) {
            return;
        }

        //Real velocity does not seem to work
        target.setVelocity(toLocation.toVector().subtract(fromLocation.toVector()));
        Stargate.log(Level.FINER, "Trying to teleport entity, initial velocity: " + target.getVelocity());
        portal.doTeleport(target);
    }

}
