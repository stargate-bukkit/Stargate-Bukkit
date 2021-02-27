package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.EntityHelper;
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
     * If the player teleported, but its vehicle was left behind, make the vehicle teleport to the player
     *
     * @param vehicle <p>The vehicle to teleport</p>
     * @param destinationPortal <p>The portal the player teleported to</p>
     * @param player <p>The player who teleported</p>
     * @param origin <p>The portal the player entered</p>
     */
    public static void teleportVehicleAfterPlayer(Vehicle vehicle, Portal destinationPortal, Player player, Portal origin) {
        destinationPortal.teleport(vehicle, origin);
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> vehicle.addPassenger(player), 6);
    }

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
        if (entrancePortal == null || !entrancePortal.isOpen() || entrancePortal.isBungee()) {
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
            Stargate.log.info(Stargate.getString("prefox") + "Found passenger vehicle");
            teleportPlayerAndVehicle(entrancePortal, vehicle, passengers);
        } else {
            Stargate.log.info(Stargate.getString("prefox") + "Found empty vehicle");
            Portal destinationPortal = entrancePortal.getDestination();
            if (destinationPortal == null) {
                Stargate.log.warning(Stargate.getString("prefox") + "Unable to find portal destination");
                return;
            }
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
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        Portal destinationPortal = entrancePortal.getDestination(player);
        if (destinationPortal == null) {
            return;
        }

        //Make sure the user can access the portal
        if (!Stargate.canAccessPortal(player, entrancePortal, destinationPortal)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.close(false);
            return;
        }

        //Transfer payment if necessary
        int cost = Stargate.getUseCost(player, entrancePortal, destinationPortal);
        if (cost > 0) {
            if (!EconomyHelper.payTeleportFee(entrancePortal, player, cost)) {
                return;
            }
        }

        Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);
        destinationPortal.teleport(vehicle, entrancePortal);
        entrancePortal.close(false);
    }

}
