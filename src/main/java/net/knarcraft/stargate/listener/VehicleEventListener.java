package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.VehicleTeleporter;
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
        String route = "VehicleEventListener::teleportVehicle";
        String prefix = Stargate.getString("prefix");

        if (!passengers.isEmpty() && passengers.get(0) instanceof Player) {
            Stargate.debug(route, prefix + "Found passenger vehicle");
            teleportPlayerAndVehicle(entrancePortal, vehicle, passengers);
        } else {
            Stargate.debug(route, prefix + "Found empty vehicle");
            Portal destinationPortal = entrancePortal.getDestination();
            if (destinationPortal == null) {
                Stargate.debug(route, prefix + "Unable to find portal destination");
                return;
            }
            Stargate.debug("vehicleTeleport", destinationPortal.getWorld() + " " +
                    destinationPortal.getSignLocation());
            new VehicleTeleporter(destinationPortal, vehicle).teleport(entrancePortal);
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
        //On the assumption that a non-player cannot sit in the driver's seat and since some portals can only be open
        // to one player at a time, we only need to check if the portal is open to the driver.
        if (!entrancePortal.isOpenFor(player)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        //If no destination exists, the teleportation cannot happen
        Portal destinationPortal = entrancePortal.getDestination(player);
        if (destinationPortal == null) {
            return;
        }

        //Make sure all player passengers are allowed to, and can afford to, enter the portal
        for (Entity entity : passengers) {
            if (entity instanceof Player && !playerCanTeleport((Player) entity, entrancePortal, destinationPortal)) {
                return;
            }
        }

        //To prevent the case where the first passenger pays and then the second passenger is denied, this has to be
        // run after it has been confirmed that all passengers are able to pay
        int cost = EconomyHandler.getUseCost(player, entrancePortal, destinationPortal);
        if (cost > 0) {
            if (!takePlayerPayment(passengers, entrancePortal, cost)) {
                return;
            }
        }

        Stargate.sendSuccessMessage(player, Stargate.getString("teleportMsg"));
        new VehicleTeleporter(destinationPortal, vehicle).teleport(entrancePortal);
        entrancePortal.close(false);
    }

    /**
     * Takes payment from all player passengers
     *
     * @param passengers     <p>All passengers in the teleporting vehicle</p>
     * @param entrancePortal <p>The portal the vehicle is entering from</p>
     * @param cost           <p>The cost each player has to pay</p>
     * @return <p>True if all player passengers paid successfully</p>
     */
    private static boolean takePlayerPayment(List<Entity> passengers, Portal entrancePortal, int cost) {
        for (Entity entity : passengers) {
            //If the passenger is a player, make it pay
            if (entity instanceof Player && EconomyHelper.cannotPayTeleportFee(entrancePortal, (Player) entity, cost)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given player is allowed to and can afford to teleport
     *
     * @param player            <p>The player trying to teleport</p>
     * @param entrancePortal    <p>The portal the player is entering</p>
     * @param destinationPortal <p>The portal the player is to exit from</p>
     * @return <p>True if the player is allowed to teleport and is able to pay necessary fees</p>
     */
    private static boolean playerCanTeleport(Player player, Portal entrancePortal, Portal destinationPortal) {
        //Make sure the user can access the portal
        if (PermissionHelper.cannotAccessPortal(player, entrancePortal, destinationPortal)) {
            Stargate.sendErrorMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.close(false);
            return false;
        }

        //Transfer payment if necessary
        int cost = EconomyHandler.getUseCost(player, entrancePortal, destinationPortal);
        return cost <= 0 || EconomyHandler.canAffordFee(player, cost);
    }

}
