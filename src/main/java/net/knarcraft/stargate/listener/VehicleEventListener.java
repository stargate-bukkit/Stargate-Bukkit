package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.PortalHandler;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.utility.EconomyHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.List;

@SuppressWarnings("unused")
public class VehicleEventListener implements Listener {

    /**
     * Check for a vehicle moving through a portal
     * @param event <p>The triggered move event</p>
     */
    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!Stargate.handleVehicles) {
            return;
        }
        List<Entity> passengers = event.getVehicle().getPassengers();
        Vehicle vehicle = event.getVehicle();

        Portal entrancePortal = PortalHandler.getByEntrance(event.getTo());

        //Return if the portal cannot be teleported through
        if (entrancePortal == null || !entrancePortal.isOpen() || entrancePortal.isBungee()) {
            return;
        }

        //TODO: As there are a lot of vehicles in the game now, a lot needs to be accounted for to make this work as expected

        teleportVehicle(passengers, entrancePortal, vehicle);
    }

    public static void teleportVehicleAfterPlayer(Vehicle vehicle, Portal destinationPortal, Player player) {
        destinationPortal.teleport(vehicle);
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> vehicle.addPassenger(player), 6);
    }

    /**
     * Teleports a vehicle through a stargate
     * @param passengers <p>The passengers inside the vehicle</p>
     * @param entrancePortal <p>The portal the vehicle is entering</p>
     * @param vehicle <p>The vehicle passing through</p>
     */
    public static void teleportVehicle(List<Entity> passengers, Portal entrancePortal, Vehicle vehicle) {
        teleportVehicle(passengers, entrancePortal, vehicle, false);
    }

    /**
     * Teleports a vehicle through a stargate
     * @param passengers <p>The passengers inside the vehicle</p>
     * @param entrancePortal <p>The portal the vehicle is entering</p>
     * @param vehicle <p>The vehicle passing through</p>
     * @param skipOpenCheck <p>Skips the check for whether the portal is open for the player</p>
     */
    public static void teleportVehicle(List<Entity> passengers, Portal entrancePortal, Vehicle vehicle, boolean skipOpenCheck) {
        if (!passengers.isEmpty() && passengers.get(0) instanceof Player) {
            Stargate.log.info(Stargate.getString("prefox") + "Found passenger vehicle");
            teleportPlayerAndVehicle(entrancePortal, vehicle, passengers, skipOpenCheck);
        } else {
            Stargate.log.info(Stargate.getString("prefox") + "Found empty vehicle");
            Portal destinationPortal = entrancePortal.getDestination();
            if (destinationPortal == null) {
                Stargate.log.warning(Stargate.getString("prefox") + "Unable to find portal destination");
                return;
            }
            destinationPortal.teleport(vehicle);
        }
    }

    /**
     * Teleports a player and the vehicle the player sits in
     * @param entrancePortal <p>The portal the minecart entered</p>
     * @param vehicle <p>The vehicle to teleport</p>
     * @param passengers <p>Any entities sitting in the minecart</p>
     */
    private static void teleportPlayerAndVehicle(Portal entrancePortal, Vehicle vehicle, List<Entity> passengers,
                                                 boolean skipOpenCheck) {
        Player player = (Player) passengers.get(0);
        if (!skipOpenCheck && !entrancePortal.isOpenFor(player)) {
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
        destinationPortal.teleport(vehicle);
        entrancePortal.close(false);
    }

}
