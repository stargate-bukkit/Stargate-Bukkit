package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateEntityPortalEvent;
import net.knarcraft.stargate.utility.DirectionHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * The portal teleporter takes care of the actual portal teleportation for any vehicles
 */
public class VehicleTeleporter extends Teleporter {

    private final Vehicle teleportingVehicle;

    /**
     * Instantiates a new vehicle teleporter
     *
     * @param portal             <p>The portal which is the target of the teleportation</p>
     * @param teleportingVehicle <p>The teleporting vehicle</p>
     */
    public VehicleTeleporter(Portal portal, Vehicle teleportingVehicle) {
        super(portal);
        this.teleportingVehicle = teleportingVehicle;
    }

    /**
     * Teleports a vehicle to this teleporter's portal
     *
     * <p>It is assumed that if a vehicle contains any players, their permissions have already been validated before
     * calling this method.</p>
     *
     * @param origin <p>The portal the vehicle teleports from</p>
     */
    public void teleport(Portal origin) {
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
                return;
            }
        }

        //Teleport the vehicle
        teleportVehicle(exit, newVelocity);
    }

    /**
     * Teleports a vehicle with any passengers to the given location
     *
     * @param exit        <p>The location the vehicle should be teleported to</p>
     * @param newVelocity <p>The velocity to give the vehicle right after teleportation</p>
     */
    private void teleportVehicle(Location exit, Vector newVelocity) {
        //Load chunks to make sure not to teleport to the void
        loadChunks();

        List<Entity> passengers = teleportingVehicle.getPassengers();
        if (!passengers.isEmpty()) {
            if (!(teleportingVehicle instanceof LivingEntity)) {
                //Teleport a normal vehicle with passengers (minecart or boat)
                putPassengersInNewVehicle(passengers, exit, newVelocity);
            } else {
                //Teleport a living vehicle with passengers (pig, horse, donkey, strider)
                teleportLivingVehicle(exit, passengers);
            }
        } else {
            //Teleport an empty vehicle
            teleportingVehicle.teleport(exit);
            scheduler.scheduleSyncDelayedTask(Stargate.stargate, () -> teleportingVehicle.setVelocity(newVelocity), 1);
        }
    }

    /**
     * Triggers the entity portal event to allow plugins to change the exit location
     *
     * @param origin <p>The origin portal teleported from</p>
     * @param exit   <p>The exit location to teleport the vehicle to</p>
     * @return <p>The location the vehicle should be teleported to, or null if the event was cancelled</p>
     */
    private Location triggerEntityPortalEvent(Portal origin, Location exit) {
        StargateEntityPortalEvent stargateEntityPortalEvent = new StargateEntityPortalEvent(teleportingVehicle, origin,
                portal, exit);
        Stargate.server.getPluginManager().callEvent(stargateEntityPortalEvent);
        //Teleport is cancelled. Teleport the entity back to where it came from just for sanity's sake
        if (stargateEntityPortalEvent.isCancelled()) {
            new VehicleTeleporter(origin, teleportingVehicle).teleport(origin);
            return null;
        }
        return stargateEntityPortalEvent.getExit();
    }

    /**
     * Teleport a vehicle which is not a minecart or a boat
     *
     * @param exit       <p>The location the vehicle will exit</p>
     * @param passengers <p>The passengers of the vehicle</p>
     */
    private void teleportLivingVehicle(Location exit, List<Entity> passengers) {
        teleportingVehicle.eject();
        teleportingVehicle.teleport(exit);
        handleVehiclePassengers(passengers, teleportingVehicle, 2);
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
     */
    private void putPassengersInNewVehicle(List<Entity> passengers, Location exit,
                                           Vector newVelocity) {
        World vehicleWorld = exit.getWorld();
        if (vehicleWorld == null) {
            Stargate.logger.warning(Stargate.getString("prefix") +
                    "Unable to get the world to teleport the vehicle to");
            return;
        }
        //Spawn a new vehicle
        Vehicle newVehicle = vehicleWorld.spawn(exit, teleportingVehicle.getClass());
        //Remove the old vehicle
        teleportingVehicle.eject();
        teleportingVehicle.remove();
        //Set rotation, add passengers and restore velocity
        newVehicle.setRotation(exit.getYaw(), exit.getPitch());
        handleVehiclePassengers(passengers, newVehicle, 1);
        scheduler.scheduleSyncDelayedTask(Stargate.stargate, () -> newVehicle.setVelocity(newVelocity), 1);
    }

    /**
     * Ejects, teleports and adds all passengers to the target vehicle
     *
     * @param passengers <p>The passengers to handle</p>
     * @param vehicle    <p>The vehicle the passengers should be put into</p>
     * @param delay      <p>The amount of milliseconds to wait before adding the vehicle passengers</p>
     */
    private void handleVehiclePassengers(List<Entity> passengers, Vehicle vehicle, long delay) {
        for (Entity passenger : passengers) {
            passenger.eject();
            scheduler.scheduleSyncDelayedTask(Stargate.stargate, () -> teleportAndAddPassenger(vehicle, passenger), delay);
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
