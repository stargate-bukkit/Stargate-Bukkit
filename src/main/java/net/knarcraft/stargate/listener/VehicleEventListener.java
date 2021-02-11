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

        if (!passengers.isEmpty() && passengers.get(0) instanceof Player) {
            Stargate.log.info(Stargate.getString("prefox") + "Found passenger minecart");
            teleportPlayerAndVehicle(entrancePortal, vehicle, passengers);
        } else {
            Stargate.log.info(Stargate.getString("prefox") + "Found empty minecart");
            Portal destinationPortal = entrancePortal.getDestination();
            if (destinationPortal == null) {
                Stargate.log.warning(Stargate.getString("prefox") + "Unable to find portal destination");
                return;
            }
            destinationPortal.teleport(vehicle);
        }
    }

    /**
     * Teleports a player and the minecart the player sits in
     * @param entrancePortal <p>The portal the minecart entered</p>
     * @param vehicle <p>The vehicle to teleport</p>
     * @param passengers <p>Any entities sitting in the minecart</p>
     */
    private void teleportPlayerAndVehicle(Portal entrancePortal, Vehicle vehicle, List<Entity> passengers) {
        Player player = (Player) passengers.get(0);
        if (!entrancePortal.isOpenFor(player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        Portal destinationPortal = entrancePortal.getDestination(player);
        if (destinationPortal == null) {
            return;
        }
        boolean deny = false;
        // Check if player has access to this network
        if (!Stargate.canAccessNetwork(player, entrancePortal.getNetwork())) {
            deny = true;
        }

        // Check if player has access to destination world
        if (!Stargate.canAccessWorld(player, destinationPortal.getWorld().getName())) {
            deny = true;
        }

        if (!Stargate.canAccessPortal(player, entrancePortal, deny)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            entrancePortal.close(false);
            return;
        }

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
