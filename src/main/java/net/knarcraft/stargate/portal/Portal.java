package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.BlockLocation;
import net.knarcraft.stargate.BloxPopulator;
import net.knarcraft.stargate.utility.EconomyHandler;
import net.knarcraft.stargate.RelativeBlockVector;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.event.StargateActivateEvent;
import net.knarcraft.stargate.event.StargateCloseEvent;
import net.knarcraft.stargate.event.StargateDeactivateEvent;
import net.knarcraft.stargate.event.StargateOpenEvent;
import net.knarcraft.stargate.event.StargatePortalEvent;
import net.knarcraft.stargate.utility.EntityHelper;
import org.bukkit.Axis;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
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

public class Portal {

    // Gate location block info
    private final BlockLocation topLeft;
    private final int modX;
    private final int modZ;
    private final float rotX;
    private final Axis rot;

    // Block references
    private final BlockLocation id;
    private final Gate gate;
    private final World world;
    private BlockLocation button;
    private BlockLocation[] frame;
    private BlockLocation[] entrances;
    // Gate information
    private String name;
    private String destination;
    private String lastDestination = "";
    private String network;
    private String ownerName;
    private UUID ownerUUID;
    private boolean verified;
    private boolean fixed;
    private Map<PortalOption, Boolean> options;

    // In-use information
    private Player player;
    private Player activePlayer;
    private List<String> destinations = new ArrayList<>();
    private boolean isOpen = false;
    private long openTime;

    /**
     * Instantiates a new portal
     *
     * @param topLeft     <p>The top-left block of the portal. This is used to decide the positions of the rest of the portal</p>
     * @param modX        <p></p>
     * @param modZ        <p></p>
     * @param rotX        <p></p>
     * @param id          <p>The location of the portal's id block, which is the sign which activated the portal</p>
     * @param button      <p>The location of the portal's open button</p>
     * @param destination <p>The destination defined on the sign's destination line</p>
     * @param name        <p>The name of the portal defined on the sign's first line</p>
     * @param verified    <p>Whether the portal's gate has been verified to match its template</p>
     * @param network     <p>The network the portal belongs to, defined on the sign's network line</p>
     * @param gate        <p>The gate template this portal uses</p>
     * @param ownerUUID   <p>The UUID of the gate's owner</p>
     * @param ownerName   <p>The name of the gate's owner</p>
     * @param options     <p>A map containing all possible portal options</p>
     */
    Portal(BlockLocation topLeft, int modX, int modZ, float rotX, BlockLocation id, BlockLocation button,
           String destination, String name, boolean verified, String network, Gate gate, UUID ownerUUID,
           String ownerName, Map<PortalOption, Boolean> options) {
        this.topLeft = topLeft;
        this.modX = modX;
        this.modZ = modZ;
        this.rotX = rotX;
        this.rot = rotX == 0.0F || rotX == 180.0F ? Axis.X : Axis.Z;
        this.id = id;
        this.destination = destination;
        this.button = button;
        this.verified = verified;
        this.network = network;
        this.name = name;
        this.gate = gate;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.options = options;
        this.world = topLeft.getWorld();
        this.fixed = destination.length() > 0 || this.isRandom() || this.isBungee();

        if (this.isAlwaysOn() && !this.isFixed()) {
            this.options.put(PortalOption.ALWAYS_ON, false);
            Stargate.debug("Portal", "Can not create a non-fixed always-on gate. Setting AlwaysOn = false");
        }

        if (this.isRandom() && !this.isAlwaysOn()) {
            this.options.put(PortalOption.ALWAYS_ON, true);
            Stargate.debug("Portal", "Gate marked as random, set to always-on");
        }

        if (verified) {
            this.drawSign();
        }
    }

    /**
     * Removes the special characters |, : and # from a portal name
     *
     * @param input <p>The name to filter</p>
     * @return <p>The filtered name</p>
     */
    public static String filterName(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[|:#]", "").trim();
    }

    /**
     * Gets whether this portal is currently open
     *
     * @return <p>Whether this portal is open</p>
     */
    public boolean isOpen() {
        return isOpen || isAlwaysOn();
    }

    /**
     * Gets whether this portal is always on
     *
     * @return <p>Whether this portal is always on</p>
     */
    public boolean isAlwaysOn() {
        return this.options.get(PortalOption.ALWAYS_ON);
    }

    /**
     * Gets whether this portal is hidden
     *
     * @return <p>Whether this portal is hidden</p>
     */
    public boolean isHidden() {
        return this.options.get(PortalOption.HIDDEN);
    }

    /**
     * Gets whether this portal is private
     *
     * @return <p>Whether this portal is private</p>
     */
    public boolean isPrivate() {
        return this.options.get(PortalOption.PRIVATE);
    }

    /**
     * Gets whether this portal is free
     *
     * @return <p>Whether this portal is free</p>
     */
    public boolean isFree() {
        return this.options.get(PortalOption.FREE);
    }

    /**
     * Gets whether this portal is backwards
     *
     * <p>A backwards portal is one where players exit through the back.</p>
     *
     * @return <p>Whether this portal is backwards</p>
     */
    public boolean isBackwards() {
        return this.options.get(PortalOption.BACKWARDS);
    }

    /**
     * Gets whether this portal is shown on the network even if it's always on
     *
     * @return <p>Whether portal gate is shown</p>
     */
    public boolean isShown() {
        return this.options.get(PortalOption.SHOW);
    }

    /**
     * Gets whether this portal shows no network
     *
     * @return <p>Whether this portal shows no network/p>
     */
    public boolean isNoNetwork() {
        return this.options.get(PortalOption.NO_NETWORK);
    }

    /**
     * Gets whether this portal goes to a random location on the network
     *
     * @return <p>Whether this portal goes to a random location</p>
     */
    public boolean isRandom() {
        return this.options.get(PortalOption.RANDOM);
    }

    /**
     * Gets whether this portal is a bungee portal
     *
     * @return <p>Whether this portal is a bungee portal</p>
     */
    public boolean isBungee() {
        return this.options.get(PortalOption.BUNGEE);
    }

    /**
     * Gets the rotation of the portal in degrees
     *
     * @return <p>The rotation of the portal</p>
     */
    public float getRotation() {
        return rotX;
    }

    public Axis getAxis() {
        return rot;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public long getOpenTime() {
        return openTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = filterName(name);
        drawSign();
    }

    public Portal getDestination(Player player) {
        if (isRandom()) {
            destinations = PortalHandler.getDestinations(this, player, getNetwork());
            if (destinations.size() == 0) {
                return null;
            }
            String dest = destinations.get((new Random()).nextInt(destinations.size()));
            destinations.clear();
            return PortalHandler.getByName(dest, getNetwork());
        }
        return PortalHandler.getByName(destination, getNetwork());
    }

    public Portal getDestination() {
        return getDestination(null);
    }

    public void setDestination(Portal destination) {
        setDestination(destination.getName());
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationName() {
        return destination;
    }

    public Gate getGate() {
        return gate;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwner(UUID owner) {
        this.ownerUUID = owner;
    }

    public boolean isOwner(Player player) {
        if (this.ownerUUID != null) {
            return player.getUniqueId().compareTo(this.ownerUUID) == 0;
        } else {
            return player.getName().equalsIgnoreCase(this.ownerName);
        }
    }

    public BlockLocation[] getEntrances() {
        if (entrances == null) {
            RelativeBlockVector[] space = gate.getLayout().getEntrances();
            entrances = new BlockLocation[space.length];
            int i = 0;

            for (RelativeBlockVector vector : space) {
                entrances[i++] = getBlockAt(vector);
            }
        }
        return entrances;
    }

    public BlockLocation[] getFrame() {
        if (frame == null) {
            RelativeBlockVector[] border = gate.getLayout().getBorder();
            frame = new BlockLocation[border.length];
            int i = 0;

            for (RelativeBlockVector vector : border) {
                frame[i++] = getBlockAt(vector);
            }
        }

        return frame;
    }

    public BlockLocation getSign() {
        return id;
    }

    public World getWorld() {
        return world;
    }

    public BlockLocation getButton() {
        return button;
    }

    public void setButton(BlockLocation button) {
        this.button = button;
    }

    public boolean open(boolean force) {
        return open(null, force);
    }

    public boolean open(Player openFor, boolean force) {
        // Call the StargateOpenEvent
        StargateOpenEvent event = new StargateOpenEvent(openFor, this, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        force = event.getForce();

        if (isOpen() && !force) {
            return false;
        }

        Material openType = gate.getPortalOpenBlock();
        Axis axis = openType == Material.NETHER_PORTAL ? rot : null;
        for (BlockLocation inside : getEntrances()) {
            Stargate.blockPopulatorQueue.add(new BloxPopulator(inside, openType, axis));
        }

        isOpen = true;
        openTime = System.currentTimeMillis() / 1000;
        Stargate.openList.add(this);
        Stargate.activeList.remove(this);

        // Open remote gate
        if (!isAlwaysOn()) {
            player = openFor;

            Portal end = getDestination();
            // Only open dest if it's not-fixed or points at this gate
            if (!isRandom() && end != null && (!end.isFixed() || end.getDestinationName().equalsIgnoreCase(getName())) && !end.isOpen()) {
                end.open(openFor, false);
                end.setDestination(this);
                if (end.isVerified()) end.drawSign();
            }
        }

        return true;
    }

    public void close(boolean force) {
        if (!isOpen) return;
        // Call the StargateCloseEvent
        StargateCloseEvent event = new StargateCloseEvent(this, force);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        force = event.getForce();

        if (isAlwaysOn() && !force) return; // Only close always-open if forced

        // Close this gate, then the dest gate.
        Material closedType = gate.getPortalClosedBlock();
        for (BlockLocation inside : getEntrances()) {
            Stargate.blockPopulatorQueue.add(new BloxPopulator(inside, closedType));
        }

        player = null;
        isOpen = false;
        Stargate.openList.remove(this);
        Stargate.activeList.remove(this);

        if (!isAlwaysOn()) {
            Portal end = getDestination();

            if (end != null && end.isOpen()) {
                end.deactivate(); // Clear it's destination first.
                end.close(false);
            }
        }

        deactivate();
    }

    public boolean isOpenFor(Player player) {
        if (!isOpen) {
            return false;
        }
        if ((isAlwaysOn()) || (this.player == null)) {
            return true;
        }
        return (player != null) && (player.getName().equalsIgnoreCase(this.player.getName()));
    }

    /**
     * Gets whether this portal points to a fixed exit portal
     *
     * @return <p>True if this portal points to a fixed exit portal</p>
     */
    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isPowered() {
        RelativeBlockVector[] controls = gate.getLayout().getControls();

        for (RelativeBlockVector vector : controls) {
            BlockData data = getBlockAt(vector).getBlock().getBlockData();

            if (data instanceof Powerable && ((Powerable) data).isPowered()) {
                return true;
            }
        }

        return false;
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
        adjustRotation(traveller, exit, origin);

        // Call the StargatePortalEvent to allow plugins to change destination
        if (!origin.equals(this)) {
            StargatePortalEvent stargatePortalEvent = new StargatePortalEvent(player, origin, this, exit);
            Stargate.server.getPluginManager().callEvent(stargatePortalEvent);
            // Teleport is cancelled
            if (stargatePortalEvent.isCancelled()) {
                origin.teleport(player, origin, event);
                return;
            }
            // Update exit if needed
            exit = stargatePortalEvent.getExit();
        }

        // If no event is passed in, assume it's a teleport, and act as such
        if (event == null) {
            exit.setYaw(this.getRotation());
            player.teleport(exit);
        } else {
            // The new method to teleport in a move event is set the "to" field.
            event.setTo(exit);
        }
    }

    /**
     * Adjusts the rotation of the player to face out from the portal
     *
     * @param entry <p>The location the player entered from</p>
     * @param exit <p>The location the player will exit from</p>
     * @param origin <p>The portal the player entered from</p>
     */
    private void adjustRotation(Location entry, Location exit, Portal origin) {
        int adjust = 180;
        if (isBackwards() != origin.isBackwards()) {
            adjust = 0;
        }
        exit.setYaw(entry.getYaw() - origin.getRotation() + this.getRotation() + adjust);
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

        // Stop and teleport
        vehicle.setVelocity(new Vector());

        // Get new velocity
        final Vector newVelocity = new Vector(modX, 0.0F, modZ);
        newVelocity.multiply(velocity);

        List<Entity> passengers = vehicle.getPassengers();
        World vehicleWorld = exit.getWorld();
        if (vehicleWorld == null) {
            Stargate.log.warning(Stargate.getString("prefix") + "Unable to get the world to teleport the vehicle to");
            return;
        }

        if (!passengers.isEmpty()) {
            if (vehicle instanceof RideableMinecart || vehicle instanceof Boat) {
                putPassengersInNewVehicle(vehicle, passengers, vehicleWorld, exit, newVelocity);
            } else {
                teleportLivingVehicle(vehicle, exit, passengers);
            }
        } else {
            vehicle.teleport(exit);
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> vehicle.setVelocity(newVelocity), 1);
        }
    }

    /**
     * Teleport a vehicle which is not a minecart or a boat
     *
     * @param vehicle <p>The vehicle to teleport</p>
     * @param exit <p>The location the vehicle will exit</p>
     * @param passengers <p>The passengers of the vehicle</p>
     */
    private void teleportLivingVehicle(Vehicle vehicle, Location exit, List<Entity> passengers) {
        vehicle.eject();
        vehicle.teleport(exit);
        handleVehiclePassengers(passengers, vehicle, exit);
    }

    /**
     * Creates a new vehicle equal to the player's previous vehicle and
     *
     * @param vehicle <p>The player's old vehicle</p>
     * @param passengers <p>A list of all passengers in the vehicle</p>
     * @param vehicleWorld <p>The world to spawn the new vehicle in</p>
     * @param exit <p>The exit location to spawn the new vehicle on</p>
     * @param newVelocity <p>The new velocity of the new vehicle</p>
     */
    private void putPassengersInNewVehicle(Vehicle vehicle, List<Entity> passengers, World vehicleWorld, Location exit,
                                           Vector newVelocity) {
        Vehicle newVehicle = vehicleWorld.spawn(exit, vehicle.getClass());
        vehicle.eject();
        vehicle.remove();
        vehicle.setRotation(exit.getYaw(), exit.getPitch());
        handleVehiclePassengers(passengers, newVehicle, exit);
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> newVehicle.setVelocity(newVelocity), 1);
    }

    /**
     * Ejects, teleports and adds all passengers to the target vehicle
     *
     * @param passengers <p>The passengers to handle</p>
     * @param targetVehicle <p>The vehicle the passengers should be put into</p>
     * @param exit <p>The exit location to teleport the passengers to</p>
     */
    private void handleVehiclePassengers(List<Entity> passengers, Vehicle targetVehicle, Location exit) {
        for (Entity passenger : passengers) {
            passenger.eject();
            if (!passenger.teleport(exit)) {
                Stargate.debug("handleVehiclePassengers", "Failed to teleport passenger");
            }
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> targetVehicle.addPassenger(passenger), 1);
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
        if (gate.getLayout().getExit() != null) {
            BlockLocation exit = getBlockAt(gate.getLayout().getExit());
            int back = (isBackwards()) ? -1 : 1;
            //TODO: Improve positioning to place the entity just far enough from the portal not to suffocate
            double entitySize = EntityHelper.getEntityMaxSize(entity);
            exitLocation = exit.modRelativeLoc(0D, 0D, entitySize, traveller.getYaw(),
                    traveller.getPitch(), modX * back, 1, modZ * back);
        } else {
            Stargate.log.log(Level.WARNING, Stargate.getString("prefix") + "Missing destination point in .gate file " + gate.getFilename());
        }

        if (exitLocation != null) {
            //Prevent traveller from spawning inside a slab
            BlockData blockData = getWorld().getBlockAt(exitLocation).getBlockData();
            if (blockData instanceof Bisected && ((Bisected) blockData).getHalf() == Bisected.Half.BOTTOM) {
                exitLocation.add(0, 0.5, 0);
            }

            exitLocation.setPitch(traveller.getPitch());
            return exitLocation;
        } else {
            Stargate.log.log(Level.WARNING, Stargate.getString("prefix") + "Unable to generate exit location");
        }
        return traveller;
    }

    /**
     * Checks whether the chunk the portal is located at is loaded
     *
     * @return <p>True if the chunk containing the portal is loaded</p>
     */
    public boolean isChunkLoaded() {
        //TODO: Improve this in the case where the portal sits between two chunks
        return getWorld().isChunkLoaded(topLeft.getBlock().getChunk());
    }

    /**
     * Gets the identity (sign) location of the portal
     *
     * @return <p>The identity location of the portal</p>
     */
    public BlockLocation getId() {
        return this.id;
    }

    public int getModX() {
        return this.modX;
    }

    public int getModZ() {
        return this.modZ;
    }

    public float getRotX() {
        return this.rotX;
    }

    /**
     * Gets the location of the top-left block of the portal
     *
     * @return <p>The location of the top-left portal block</p>
     */
    public BlockLocation getTopLeft() {
        return this.topLeft;
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
        return gate.matches(topLeft, modX, modZ);
    }

    /**
     * Activates this portal for the given player
     *
     * @param player <p>The player to activate the portal for</p>
     * @return <p>True if the portal was activated</p>
     */
    public boolean activate(Player player) {
        destinations.clear();
        destination = "";
        Stargate.activeList.add(this);
        activePlayer = player;
        String network = getNetwork();
        destinations = PortalHandler.getDestinations(this, player, network);
        if (Stargate.sortLists) {
            Collections.sort(destinations);
        }
        if (Stargate.destMemory && !lastDestination.isEmpty() && destinations.contains(lastDestination)) {
            destination = lastDestination;
        }

        StargateActivateEvent event = new StargateActivateEvent(this, player, destinations, destination);
        Stargate.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Stargate.activeList.remove(this);
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

        Stargate.activeList.remove(this);
        if (isFixed()) {
            return;
        }
        destinations.clear();
        destination = "";
        activePlayer = null;
        drawSign();
    }

    public boolean isActive() {
        return isFixed() || (destinations.size() > 0);
    }

    public void cycleDestination(Player player) {
        cycleDestination(player, 1);
    }

    public void cycleDestination(Player player, int dir) {
        boolean activate = false;
        if (!isActive() || getActivePlayer() != player) {
            // If the event is cancelled, return
            if (!activate(player)) {
                return;
            }
            Stargate.debug("cycleDestination", "Network Size: " + PortalHandler.getNetwork(network).size());
            Stargate.debug("cycleDestination", "Player has access to: " + destinations.size());
            activate = true;
        }

        if (destinations.size() == 0) {
            Stargate.sendMessage(player, Stargate.getString("destEmpty"));
            return;
        }

        if (!Stargate.destMemory || !activate || lastDestination.isEmpty()) {
            int index = destinations.indexOf(destination);
            index += dir;
            if (index >= destinations.size())
                index = 0;
            else if (index < 0)
                index = destinations.size() - 1;
            destination = destinations.get(index);
            lastDestination = destination;
        }
        openTime = System.currentTimeMillis() / 1000;
        drawSign();
    }

    /**
     * Draws the sign on this portal
     */
    public final void drawSign() {
        BlockState state = id.getBlock().getState();
        if (!(state instanceof Sign)) {
            Stargate.log.warning(Stargate.getString("prefix") + "Sign block is not a Sign object");
            Stargate.debug("Portal::drawSign", "Block: " + id.getBlock().getType() + " @ " + id.getBlock().getLocation());
            return;
        }

        Sign sign = (Sign) state;

        //Clear sign
        for (int index = 0; index <= 3; index++) {
            sign.setLine(index, "");
        }
        Stargate.setLine(sign, 0, "-" + name + "-");

        if (!isActive()) {
            //Default sign text
            drawInactiveSign(sign);
        } else {
            if (isBungee()) {
                //Bungee sign
                drawBungeeSign(sign);
            } else if (isFixed()) {
                //Sign pointing at one other portal
                drawFixedSign(sign);
            } else {
                //Networking stuff
                drawNetworkSign(sign);
            }
        }

        sign.update();
    }

    /**
     * Draws a sign with chooseable network locations
     *
     * @param sign <p>The sign to draw on</p>
     */
    private void drawNetworkSign(Sign sign) {
        int maxIndex = destinations.size() - 1;
        int signLineIndex = 0;
        int destinationIndex = destinations.indexOf(destination);
        boolean freeGatesGreen = EconomyHandler.useEconomy() && EconomyHandler.freeGatesGreen;

        //Last entry, and not only entry. Draw the entry two previously
        if ((destinationIndex == maxIndex) && (maxIndex > 1)) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex - 2);
        }
        //Not first entry. Draw the previous entry
        if (destinationIndex > 0) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex - 1);
        }
        //Draw the chosen entry (line 2 or 3)
        drawNetworkSignChosenLine(freeGatesGreen, sign, ++signLineIndex);
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 1) && (++signLineIndex <= 3)) {
            drawNetworkSignLine(freeGatesGreen, sign, signLineIndex, destinationIndex + 1);
        }
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 2) && (++signLineIndex <= 3)) {
            drawNetworkSignLine(freeGatesGreen, sign, signLineIndex, destinationIndex + 2);
        }
    }

    /**
     * Draws the chosen destination on one sign line
     *
     * @param freeGatesGreen <p>Whether to display free gates in a green color</p>
     * @param sign <p>The sign to draw on</p>
     * @param signLineIndex <p>The line to draw on</p>
     */
    private void drawNetworkSignChosenLine(boolean freeGatesGreen, Sign sign, int signLineIndex) {
        if (freeGatesGreen) {
            Portal destination = PortalHandler.getByName(this.destination, network);
            boolean green = Stargate.isFree(activePlayer, this, destination);
            Stargate.setLine(sign, signLineIndex, (green ? ChatColor.DARK_GREEN : "") + ">" + this.destination + "<");
        } else {
            Stargate.setLine(sign, signLineIndex, " >" + destination + "< ");
        }
    }

    /**
     * Draws one network destination on one sign line
     *
     * @param freeGatesGreen <p>Whether to display free gates in a green color</p>
     * @param sign <p>The sign to draw on</p>
     * @param signLineIndex <p>The line to draw on</p>
     * @param destinationIndex <p>The index of the destination to draw</p>
     */
    private void drawNetworkSignLine(boolean freeGatesGreen, Sign sign, int signLineIndex, int destinationIndex) {
        if (freeGatesGreen) {
            Portal destination = PortalHandler.getByName(destinations.get(destinationIndex), network);
            boolean green = Stargate.isFree(activePlayer, this, destination);
            Stargate.setLine(sign, signLineIndex, (green ? ChatColor.DARK_GREEN : "") + destinations.get(destinationIndex));
        } else {
            Stargate.setLine(sign, signLineIndex, destinations.get(destinationIndex));
        }
    }

    /**
     * Draws a bungee sign
     *
     * @param sign <p>The sign to draw on</p>
     */
    private void drawBungeeSign(Sign sign) {
        Stargate.setLine(sign, 1, Stargate.getString("bungeeSign"));
        Stargate.setLine(sign, 2, ">" + destination + "<");
        Stargate.setLine(sign, 3, "[" + network + "]");
    }

    /**
     * Draws an inactive sign
     *
     * @param sign <p>The sign to draw on</p>
     */
    private void drawInactiveSign(Sign sign) {
        Stargate.setLine(sign, 1, Stargate.getString("signRightClick"));
        Stargate.setLine(sign, 2, Stargate.getString("signToUse"));
        if (!isNoNetwork()) {
            Stargate.setLine(sign, 3, "(" + network + ")");
        } else {
            Stargate.setLine(sign, 3, "");
        }
    }

    /**
     * Draws a sign pointing to a fixed location
     *
     * @param sign <p>The sign to draw on</p>
     */
    private void drawFixedSign(Sign sign) {
        if (isRandom()) {
            Stargate.setLine(sign, 1, "> " + Stargate.getString("signRandom") + " <");
        } else {
            Stargate.setLine(sign, 1, ">" + destination + "<");
        }
        if (isNoNetwork()) {
            Stargate.setLine(sign, 2, "");
        } else {
            Stargate.setLine(sign, 2, "(" + network + ")");
        }
        Portal destination = PortalHandler.getByName(this.destination, network);
        if (destination == null && !isRandom()) {
            Stargate.setLine(sign, 3, Stargate.getString("signDisconnected"));
        } else {
            Stargate.setLine(sign, 3, "");
        }
    }

    /**
     * Gets the block at a relative block vector location
     *
     * @param vector <p>The relative block vector</p>
     * @return <p>The block at the given relative position</p>
     */
    BlockLocation getBlockAt(RelativeBlockVector vector) {
        return topLeft.modRelative(vector.getRight(), vector.getDepth(), vector.getDistance(), modX, 1, modZ);
    }

    @Override
    public String toString() {
        return String.format("Portal [id=%s, network=%s name=%s, type=%s]", id, network, name, gate.getFilename());
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
