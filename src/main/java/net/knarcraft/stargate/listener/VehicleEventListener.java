package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.Message;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.teleporter.VehicleTeleporter;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.EntityHelper;
import net.knarcraft.stargate.utility.TeleportHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void onVehicleMove(@NotNull VehicleMoveEvent event) {
        if (!Stargate.getGateConfig().handleVehicles()) {
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
    private static void teleportVehicle(@NotNull List<Entity> passengers, @NotNull Portal entrancePortal,
                                        @NotNull Vehicle vehicle) {
        String route = "VehicleEventListener::teleportVehicle";

        if (!passengers.isEmpty() && TeleportHelper.containsPlayer(passengers)) {
            Stargate.debug(route, "Found passenger vehicle");
            teleportPlayerAndVehicle(entrancePortal, vehicle);
        } else {
            Stargate.debug(route, "Found vehicle without players");
            Portal destinationPortal = entrancePortal.getPortalActivator().getDestination();
            if (destinationPortal == null) {
                Stargate.debug(route, "Unable to find portal destination");
                return;
            }
            Stargate.debug("vehicleTeleport", destinationPortal.getWorld() + " " +
                    destinationPortal.getSignLocation());
            new VehicleTeleporter(destinationPortal, vehicle).teleportEntity(entrancePortal);
        }
    }

    /**
     * Teleports a player and the vehicle the player sits in
     *
     * @param entrancePortal <p>The portal the minecart entered</p>
     * @param vehicle        <p>The vehicle to teleport</p>
     */
    private static void teleportPlayerAndVehicle(@NotNull Portal entrancePortal, @NotNull Vehicle vehicle) {
        Entity rootEntity = vehicle;
        while (rootEntity.getVehicle() != null) {
            rootEntity = rootEntity.getVehicle();
        }
        List<Player> players = TeleportHelper.getPlayers(rootEntity.getPassengers());
        Portal destinationPortal = getDestinationPortal(players, entrancePortal);

        //Cancel the teleport if no players activated the portal, or if any players are denied access
        boolean cancelTeleportation = false;
        for (Player player : players) {
            if (destinationPortal == null) {
                cancelTeleportation = true;
                if (!entrancePortal.getOptions().isSilent()) {
                    Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString(Message.INVALID_DESTINATION));
                }
            } else if (!TeleportHelper.playerCanTeleport(player, entrancePortal, destinationPortal)) {
                cancelTeleportation = true;
            }
        }
        if (cancelTeleportation || destinationPortal == null) {
            return;
        }

        //Take payment from all players
        if (!takePayment(players, entrancePortal, destinationPortal)) {
            return;
        }

        //Teleport the vehicle and inform the user if the vehicle was teleported
        boolean teleported = new VehicleTeleporter(destinationPortal, vehicle).teleportEntity(entrancePortal);
        if (teleported) {
            if (!entrancePortal.getOptions().isSilent()) {
                for (Player player : players) {
                    Stargate.getMessageSender().sendSuccessMessage(player, Stargate.getString(Message.TELEPORTED));
                }
            }
            entrancePortal.getPortalOpener().closePortal(false);
        }
    }

    /**
     * Tries to get the destination portal selected by one of the players included in the teleportation
     *
     * @param players        <p>The players to be teleported</p>
     * @param entrancePortal <p>The portal the players are entering</p>
     * @return <p>The destination portal, or null if not found</p>
     */
    @Nullable
    private static Portal getDestinationPortal(@NotNull List<Player> players, @NotNull Portal entrancePortal) {
        for (Player player : players) {
            //The entrance portal must be open for one player for the teleportation to happen
            if (!entrancePortal.getPortalOpener().isOpenFor(player)) {
                continue;
            }

            //Check if any of the players has selected the destination
            Portal possibleDestinationPortal = entrancePortal.getPortalActivator().getDestination(player);
            if (possibleDestinationPortal != null) {
                return possibleDestinationPortal;
            }
        }

        return null;
    }

    /**
     * Takes payment for the given players
     *
     * @param players           <p>The players to take payment from</p>
     * @param entrancePortal    <p>The portal the players are travelling from</p>
     * @param destinationPortal <p>The portal the players are travelling to</p>
     * @return <p>True if payment was successfully taken, false otherwise</p>
     */
    private static boolean takePayment(@NotNull List<Player> players, @NotNull Portal entrancePortal,
                                       @NotNull Portal destinationPortal) {
        for (Player player : players) {
            //To prevent the case where the first passenger pays and then the second passenger is denied, this has to be
            // run after it has been confirmed that all passengers are able to pay. Also note that some players might 
            // not have to pay, and thus the cost check has to be in the loop,
            int cost = EconomyHelper.getUseCost(player, entrancePortal, destinationPortal);
            if (cost > 0) {
                if (EconomyHelper.cannotPayTeleportFee(entrancePortal, player, cost)) {
                    return false;
                }
            }
        }

        return true;
    }

}
