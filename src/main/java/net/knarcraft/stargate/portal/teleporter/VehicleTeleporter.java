package net.knarcraft.stargate.portal.teleporter;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.StargateGateConfig;
import net.knarcraft.stargate.event.StargateEntityPortalEvent;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.TeleportHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * The portal teleporter takes care of the actual portal teleportation for any vehicles
 */
public class VehicleTeleporter extends EntityTeleporter {

    private final Vehicle teleportingVehicle;

    /**
     * Instantiates a new vehicle teleporter
     *
     * @param targetPortal       <p>The targetPortal which is the target of the teleportation</p>
     * @param teleportingVehicle <p>The teleporting vehicle</p>
     */
    public VehicleTeleporter(@NotNull Portal targetPortal, @NotNull Vehicle teleportingVehicle) {
        super(targetPortal, teleportingVehicle);
        this.teleportingVehicle = teleportingVehicle;
    }

    /**
     * Teleports a vehicle to this teleporter's portal
     *
     * <p>It is assumed that if a vehicle contains any players, their permissions have already been validated before
     * calling this method.</p>
     *
     * @param origin <p>The portal the vehicle is teleporting from</p>
     * @return <p>True if the vehicle was teleported. False otherwise</p>
     */
    @Override
    public boolean teleportEntity(@NotNull Portal origin) {
        Stargate.debug("VehicleTeleporter::teleport", "Preparing to teleport: " + teleportingVehicle);

        double velocity = teleportingVehicle.getVelocity().length();

        //Stop the vehicle before teleporting
        teleportingVehicle.setVelocity(new Vector());

        //Get new velocity
        Vector newVelocityDirection = DirectionHelper.getDirectionVectorFromYaw(portal.getYaw());
        Vector newVelocity = newVelocityDirection.multiply(velocity);

        //Call the StargateEntityPortalEvent to allow plugins to change destination
        exit = triggerPortalEvent(origin, new StargateEntityPortalEvent(teleportingVehicle, origin, portal, exit));
        if (exit == null) {
            return false;
        }

        //Teleport the vehicle
        return teleportVehicle(exit, newVelocity, origin);
    }

    /**
     * Teleports a vehicle with any passengers to the given location
     *
     * @param exit        <p>The location the vehicle should be teleported to</p>
     * @param newVelocity <p>The velocity to give the vehicle right after teleportation</p>
     * @param origin      <p>The portal the vehicle teleported from</p>
     * @return <p>True if the vehicle was teleported. False otherwise</p>
     */
    private boolean teleportVehicle(@NotNull Location exit, @NotNull Vector newVelocity, @NotNull Portal origin) {
        //Load chunks to make sure not to teleport to the void
        loadChunks();

        List<Entity> passengers = teleportingVehicle.getPassengers();
        if (!passengers.isEmpty()) {
            //Check if the passengers are allowed according to current config settings
            if (!vehiclePassengersAllowed(passengers)) {
                return false;
            }

            if (!(teleportingVehicle instanceof LivingEntity) &&
                    Stargate.getGateConfig().enableCraftBookRemoveOnEjectFix()) {
                //Teleport a normal vehicle with passengers (minecart or boat)
                putPassengersInNewVehicle(passengers, exit, newVelocity, origin);
            } else {
                //Teleport a living vehicle with passengers (pig, horse, donkey, strider)
                teleportVehicle(passengers, exit, newVelocity, origin);
            }
        } else {
            //Check if teleportation of empty vehicles is enabled
            if (!Stargate.getGateConfig().handleEmptyVehicles()) {
                return false;
            }
            //Teleport an empty vehicle
            teleportingVehicle.teleport(exit);
            scheduler.scheduleSyncDelayedTask(Stargate.getInstance(),
                    () -> teleportingVehicle.setVelocity(newVelocity), 1);
        }
        return true;
    }

    /**
     * Checks whether current config values allow the teleportation of the given passengers
     *
     * @param passengers <p>The passengers to teleport</p>
     * @return <p>True if the passengers are allowed to teleport</p>
     */
    private boolean vehiclePassengersAllowed(@NotNull List<Entity> passengers) {
        StargateGateConfig config = Stargate.getGateConfig();
        //Don't teleport if the vehicle contains a creature and creature transportation is disabled
        if (TeleportHelper.containsNonPlayer(passengers) && !config.handleCreatureTransportation()) {
            return false;
        }
        //Don't teleport if the player does not contain a player and non-player vehicles is disabled
        return TeleportHelper.containsPlayer(passengers) || config.handleNonPlayerVehicles();
    }

    /**
     * Teleport a vehicle which is not a minecart or a boat
     *
     * @param passengers  <p>The passengers of the vehicle</p>
     * @param exit        <p>The location the vehicle will exit</p>
     * @param newVelocity <p>The new velocity of the teleported vehicle</p>
     * @param origin      <p>The portal the vehicle teleported from</p>
     */
    private void teleportVehicle(@NotNull List<Entity> passengers, @NotNull Location exit, @NotNull Vector newVelocity,
                                 @NotNull Portal origin) {
        if (teleportingVehicle.eject()) {
            TeleportHelper.handleEntityPassengers(passengers, teleportingVehicle, origin, portal, exit.getDirection(),
                    newVelocity);
        }
        Stargate.debug("VehicleTeleporter::teleportVehicle", "Teleporting " + teleportingVehicle +
                " to final location " + exit + " with direction " + exit.getDirection());
        teleportingVehicle.teleport(exit, PlayerTeleportEvent.TeleportCause.PLUGIN);
        scheduler.scheduleSyncDelayedTask(Stargate.getInstance(),
                () -> {
                    Stargate.debug("VehicleTeleporter::teleportVehicle", "Setting velocity " + newVelocity +
                            " for vehicle " + teleportingVehicle);
                    teleportingVehicle.setVelocity(newVelocity);
                }, 1);
    }

    /**
     * Creates a new vehicle equal to the player's previous vehicle and puts any passengers inside
     *
     * <p>While it is possible to teleport boats and minecarts using the same methods as "teleportLivingVehicle", this
     * method works better with CraftBook with minecart options enabled. Using normal teleportation, CraftBook destroys
     * the minecart once the player is ejected, causing the minecart to disappear and the player to teleport without it.</p>
     *
     * @param passengers  <p>A list of all passengers in the vehicle</p>
     * @param exit        <p>The exit location to spawn the new vehicle on</p>
     * @param newVelocity <p>The new velocity of the new vehicle</p>
     * @param origin      <p>The portal the vehicle teleported from</p>
     */
    private void putPassengersInNewVehicle(@NotNull List<Entity> passengers, @NotNull Location exit,
                                           @NotNull Vector newVelocity, Portal origin) {
        World vehicleWorld = exit.getWorld();
        if (vehicleWorld == null) {
            Stargate.logWarning("Unable to get the world to teleport the vehicle to");
            return;
        }
        //Spawn a new vehicle
        Vehicle newVehicle = (Vehicle) vehicleWorld.spawn(exit,
                Objects.requireNonNull(teleportingVehicle.getType().getEntityClass()));
        if (teleportingVehicle instanceof Boat boat) {
            ((Boat) newVehicle).setBoatType(boat.getBoatType());
        }
        //Remove the old vehicle
        if (teleportingVehicle.eject()) {
            TeleportHelper.handleEntityPassengers(passengers, newVehicle, origin, portal, exit.getDirection(),
                    newVelocity);
        }
        teleportingVehicle.remove();
        scheduler.scheduleSyncDelayedTask(Stargate.getInstance(), () -> newVehicle.setVelocity(newVelocity), 1);
    }

}
