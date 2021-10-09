package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.event.StargateActivateEvent;
import net.knarcraft.stargate.event.StargateCloseEvent;
import net.knarcraft.stargate.event.StargateDeactivateEvent;
import net.knarcraft.stargate.event.StargateOpenEvent;
import net.knarcraft.stargate.event.StargatePortalEvent;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EntityHelper;
import net.knarcraft.stargate.utility.SignHelper;
import org.bukkit.Axis;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This class represents a portal in space which points to one or several portals
 */
public class Portal {

    // Gate location block info
    private final PortalLocation location;

    // Block references
    private final Gate gate;
    private BlockLocation button;
    private BlockLocation[] frame;
    private BlockLocation[] entrances;

    // Gate information
    private String name;
    private String destination;
    private String lastDestination = "";
    private String network;
    private final String ownerName;
    private UUID ownerUUID;
    private boolean verified;
    private final PortalOptions options;

    // In-use information
    private Player player;
    private Player activePlayer;
    private List<String> destinations = new ArrayList<>();
    private boolean isOpen = false;
    private long openTime;

    /**
     * Instantiates a new portal
     *
     * @param portalLocation <p>Object containing locations of all relevant blocks</p>
     * @param button         <p>The location of the portal's open button</p>
     * @param destination    <p>The destination defined on the sign's destination line</p>
     * @param name           <p>The name of the portal defined on the sign's first line</p>
     * @param network        <p>The network the portal belongs to, defined on the sign's network line</p>
     * @param gate           <p>The gate template this portal uses</p>
     * @param ownerUUID      <p>The UUID of the gate's owner</p>
     * @param ownerName      <p>The name of the gate's owner</p>
     * @param options        <p>A map containing all possible portal options</p>
     */
    Portal(PortalLocation portalLocation, BlockLocation button, String destination, String name, String network,
           Gate gate, UUID ownerUUID, String ownerName, Map<PortalOption, Boolean> options) {
        this.location = portalLocation;
        this.destination = destination;
        this.button = button;
        this.verified = false;
        this.network = network;
        this.name = name;
        this.gate = gate;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.options = new PortalOptions(options, destination.length() > 0);
    }

    /**
     * Gets the portal options for this portal
     *
     * @return <p>This portal's portal options</p>
     */
    public PortalOptions getOptions() {
        return this.options;
    }

    /**
     * Gets whether this portal is currently open
     *
     * @return <p>Whether this portal is open</p>
     */
    public boolean isOpen() {
        return isOpen || options.isAlwaysOn();
    }

    /**
     * Gets the player currently using this portal
     *
     * @return <p>The player currently using this portal</p>
     */
    public Player getActivePlayer() {
        return activePlayer;
    }

    /**
     * Gets the network this gate belongs to
     *
     * @return <p>The network this gate belongs to</p>
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Sets the network this gate belongs to
     *
     * @param network <p>The new network for this gate</p>
     */
    @SuppressWarnings("unused")
    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * Gets the time this portal opened
     *
     * @return <p>The time this portal opened</p>
     */
    public long getOpenTime() {
        return openTime;
    }

    /**
     * Gets the name of this portal
     *
     * @return <p>The name of this portal</p>
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this portal
     *
     * @param name <p>The new name of this portal</p>
     */
    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = filterName(name);
        drawSign();
    }

    /**
     * Gets the destinations of this portal
     *
     * @return <p>The destinations of this portal</p>
     */
    public List<String> getDestinations() {
        return new ArrayList<>(this.destinations);
    }

    /**
     * Gets the portal destination given a player
     *
     * @param player <p>Used for random gates to determine which destinations are available</p>
     * @return <p>The destination portal the player should teleport to</p>
     */
    public Portal getDestination(Player player) {
        if (options.isRandom()) {
            destinations = PortalHandler.getDestinations(this, player, getNetwork());
            if (destinations.size() == 0) {
                return null;
            }
            String destination = destinations.get((new Random()).nextInt(destinations.size()));
            destinations.clear();
            return PortalHandler.getByName(destination, getNetwork());
        }
        return PortalHandler.getByName(destination, getNetwork());
    }

    /**
     * Gets the portal destination
     *
     * <p>If this portal is random, a player should be given to get correct destinations.</p>
     *
     * @return <p>The portal destination</p>
     */
    public Portal getDestination() {
        return getDestination(null);
    }

    /**
     * Sets the destination of this portal
     *
     * @param destination <p>The new destination of this portal</p>
     */
    public void setDestination(Portal destination) {
        setDestination(destination.getName());
    }

    /**
     * Sets the destination of this portal
     *
     * @param destination <p>The new destination of this portal</p>
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets the name of the destination of this portal
     *
     * @return <p>The name of this portal's destination</p>
     */
    public String getDestinationName() {
        return destination;
    }

    /**
     * Gets the gate used by this portal
     *
     * @return <p>The gate used by this portal</p>
     */
    public Gate getGate() {
        return gate;
    }

    /**
     * Gets the name of this portal's owner
     *
     * @return <p>The name of this portal's owner</p>
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Gets the UUID of this portal's owner
     *
     * @return <p>The UUID of this portal's owner</p>
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * Sets the UUId of this portal's owner
     *
     * @param owner <p>The new UUID of this portal's owner</p>
     */
    @SuppressWarnings("unused")
    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
    }

    /**
     * Checks whether a given player is the owner of this portal
     *
     * @param player <p>The player to check</p>
     * @return <p>True if the player is the owner of this portal</p>
     */
    public boolean isOwner(Player player) {
        if (this.ownerUUID != null) {
            return player.getUniqueId().compareTo(this.ownerUUID) == 0;
        } else {
            return player.getName().equalsIgnoreCase(this.ownerName);
        }
    }

    /**
     * Gets the locations of this portal's entrances
     *
     * @return <p>The locations of this portal's entrances</p>
     */
    public BlockLocation[] getEntrances() {
        if (entrances == null) {
            entrances = relativeBlockVectorsToBlockLocations(gate.getLayout().getEntrances());
        }
        return entrances;
    }

    /**
     * Gets the locations of this portal's frame
     *
     * @return <p>The locations of this portal's frame</p>
     */
    public BlockLocation[] getFrame() {
        if (frame == null) {
            frame = relativeBlockVectorsToBlockLocations(gate.getLayout().getBorder());
        }
        return frame;
    }

    /**
     * Gets the world this portal belongs to
     *
     * @return <p>The world this portal belongs to</p>
     */
    public World getWorld() {
        return location.getWorld();
    }

    /**
     * Gets the location of this portal's button
     *
     * @return <p>The location of this portal's button</p>
     */
    public BlockLocation getButton() {
        return button;
    }

    /**
     * Sets the location of this portal's button
     *
     * @param button <p>The location of this portal's button</p>
     */
    public void setButton(BlockLocation button) {
        this.button = button;
    }

    /**
     * Open this portal
     *
     * @param force <p>Whether to force this portal open, even if it's already open for some player</p>
     */
    public void open(boolean force) {
        open(null, force);
    }

    /**
     * Open this portal
     *
     * @param force <p>Whether to force this portal open, even if it's already open for some player</p>
     */
    public void open(Player openFor, boolean force) {
        //Call the StargateOpenEvent
        StargateOpenEvent event = new StargateOpenEvent(openFor, this, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled() || (isOpen() && !event.getForce())) {
            return;
        }

        //Change the opening blocks to the correct type
        Material openType = gate.getPortalOpenBlock();
        Axis axis = (openType.createBlockData() instanceof Orientable) ? location.getRotationAxis() : null;
        for (BlockLocation inside : getEntrances()) {
            Stargate.blockChangeRequestQueue.add(new BlockChangeRequest(inside, openType, axis));
        }

        updatePortalOpenState(openFor);
    }

    /**
     * Updates this portal to be recognized as open and opens its destination portal
     *
     * @param openFor <p>The player to open this portal for</p>
     */
    private void updatePortalOpenState(Player openFor) {
        //Update the open state of this portal
        isOpen = true;
        openTime = System.currentTimeMillis() / 1000;
        Stargate.openPortalsQueue.add(this);
        Stargate.activePortalsQueue.remove(this);

        //Open remote portal
        if (!options.isAlwaysOn()) {
            player = openFor;

            Portal destination = getDestination();
            // Only open destination if it's not-fixed or points at this portal
            if (!options.isRandom() && destination != null && (!destination.options.isFixed() ||
                    destination.getDestinationName().equalsIgnoreCase(getName())) && !destination.isOpen()) {
                destination.open(openFor, false);
                destination.setDestination(this);
                if (destination.isVerified()) {
                    destination.drawSign();
                }
            }
        }
    }

    /**
     * Closes this portal
     *
     * @param force <p>Whether to force this portal closed, even if it's set as always on</p>
     */
    public void close(boolean force) {
        if (!isOpen) {
            return;
        }
        //Call the StargateCloseEvent
        StargateCloseEvent event = new StargateCloseEvent(this, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        force = event.getForce();

        //Only close always-open if forced to
        if (options.isAlwaysOn() && !force) {
            return;
        }

        //Close this gate, then the dest gate.
        Material closedType = gate.getPortalClosedBlock();
        for (BlockLocation inside : getEntrances()) {
            Stargate.blockChangeRequestQueue.add(new BlockChangeRequest(inside, closedType, null));
        }

        updatePortalClosedState();
        deactivate();
    }

    /**
     * Updates this portal to be recognized as closed and closes its destination portal
     */
    private void updatePortalClosedState() {
        //Update the closed state of this portal
        player = null;
        isOpen = false;
        Stargate.openPortalsQueue.remove(this);
        Stargate.activePortalsQueue.remove(this);

        //Close remote portal
        if (!options.isAlwaysOn()) {
            Portal end = getDestination();

            if (end != null && end.isOpen()) {
                //Clear its destination first
                end.deactivate();
                end.close(false);
            }
        }
    }

    /**
     * Gets whether this portal is open for the given player
     *
     * @param player <p>The player to check portal state for</p>
     * @return <p>True if this portal is open to the given player</p>
     */
    public boolean isOpenFor(Player player) {
        if (!isOpen) {
            return false;
        }
        if (options.isAlwaysOn() || this.player == null) {
            return true;
        }
        return player != null && player.getName().equalsIgnoreCase(this.player.getName());
    }

    /**
     * Teleports a player to this portal
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

        //Call the StargatePortalEvent to allow plugins to change destination
        if (!origin.equals(this)) {
            StargatePortalEvent stargatePortalEvent = new StargatePortalEvent(player, origin, this, exit);
            Stargate.server.getPluginManager().callEvent(stargatePortalEvent);
            //Teleport is cancelled. Teleport the player back to where it came from
            if (stargatePortalEvent.isCancelled()) {
                origin.teleport(player, origin, event);
                return;
            }
            //Update exit if needed
            exit = stargatePortalEvent.getExit();
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
        if (options.isBackwards()) {
            adjust = 180;
        }
        float newYaw = (this.getYaw() + adjust) % 360;
        Stargate.debug("Portal::adjustRotation", "Setting exit yaw to " + newYaw);
        exit.setYaw(newYaw);
    }

    /**
     * Teleports a vehicle to this portal
     *
     * @param vehicle <p>The vehicle to teleport</p>
     */
    public void teleport(final Vehicle vehicle) {
        Location traveller = vehicle.getLocation();
        Location exit = getExit(vehicle, traveller);

        double velocity = vehicle.getVelocity().length();

        //Stop and teleport
        vehicle.setVelocity(new Vector());

        //Get new velocity
        Vector newVelocityDirection = DirectionHelper.getDirectionVectorFromYaw(this.getYaw());
        Vector newVelocity = newVelocityDirection.multiply(velocity);
        adjustRotation(exit);

        List<Entity> passengers = vehicle.getPassengers();

        //Load chunks to make sure not to teleport to the void
        loadChunks();

        if (!passengers.isEmpty()) {
            if (vehicle instanceof RideableMinecart || vehicle instanceof Boat) {
                World vehicleWorld = exit.getWorld();
                if (vehicleWorld == null) {
                    Stargate.logger.warning(Stargate.getString("prefix") +
                            "Unable to get the world to teleport the vehicle to");
                    return;
                }
                putPassengersInNewVehicle(vehicle, passengers, vehicleWorld, exit, newVelocity);
            } else {
                teleportLivingVehicle(vehicle, exit, passengers);
            }
        } else {
            vehicle.teleport(exit);
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate,
                    () -> vehicle.setVelocity(newVelocity), 1);
        }
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
        handleVehiclePassengers(passengers, vehicle);
    }

    /**
     * Creates a new vehicle equal to the player's previous vehicle and
     *
     * @param vehicle      <p>The player's old vehicle</p>
     * @param passengers   <p>A list of all passengers in the vehicle</p>
     * @param vehicleWorld <p>The world to spawn the new vehicle in</p>
     * @param exit         <p>The exit location to spawn the new vehicle on</p>
     * @param newVelocity  <p>The new velocity of the new vehicle</p>
     */
    private void putPassengersInNewVehicle(Vehicle vehicle, List<Entity> passengers, World vehicleWorld, Location exit,
                                           Vector newVelocity) {
        Vehicle newVehicle = vehicleWorld.spawn(exit, vehicle.getClass());
        vehicle.eject();
        vehicle.remove();
        newVehicle.setRotation(exit.getYaw(), exit.getPitch());
        handleVehiclePassengers(passengers, newVehicle);
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate,
                () -> newVehicle.setVelocity(newVelocity), 1);
    }

    /**
     * Ejects, teleports and adds all passengers to the target vehicle
     *
     * @param passengers    <p>The passengers to handle</p>
     * @param targetVehicle <p>The vehicle the passengers should be put into</p>
     */
    private void handleVehiclePassengers(List<Entity> passengers, Vehicle targetVehicle) {
        for (Entity passenger : passengers) {
            passenger.eject();
            //TODO: Fix random java.lang.IllegalStateException: Removing entity while ticking!
            if (!passenger.teleport(targetVehicle.getLocation())) {
                Stargate.debug("handleVehiclePassengers", "Failed to teleport passenger");
            }
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate,
                    () -> targetVehicle.addPassenger(passenger), 6);
        }
    }

    /**
     * Gets the exit location for a given entity and current location
     *
     * @param entity    <p>The entity to teleport (used to determine distance from portal to avoid suffocation)</p>
     * @param traveller <p>The location of the entity travelling</p>
     * @return <p>The location the entity should be teleported to.</p>
     */
    private Location getExit(Entity entity, Location traveller) {
        Location exitLocation = null;
        // Check if the gate has an exit block
        RelativeBlockVector relativeExit = gate.getLayout().getExit();
        if (relativeExit != null) {
            BlockLocation exit = getBlockAt(relativeExit);
            float portalYaw = this.getYaw();
            if (options.isBackwards()) {
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
                    "Missing destination point in .gate file " + gate.getFilename());
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
        exitLocation = DirectionHelper.moveLocation(exitLocation, newOffset, 0, 0, getYaw());

        //Move large entities further from the portal, especially if this portal will teleport them at once
        double entitySize = EntityHelper.getEntityMaxSize(entity);
        int entityBoxSize = EntityHelper.getEntityMaxSizeInt(entity);
        if (entitySize > 1) {
            if (options.isAlwaysOn()) {
                exitLocation = DirectionHelper.moveLocation(exitLocation, 0, 0, (entityBoxSize / 2D),
                        getYaw());
            } else {
                exitLocation = DirectionHelper.moveLocation(exitLocation, 0, 0,
                        (entitySize / 2D) - 1, getYaw());
            }
        }

        //If a horse has a player riding it, the player will spawn inside the roof of a standard portal unless it's
        //moved one block out.
        if (entity instanceof AbstractHorse) {
            exitLocation = DirectionHelper.moveLocation(exitLocation, 0, 0, 1, getYaw());
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
            if (gate.getLayout().getExits().contains(possibleOpening)) {
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
            //Prevent traveller from spawning inside a slab
            BlockData blockData = getWorld().getBlockAt(exitLocation).getBlockData();
            if ((blockData instanceof Bisected && ((Bisected) blockData).getHalf() == Bisected.Half.BOTTOM) ||
                    (blockData instanceof Slab) && ((Slab) blockData).getType() == Slab.Type.BOTTOM) {
                Stargate.debug("adjustExitLocation", "Added half a block to get above a slab");
                exitLocation.add(0, 0.5, 0);
            } else if (blockData.getMaterial() == Material.WATER) {
                //If there's water outside, go one up to allow for boat teleportation
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
     * Loads the chunks at the portal's corners
     */
    public void loadChunks() {
        for (RelativeBlockVector vector : gate.getLayout().getCorners()) {
            Chunk chunk = getBlockAt(vector).getChunk();

            //Get the chunk in front of the gate corner
            Location cornerLocation = getBlockAt(vector).getLocation();
            int blockOffset = options.isBackwards() ? -5 : 5;
            Location fiveBlocksForward = DirectionHelper.moveLocation(cornerLocation, 0, 0, blockOffset,
                    getYaw());
            Chunk forwardChunk = fiveBlocksForward.getChunk();

            //Load the chunks
            loadOneChunk(chunk);
            loadOneChunk(forwardChunk);
        }
    }

    /**
     * Loads one chunk
     *
     * @param chunk <p>The chunk to load</p>
     */
    private void loadOneChunk(Chunk chunk) {
        if (!getWorld().isChunkLoaded(chunk)) {
            if (!chunk.load()) {
                Stargate.debug("loadChunks", "Failed to load chunk " + chunk);
            }
        }
    }

    /**
     * Gets the identity (sign) location of the portal
     *
     * @return <p>The identity location of the portal</p>
     */
    public BlockLocation getSignLocation() {
        return this.location.getSignLocation();
    }

    /**
     * Gets the rotation of this portal
     *
     * @return <p>The rotation of this portal</p>
     */
    public float getYaw() {
        return this.location.getYaw();
    }

    /**
     * Gets the location of the top-left block of the portal
     *
     * @return <p>The location of the top-left portal block</p>
     */
    public BlockLocation getTopLeft() {
        return this.location.getTopLeft();
    }

    /**
     * Verifies that all control blocks in this portal follows its gate template
     *
     * @return <p>True if all control blocks were verified</p>
     */
    public boolean isVerified() {
        verified = true;
        if (!Stargate.verifyPortals) {
            return true;
        }
        for (RelativeBlockVector control : gate.getLayout().getControls()) {
            verified = verified && getBlockAt(control).getBlock().getType().equals(gate.getControlBlock());
        }
        return verified;
    }

    /**
     * Gets the result of the last portal verification
     *
     * @return <p>True if this portal was verified</p>
     */
    public boolean wasVerified() {
        if (!Stargate.verifyPortals) {
            return true;
        }
        return verified;
    }

    /**
     * Checks if all blocks in a gate matches the gate template
     *
     * @return <p>True if all blocks match the gate template</p>
     */
    public boolean checkIntegrity() {
        if (!Stargate.verifyPortals) {
            return true;
        }
        return gate.matches(getTopLeft(), getYaw());
    }

    /**
     * Activates this portal for the given player
     *
     * @param player <p>The player to activate the portal for</p>
     * @return <p>True if the portal was activated</p>
     */
    private boolean activate(Player player) {
        destinations.clear();
        destination = "";
        Stargate.activePortalsQueue.add(this);
        activePlayer = player;
        String network = getNetwork();
        destinations = PortalHandler.getDestinations(this, player, network);
        if (Stargate.sortNetworkDestinations) {
            Collections.sort(destinations);
        }
        if (Stargate.rememberDestination && !lastDestination.isEmpty() && destinations.contains(lastDestination)) {
            destination = lastDestination;
        }

        StargateActivateEvent event = new StargateActivateEvent(this, player, destinations, destination);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Stargate.activePortalsQueue.remove(this);
            return false;
        }
        destination = event.getDestination();
        destinations = event.getDestinations();
        drawSign();
        return true;
    }

    /**
     * Deactivates this portal
     */
    public void deactivate() {
        StargateDeactivateEvent event = new StargateDeactivateEvent(this);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        Stargate.activePortalsQueue.remove(this);
        if (options.isFixed()) {
            return;
        }
        destinations.clear();
        destination = "";
        activePlayer = null;
        drawSign();
    }

    /**
     * Gets whether this portal is active
     *
     * @return <p>Whether this portal is active</p>
     */
    public boolean isActive() {
        return options.isFixed() || (destinations.size() > 0);
    }

    /**
     * Cycles destination for a network gate forwards
     *
     * @param player <p>The player to cycle the gate for</p>
     */
    public void cycleDestination(Player player) {
        cycleDestination(player, 1);
    }

    /**
     * Cycles destination for a network gate
     *
     * @param player    <p>The player cycling destinations</p>
     * @param direction <p>The direction of the cycle (+1 for next, -1 for previous)</p>
     */
    public void cycleDestination(Player player, int direction) {
        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("The destination direction must be 1 or -1.");
        }

        boolean activate = false;
        if (!isActive() || getActivePlayer() != player) {
            //If the stargate activate event is cancelled, return
            if (!activate(player)) {
                return;
            }
            Stargate.debug("cycleDestination", "Network Size: " + PortalHandler.getNetwork(network).size());
            Stargate.debug("cycleDestination", "Player has access to: " + destinations.size());
            activate = true;
        }

        if (destinations.size() == 0) {
            Stargate.sendErrorMessage(player, Stargate.getString("destEmpty"));
            return;
        }

        if (!Stargate.rememberDestination || !activate || lastDestination.isEmpty()) {
            cycleDestination(direction);
        }
        openTime = System.currentTimeMillis() / 1000;
        drawSign();
    }

    /**
     * Performs the actual destination cycling with no input checks
     *
     * @param direction <p>The direction of the cycle (+1 for next, -1 for previous)</p>
     */
    private void cycleDestination(int direction) {
        int index = destinations.indexOf(destination);
        index += direction;

        //Wrap around
        if (index >= destinations.size()) {
            index = 0;
        } else if (index < 0) {
            index = destinations.size() - 1;
        }
        //Store selected destination
        destination = destinations.get(index);
        lastDestination = destination;
    }

    /**
     * Draws the sign on this portal
     */
    public final void drawSign() {
        BlockState state = getSignLocation().getBlock().getState();
        if (!(state instanceof Sign sign)) {
            Stargate.logger.warning(Stargate.getString("prefix") + "Sign block is not a Sign object");
            Stargate.debug("Portal::drawSign", "Block: " + getSignLocation().getBlock().getType() + " @ "
                    + getSignLocation().getBlock().getLocation());
            return;
        }

        SignHelper.drawSign(sign, this);
    }

    /**
     * Gets the block at a relative block vector location
     *
     * @param vector <p>The relative block vector</p>
     * @return <p>The block at the given relative position</p>
     */
    public BlockLocation getBlockAt(RelativeBlockVector vector) {
        return DirectionHelper.getBlockAt(getTopLeft(), vector, getYaw());
    }

    /**
     * Removes the special characters |, : and # from a portal name
     *
     * @param input <p>The name to filter</p>
     * @return <p>The filtered name</p>
     */
    private static String filterName(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[|:#]", "").trim();
    }

    /**
     * Gets a list of block locations from a list of relative block vectors
     *
     * @param vectors <p>The relative block vectors to convert</p>
     * @return <p>A list of block locations</p>
     */
    private BlockLocation[] relativeBlockVectorsToBlockLocations(RelativeBlockVector[] vectors) {
        BlockLocation[] locations = new BlockLocation[vectors.length];
        int i = 0;

        for (RelativeBlockVector vector : vectors) {
            locations[i++] = getBlockAt(vector);
        }
        return locations;
    }

    @Override
    public String toString() {
        return String.format("Portal [id=%s, network=%s name=%s, type=%s]", getSignLocation(), network, name,
                gate.getFilename());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((network == null) ? 0 : network.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Portal other = (Portal) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equalsIgnoreCase(other.name)) {
            return false;
        }
        if (network == null) {
            return other.network == null;
        } else {
            return network.equalsIgnoreCase(other.network);
        }
    }
}
