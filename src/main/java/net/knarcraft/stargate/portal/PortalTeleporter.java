package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.ChunkUnloadRequest;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.event.StargateEntityPortalEvent;
import net.knarcraft.stargate.event.StargatePlayerPortalEvent;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EntityHelper;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * The portal teleporter takes care of the actual portal teleportation
 */
public class PortalTeleporter {

    private final Portal portal;

    /**
     * Instantiates a new portal teleporter
     *
     * @param portal <p>The portal which is the target of the teleportation</p>
     */
    public PortalTeleporter(Portal portal) {
        this.portal = portal;
    }

    /**
     * Teleports a vehicle to this teleporter's portal
     *
     * <p>It is assumed that if a vehicle contains any players, their permissions have already been validated before
     * calling this method.</p>
     *
     * @param vehicle <p>The vehicle to teleport</p>
     * @param origin  <p>The portal the vehicle teleports from</p>
     */
    public void teleport(final Vehicle vehicle, Portal origin) {
        Location traveller = vehicle.getLocation();
        Location exit = getExit(vehicle, traveller);

        double velocity = vehicle.getVelocity().length();

        //Stop and teleport
        vehicle.setVelocity(new Vector());

        //Get new velocity
        Vector newVelocityDirection = DirectionHelper.getDirectionVectorFromYaw(portal.getYaw());
        Vector newVelocity = newVelocityDirection.multiply(velocity);

        //Make sure the vehicle points out from the portal
        adjustRotation(exit);

        //Call the StargateEntityPortalEvent to allow plugins to change destination
        if (!origin.equals(portal)) {
            exit = triggerEntityPortalEvent(origin, exit, vehicle);
            if (exit == null) {
                return;
            }
        }

        //Teleport the vehicle
        teleportVehicle(vehicle, exit, newVelocity);
    }

    /**
     * Teleports a vehicle with any passengers to the given location
     *
     * @param vehicle     <p>The vehicle to teleport</p>
     * @param exit        <p>The location the vehicle should be teleported to</p>
     * @param newVelocity <p>The velocity to give the vehicle right after teleportation</p>
     */
    private void teleportVehicle(Vehicle vehicle, Location exit, Vector newVelocity) {
        //Load chunks to make sure not to teleport to the void
        loadChunks();

        List<Entity> passengers = vehicle.getPassengers();
        if (!passengers.isEmpty()) {
            if (!(vehicle instanceof LivingEntity)) {
                //Teleport a normal vehicle with passengers (minecart or boat)
                putPassengersInNewVehicle(vehicle, passengers, exit, newVelocity);
            } else {
                //Teleport a living vehicle with passengers (pig, horse, donkey, strider)
                teleportLivingVehicle(vehicle, exit, passengers);
            }
        } else {
            //Teleport an empty vehicle
            vehicle.teleport(exit);
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate,
                    () -> vehicle.setVelocity(newVelocity), 1);
        }
    }

    /**
     * Triggers the entity portal event to allow plugins to change the exit location
     *
     * @param origin  <p>The origin portal teleported from</p>
     * @param exit    <p>The exit location to teleport the vehicle to</p>
     * @param vehicle <p>The teleporting vehicle</p>
     * @return <p>The location the vehicle should be teleported to, or null if the event was cancelled</p>
     */
    private Location triggerEntityPortalEvent(Portal origin, Location exit, Vehicle vehicle) {
        StargateEntityPortalEvent stargateEntityPortalEvent = new StargateEntityPortalEvent(vehicle, origin,
                portal, exit);
        Stargate.server.getPluginManager().callEvent(stargateEntityPortalEvent);
        //Teleport is cancelled. Teleport the entity back to where it came from just for sanity's sake
        if (stargateEntityPortalEvent.isCancelled()) {
            new PortalTeleporter(origin).teleport(vehicle, origin);
            return null;
        }
        return stargateEntityPortalEvent.getExit();
    }

    private Location triggerPlayerPortalEvent(Portal origin, Location exit, Player player, PlayerMoveEvent event) {
        StargatePlayerPortalEvent stargatePlayerPortalEvent = new StargatePlayerPortalEvent(player, origin,
                portal, exit);
        Stargate.server.getPluginManager().callEvent(stargatePlayerPortalEvent);
        //Teleport is cancelled. Teleport the player back to where it came from
        if (stargatePlayerPortalEvent.isCancelled()) {
            new PortalTeleporter(origin).teleport(player, origin, event);
            return null;
        }
        return stargatePlayerPortalEvent.getExit();
    }

    /**
     * Teleports a player to this teleporter's portal
     *
     * @param player <p>The player to teleport</p>
     * @param origin <p>The portal the player teleports from</p>
     * @param event  <p>The player move event triggering the event</p>
     */
    public void teleport(Player player, Portal origin, PlayerMoveEvent event) {
        Location traveller = player.getLocation();
        Location exit = getExit(player, traveller);

        //Rotate the player to face out from the portal
        adjustRotation(exit);

        //Call the StargatePlayerPortalEvent to allow plugins to change destination
        if (!origin.equals(portal)) {
            exit = triggerPlayerPortalEvent(origin, exit, player, event);
            if (exit == null) {
                return;
            }
        }

        //Load chunks to make sure not to teleport to the void
        loadChunks();

        //If no event is passed in, assume it's a teleport, and act as such
        if (event == null) {
            player.teleport(exit);
        } else {
            //The new method to teleport in a move event is set the "to" field.
            event.setTo(exit);
        }
    }

    /**
     * Adjusts the rotation of the player to face out from the portal
     *
     * @param exit <p>The location the player will exit from</p>
     */
    private void adjustRotation(Location exit) {
        int adjust = 0;
        if (portal.getOptions().isBackwards()) {
            adjust = 180;
        }
        float newYaw = (portal.getYaw() + adjust) % 360;
        Stargate.debug("Portal::adjustRotation", "Setting exit yaw to " + newYaw);
        exit.setYaw(newYaw);
    }

    /**
     * Teleport a vehicle which is not a minecart or a boat
     *
     * @param vehicle    <p>The vehicle to teleport</p>
     * @param exit       <p>The location the vehicle will exit</p>
     * @param passengers <p>The passengers of the vehicle</p>
     */
    private void teleportLivingVehicle(Vehicle vehicle, Location exit, List<Entity> passengers) {
        vehicle.eject();
        vehicle.teleport(exit);
        handleVehiclePassengers(passengers, vehicle, 2);
    }

    /**
     * Creates a new vehicle equal to the player's previous vehicle and puts any passengers inside
     *
     * <p>While it is possible to teleport boats and minecarts using the same methods as "teleportLivingVehicle", this
     * method works better with CraftBook with minecart options enabled. Using normal teleportation, CraftBook destroys
     * the minecart once the player is ejected, causing the minecart to disappear and the player to teleport without it.</p>
     *
     * @param vehicle     <p>The player's old vehicle</p>
     * @param passengers  <p>A list of all passengers in the vehicle</p>
     * @param exit        <p>The exit location to spawn the new vehicle on</p>
     * @param newVelocity <p>The new velocity of the new vehicle</p>
     */
    private void putPassengersInNewVehicle(Vehicle vehicle, List<Entity> passengers, Location exit,
                                           Vector newVelocity) {
        World vehicleWorld = exit.getWorld();
        if (vehicleWorld == null) {
            Stargate.logger.warning(Stargate.getString("prefix") +
                    "Unable to get the world to teleport the vehicle to");
            return;
        }
        //Spawn a new vehicle
        Vehicle newVehicle = vehicleWorld.spawn(exit, vehicle.getClass());
        //Remove the old vehicle
        vehicle.eject();
        vehicle.remove();
        //Set rotation, add passengers and restore velocity
        newVehicle.setRotation(exit.getYaw(), exit.getPitch());
        handleVehiclePassengers(passengers, newVehicle, 1);
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate,
                () -> newVehicle.setVelocity(newVelocity), 1);
    }

    /**
     * Ejects, teleports and adds all passengers to the target vehicle
     *
     * @param passengers    <p>The passengers to handle</p>
     * @param targetVehicle <p>The vehicle the passengers should be put into</p>
     * @param delay         <p>The amount of milliseconds to wait before adding the vehicle passengers</p>
     */
    private void handleVehiclePassengers(List<Entity> passengers, Vehicle targetVehicle, long delay) {
        for (Entity passenger : passengers) {
            passenger.eject();
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate,
                    () -> teleportAndAddPassenger(targetVehicle, passenger), delay);
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

    /**
     * Gets the exit location for a given entity and current location
     *
     * @param entity    <p>The entity to teleport (used to determine distance from portal to avoid suffocation)</p>
     * @param traveller <p>The location of the entity travelling</p>
     * @return <p>The location the entity should be teleported to.</p>
     */
    public Location getExit(Entity entity, Location traveller) {
        Location exitLocation = null;
        // Check if the gate has an exit block
        RelativeBlockVector relativeExit = portal.getGate().getLayout().getExit();
        if (relativeExit != null) {
            BlockLocation exit = portal.getBlockAt(relativeExit);
            float portalYaw = portal.getYaw();
            if (portal.getOptions().isBackwards()) {
                portalYaw += 180;
            }
            exitLocation = exit.getRelativeLocation(0D, 0D, 1, portalYaw);

            if (entity != null) {
                double entitySize = EntityHelper.getEntityMaxSize(entity);
                if (entitySize > 1) {
                    exitLocation = preventExitSuffocation(relativeExit, exitLocation, entity);
                }
            }
        } else {
            Stargate.logger.log(Level.WARNING, Stargate.getString("prefix") +
                    "Missing destination point in .gate file " + portal.getGate().getFilename());
        }

        return adjustExitLocation(traveller, exitLocation);
    }

    /**
     * Adjusts the positioning of the portal exit to prevent the given entity from suffocating
     *
     * @param relativeExit <p>The relative exit defined as the portal's exit</p>
     * @param exitLocation <p>The currently calculated portal exit</p>
     * @param entity       <p>The travelling entity</p>
     * @return <p>A location which won't suffocate the entity inside the portal</p>
     */
    private Location preventExitSuffocation(RelativeBlockVector relativeExit, Location exitLocation, Entity entity) {
        //Go left to find start of opening
        RelativeBlockVector openingLeft = getPortalExitEdge(relativeExit, -1);

        //Go right to find the end of the opening
        RelativeBlockVector openingRight = getPortalExitEdge(relativeExit, 1);

        //Get the width to check if the entity fits
        int openingWidth = openingRight.getRight() - openingLeft.getRight() + 1;
        int existingOffset = relativeExit.getRight() - openingLeft.getRight();
        double newOffset = (openingWidth - existingOffset) / 2D;

        //Remove the half offset for better centering
        if (openingWidth > 1) {
            newOffset -= 0.5;
        }
        exitLocation = DirectionHelper.moveLocation(exitLocation, newOffset, 0, 0, portal.getYaw());

        //Move large entities further from the portal, especially if this teleporter's portal will teleport them at once
        double entitySize = EntityHelper.getEntityMaxSize(entity);
        int entityBoxSize = EntityHelper.getEntityMaxSizeInt(entity);
        if (entitySize > 1) {
            if (portal.getOptions().isAlwaysOn()) {
                exitLocation = DirectionHelper.moveLocation(exitLocation, 0, 0, (entityBoxSize / 2D),
                        portal.getYaw());
            } else {
                exitLocation = DirectionHelper.moveLocation(exitLocation, 0, 0,
                        (entitySize / 2D) - 1, portal.getYaw());
            }
        }

        //If a horse has a player riding it, the player will spawn inside the roof of a standard portal unless it's
        //moved one block out.
        if (entity instanceof AbstractHorse) {
            exitLocation = DirectionHelper.moveLocation(exitLocation, 0, 0, 1, portal.getYaw());
        }

        return exitLocation;
    }

    /**
     * Gets one of the edges of a portal's opening/exit
     *
     * @param relativeExit <p>The known exit to start from</p>
     * @param direction    <p>The direction to move (+1 for right, -1 for left)</p>
     * @return <p>The right or left edge of the opening</p>
     */
    private RelativeBlockVector getPortalExitEdge(RelativeBlockVector relativeExit, int direction) {
        RelativeBlockVector openingEdge = relativeExit;
        do {
            RelativeBlockVector possibleOpening = new RelativeBlockVector(openingEdge.getRight() + direction,
                    openingEdge.getDepth(), openingEdge.getDistance());
            if (portal.getGate().getLayout().getExits().contains(possibleOpening)) {
                openingEdge = possibleOpening;
            } else {
                break;
            }
        } while (true);
        return openingEdge;
    }

    /**
     * Adjusts an exit location with rotation and slab height incrementation
     *
     * @param traveller    <p>The location of the travelling entity</p>
     * @param exitLocation <p>The exit location generated</p>
     * @return <p>The location the travelling entity should be teleported to</p>
     */
    private Location adjustExitLocation(Location traveller, Location exitLocation) {
        if (exitLocation != null) {
            BlockData blockData = portal.getWorld().getBlockAt(exitLocation).getBlockData();
            if ((blockData instanceof Bisected && ((Bisected) blockData).getHalf() == Bisected.Half.BOTTOM) ||
                    (blockData instanceof Slab) && ((Slab) blockData).getType() == Slab.Type.BOTTOM) {
                //Prevent traveller from spawning inside a slab
                Stargate.debug("adjustExitLocation", "Added a block to get above a slab");
                exitLocation.add(0, 1, 0);
            } else if (blockData.getMaterial() == Material.WATER) {
                //If there's water outside, go one up to allow for boat teleportation
                Stargate.debug("adjustExitLocation", "Added a block to get above a block of water");
                exitLocation.add(0, 1, 0);
            }

            exitLocation.setPitch(traveller.getPitch());
            return exitLocation;
        } else {
            Stargate.logger.log(Level.WARNING, Stargate.getString("prefix") +
                    "Unable to generate exit location");
        }
        return traveller;
    }

    /**
     * Loads the chunks outside the portal's entrance
     */
    private void loadChunks() {
        for (Chunk chunk : getChunksToLoad()) {
            chunk.addPluginChunkTicket(Stargate.stargate);
            //Allow the chunk to unload after 3 seconds
            Stargate.addChunkUnloadRequest(new ChunkUnloadRequest(chunk, 3000L));
        }
    }

    /**
     * Gets all relevant chunks near this teleporter's portal's entrance which need to be loaded before teleportation
     *
     * @return <p>A list of chunks to load</p>
     */
    private List<Chunk> getChunksToLoad() {
        List<Chunk> chunksToLoad = new ArrayList<>();
        for (RelativeBlockVector vector : portal.getGate().getLayout().getEntrances()) {
            BlockLocation entranceLocation = portal.getBlockAt(vector);
            Chunk chunk = entranceLocation.getChunk();
            //Make sure not to load chunks twice
            if (!chunksToLoad.contains(chunk)) {
                chunksToLoad.add(chunk);
            }

            //Get the chunk in front of the gate entrance
            int blockOffset = portal.getOptions().isBackwards() ? -5 : 5;
            Location fiveBlocksForward = DirectionHelper.moveLocation(entranceLocation, 0, 0, blockOffset,
                    portal.getYaw());
            //Load the chunk five blocks forward to make sure the teleported entity will never spawn in unloaded chunks
            Chunk forwardChunk = fiveBlocksForward.getChunk();
            if (!chunksToLoad.contains(forwardChunk)) {
                chunksToLoad.add(forwardChunk);
            }
        }
        return chunksToLoad;
    }

}
