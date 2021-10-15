package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.utility.EconomyHandler;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.EntityHelper;
import net.knarcraft.stargate.utility.PermissionHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.List;

/**
 * This listener listens for the vehicle move event to teleport vehicles through portals
 */
@SuppressWarnings("unused")
public class VehicleEventListener implements Listener {

    /**
     * Check for a vehicle moving through a portal
     *
     * @param event <p>The triggered move event</p>
     */
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!Stargate.handleVehicles) {
            return;
        }
        List<Entity> passengers = event.getVehicle().getPassengers();
        Vehicle vehicle = event.getVehicle();

        Portal entrancePortal;
        int entitySize = EntityHelper.getEntityMaxSizeInt(vehicle);
        if (EntityHelper.getEntityMaxSize(vehicle) > 1) {
            entrancePortal = PortalHandler.getByAdjacentEntrance(event.getTo(), entitySize - 1);
        } else {
            entrancePortal = PortalHandler.getByEntrance(event.getTo());
        }

        //Return if the portal cannot be teleported through
        if (entrancePortal == null || !entrancePortal.isOpen() || entrancePortal.getOptions().isBungee()) {
            return;
        }

        teleportVehicle(passengers, entrancePortal, vehicle);
    }

    /**
     * Teleports a vehicle through a stargate
     *
     * @param passengers     <p>The passengers inside the vehicle</p>
     * @param entrancePortal <p>The portal the vehicle is entering</p>
     * @param vehicle        <p>The vehicle passing through</p>
     */
    private static void teleportVehicle(List<Entity> passengers, Portal entrancePortal, Vehicle vehicle) {
        if (!passengers.isEmpty() && passengers.get(0) instanceof Player) {
            Stargate.logger.info(Stargate.getString("prefix") + "Found passenger vehicle");
            teleportPlayerAndVehicle(entrancePortal, vehicle, passengers);
        } else {
            Stargate.logger.info(Stargate.getString("prefix") + "Found empty vehicle");
            Portal destinationPortal = entrancePortal.getDestination();
            if (destinationPortal == null) {
                Stargate.logger.warning(Stargate.getString("prefix") + "Unable to find portal destination");
                return;
            }
            Stargate.debug("vehicleTeleport", destinationPortal.getWorld() + " " +
                    destinationPortal.getSignLocation());
            destinationPortal.teleport(vehicle, entrancePortal);
        }
    }

    /**
     * Teleports a player and the vehicle the player sits in
     *
     * @param entrancePortal <p>The portal the minecart entered</p>
     * @param vehicle        <p>The vehicle to teleport</p>
     * @param passengers     <p>Any entities sitting in the minecart</p>
     */
    private static void teleportPlayerAndVehicle(Portal entrancePortal, Vehicle vehicle, List<Entity> passengers) {
        Player player = (Player) passengers.get(0);
        if (!entrancePortal.isOpenFor(player)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        Portal destinationPortal = entrancePortal.getDestination(player);
        if (destinationPortal == null) {
            return;
        }

        //Make sure the user can access the portal
        if (PermissionHelper.cannotAccessPortal(player, entrancePortal, destinationPortal)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.close(false);
            return;
        }

        //Transfer payment if necessary
        int cost = EconomyHandler.getDefaultUseCost(player, entrancePortal, destinationPortal);
        if (cost > 0) {
            if (EconomyHelper.cannotPayTeleportFee(entrancePortal, player, cost)) {
                return;
            }
        }

        Stargate.sendSuccessMessage(player, Stargate.getString("teleportMsg"));
        destinationPortal.teleport(vehicle, entrancePortal);
        entrancePortal.close(false);
    }

}
