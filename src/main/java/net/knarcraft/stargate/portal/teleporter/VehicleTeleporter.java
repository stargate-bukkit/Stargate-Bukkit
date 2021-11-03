package net.knarcraft.stargate.portal.teleporter;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.StargateGateConfig;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.utility.DirectionHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * The portal teleporter takes care of the actual portal teleportation for any vehicles
 */
public class VehicleTeleporter extends EntityTeleporter {

    private final Vehicle teleportingVehicle;

    /**
     * Instantiates a new vehicle teleporter
     *
     * @param portal             <p>The portal which is the target of the teleportation</p>
     * @param teleportingVehicle <p>The teleporting vehicle</p>
     */
    public VehicleTeleporter(Portal portal, Vehicle teleportingVehicle) {
        super(portal, teleportingVehicle);
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
    public boolean teleport(Portal origin) {
        Location traveller = teleportingVehicle.getLocation();
        Location exit = getExit(teleportingVehicle, traveller);

        double velocity = teleportingVehicle.getVelocity().length();

        //Stop and teleport
        teleportingVehicle.setVelocity(new Vector());

        //Get new velocity
        Vector newVelocityDirection = DirectionHelper.getDirectionVectorFromYaw(portal.getYaw());
        Vector newVelocity = newVelocityDirection.multiply(velocity);

        //Make sure the vehicle points out from the portal
        adjustRotation(exit);

        //Call the StargateEntityPortalEvent to allow plugins to change destination
        if (!origin.equals(portal)) {
            exit = triggerEntityPortalEvent(origin, exit);
            if (exit == null) {
                return false;
            }
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
    private boolean teleportVehicle(Location exit, Vector newVelocity, Portal origin) {
        //Load chunks to make sure not to teleport to the void
        loadChunks();

        List<Entity> passengers = teleportingVehicle.getPassengers();
        if (!passengers.isEmpty()) {
            //Check if the passengers are allowed according to current config settings
            if (!vehiclePassengersAllowed(passengers)) {
                return false;
            }

            if (!(teleportingVehicle instanceof LivingEntity)) {
                //Teleport a normal vehicle with passengers (minecart or boat)
                putPassengersInNewVehicle(passengers, exit, newVelocity, origin);
            } else {
                //Teleport a living vehicle with passengers (pig, horse, donkey, strider)
                teleportLivingVehicle(exit, passengers, origin);
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
    private boolean vehiclePassengersAllowed(List<Entity> passengers) {
        StargateGateConfig config = Stargate.getGateConfig();
        //Don't teleport if the vehicle contains a creature and creature transportation is disabled
        if (containsNonPlayer(passengers) && !config.handleCreatureTransportation()) {
            return false;
        }
        //Don't teleport if the player does not contain a player and non-player vehicles is disabled
        return containsPlayer(passengers) || config.handleNonPlayerVehicles();
    }

    /**
     * Checks whether a list of entities contains any non-players
     *
     * @param entities <p>The list of entities to check</p>
     * @return <p>True if at least one entity is not a player</p>
     */
    private boolean containsNonPlayer(List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a list of entities contains at least one player
     *
     * @param entities <p>The list of entities to check</p>
     * @return <p>True if at least one player is present among the passengers</p>
     */
    private boolean containsPlayer(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                return true;
            }
        }
        return false;
    }

    /**
     * Teleport a vehicle which is not a minecart or a boat
     *
     * @param exit       <p>The location the vehicle will exit</p>
     * @param passengers <p>The passengers of the vehicle</p>
     * @param origin     <p>The portal the vehicle teleported from</p>
     */
    private void teleportLivingVehicle(Location exit, List<Entity> passengers, Portal origin) {
        teleportingVehicle.eject();
        teleportingVehicle.teleport(exit);
        handleVehiclePassengers(passengers, teleportingVehicle, 2, origin);
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
    private void putPassengersInNewVehicle(List<Entity> passengers, Location exit,
                                           Vector newVelocity, Portal origin) {
        World vehicleWorld = exit.getWorld();
        if (vehicleWorld == null) {
            Stargate.logWarning("Unable to get the world to teleport the vehicle to");
            return;
        }
        //Spawn a new vehicle
        Vehicle newVehicle = vehicleWorld.spawn(exit, teleportingVehicle.getClass());
        //Remove the old vehicle
        teleportingVehicle.eject();
        teleportingVehicle.remove();
        //Set rotation, add passengers and restore velocity
        newVehicle.setRotation(exit.getYaw(), exit.getPitch());
        handleVehiclePassengers(passengers, newVehicle, 1, origin);
        scheduler.scheduleSyncDelayedTask(Stargate.getInstance(), () -> newVehicle.setVelocity(newVelocity), 1);
    }

    /**
     * Ejects, teleports and adds all passengers to the target vehicle
     *
     * @param passengers <p>The passengers to handle</p>
     * @param vehicle    <p>The vehicle the passengers should be put into</p>
     * @param delay      <p>The amount of milliseconds to wait before adding the vehicle passengers</p>
     * @param origin     <p>The portal the vehicle teleported from</p>
     */
    private void handleVehiclePassengers(List<Entity> passengers, Vehicle vehicle, long delay, Portal origin) {
        for (Entity passenger : passengers) {
            passenger.eject();
            scheduler.scheduleSyncDelayedTask(Stargate.getInstance(), () -> {
                if (passenger instanceof Player player) {
                    //Teleport any creatures leashed by the player in a 15-block range
                    teleportLeashedCreatures(player, origin);
                }
                teleportAndAddPassenger(vehicle, passenger);
            }, delay);
        }
    }

    /**
     * Teleports and adds a passenger to a vehicle
     *
     * <p>Teleportation of living vehicles is really buggy if you wait between the teleportation and passenger adding,
     * but there needs to be a delay between teleporting the vehicle and teleporting and adding the passenger.</p>
     *
     * @param targetVehicle <p>The vehicle to add the passenger to</p>
     * @param passenger     <p>The passenger to teleport and add</p>
     */
    private void teleportAndAddPassenger(Vehicle targetVehicle, Entity passenger) {
        if (!passenger.teleport(targetVehicle.getLocation())) {
            Stargate.debug("handleVehiclePassengers", "Failed to teleport passenger");
        }
        if (!targetVehicle.addPassenger(passenger)) {
            Stargate.debug("handleVehiclePassengers", "Failed to add passenger");
        }
    }

}
