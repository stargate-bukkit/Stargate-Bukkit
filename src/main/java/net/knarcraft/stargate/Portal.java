package net.knarcraft.stargate;

import net.knarcraft.stargate.event.StargateActivateEvent;
import net.knarcraft.stargate.event.StargateCloseEvent;
import net.knarcraft.stargate.event.StargateDeactivateEvent;
import net.knarcraft.stargate.event.StargateOpenEvent;
import net.knarcraft.stargate.event.StargatePortalEvent;
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
    private BlockLocation button;
    private BlockLocation[] frame;
    private BlockLocation[] entrances;

    // Gate information
    private String name;
    private String destination;
    private String lastDestination = "";
    private String network;
    private final Gate gate;
    private String ownerName;
    private UUID ownerUUID;
    private final World world;
    private boolean verified;
    private boolean fixed;

    // Options
    private boolean hidden = false;
    private boolean alwaysOn = false;
    private boolean isPrivate = false;
    private boolean free = false;
    private boolean backwards = false;
    private boolean show = false;
    private boolean noNetwork = false;
    private boolean random = false;
    private boolean bungee = false;

    // In-use information
    private Player player;
    private Player activePlayer;
    private ArrayList<String> destinations = new ArrayList<>();
    private boolean isOpen = false;
    private long openTime;

    Portal(BlockLocation topLeft, int modX, int modZ, float rotX, BlockLocation id, BlockLocation button,
           String dest, String name, boolean verified, String network, Gate gate, UUID ownerUUID, String ownerName) {
        this.topLeft = topLeft;
        this.modX = modX;
        this.modZ = modZ;
        this.rotX = rotX;
        this.rot = rotX == 0.0F || rotX == 180.0F ? Axis.X : Axis.Z;
        this.id = id;
        this.destination = dest;
        this.button = button;
        this.verified = verified;
        this.network = network;
        this.name = name;
        this.gate = gate;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.world = topLeft.getWorld();
        this.fixed = dest.length() > 0 || this.random || this.bungee;

        if (this.isAlwaysOn() && !this.isFixed()) {
            this.alwaysOn = false;
            Stargate.debug("Portal", "Can not create a non-fixed always-on gate. Setting AlwaysOn = false");
        }

        if (this.random && !this.isAlwaysOn()) {
            this.alwaysOn = true;
            Stargate.debug("Portal", "Gate marked as random, set to always-on");
        }

        if (verified) {
            this.drawSign();
        }
    }

    Portal(BlockLocation topLeft, int modX, int modZ,
           float rotX, BlockLocation id, BlockLocation button,
           String dest, String name,
           boolean verified, String network, Gate gate, UUID ownerUUID, String ownerName,
           boolean hidden, boolean alwaysOn, boolean isPrivate, boolean free, boolean backwards, boolean show, boolean noNetwork, boolean random, boolean bungee) {
        this.topLeft = topLeft;
        this.modX = modX;
        this.modZ = modZ;
        this.rotX = rotX;
        this.rot = rotX == 0.0F || rotX == 180.0F ? Axis.X : Axis.Z;
        this.id = id;
        this.destination = dest;
        this.button = button;
        this.verified = verified;
        this.network = network;
        this.name = name;
        this.gate = gate;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.hidden = hidden;
        this.alwaysOn = alwaysOn;
        this.isPrivate = isPrivate;
        this.free = free;
        this.backwards = backwards;
        this.show = show;
        this.noNetwork = noNetwork;
        this.random = random;
        this.bungee = bungee;
        this.world = topLeft.getWorld();
        this.fixed = dest.length() > 0 || this.random || this.bungee;

        if (this.isAlwaysOn() && !this.isFixed()) {
            this.alwaysOn = false;
            Stargate.debug("Portal", "Can not create a non-fixed always-on gate. Setting AlwaysOn = false");
        }

        if (this.random && !this.isAlwaysOn()) {
            this.alwaysOn = true;
            Stargate.debug("Portal", "Gate marked as random, set to always-on");
        }

        if (verified) {
            this.drawSign();
        }
    }

    /**
     * Option Check Functions
     */
    public boolean isOpen() {
        return isOpen || isAlwaysOn();
    }

    public boolean isAlwaysOn() {
        return alwaysOn;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isFree() {
        return free;
    }

    public boolean isBackwards() {
        return backwards;
    }

    public boolean isShown() {
        return show;
    }

    public boolean isNoNetwork() {
        return noNetwork;
    }

    public boolean isRandom() {
        return random;
    }

    public boolean isBungee() {
        return bungee;
    }

    public Portal setAlwaysOn(boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
        return this;
    }

    public Portal setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public Portal setPrivate(boolean priv) {
        this.isPrivate = priv;
        return this;
    }

    public Portal setFree(boolean free) {
        this.free = free;
        return this;
    }

    public Portal setBackwards(boolean backwards) {
        this.backwards = backwards;
        return this;
    }

    public Portal setShown(boolean show) {
        this.show = show;
        return this;
    }


    public Portal setNoNetwork(boolean noNetwork) {
        this.noNetwork = noNetwork;
        return this;
    }

    public Portal setRandom(boolean random) {
        this.random = random;
        return this;
    }

    public Portal setBungee(boolean bungee) {
        this.bungee = bungee;
        return this;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Getters and Setters
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
            RelativeBlockVector[] space = gate.getEntrances();
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
            RelativeBlockVector[] border = gate.getBorder();
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
        if (event.isCancelled()) return false;
        force = event.getForce();

        if (isOpen() && !force) return false;

        Material openType = gate.getPortalBlockOpen();
        Axis ax = openType == Material.NETHER_PORTAL ? rot : null;
        for (BlockLocation inside : getEntrances()) {
            Stargate.blockPopulatorQueue.add(new BloxPopulator(inside, openType, ax));
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
            if (!random && end != null && (!end.isFixed() || end.getDestinationName().equalsIgnoreCase(getName())) && !end.isOpen()) {
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
        Material closedType = gate.getPortalBlockClosed();
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
     * @return <p>True if this portal points to a fixed exit portal</p>
     */
    public boolean isFixed() {
        return fixed;
    }

    public boolean isPowered() {
        RelativeBlockVector[] controls = gate.getControls();

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
     * @param player <p>The player to teleport</p>
     * @param origin <p>The portal the player teleports from</p>
     * @param event <p>The player move event triggering the event</p>
     */
    public void teleport(Player player, Portal origin, PlayerMoveEvent event) {
        Location traveller = player.getLocation();
        Location exit = getExit(player, traveller);

        //Rotate the player to face out from the portal
        int adjust = 180;
        if (isBackwards() != origin.isBackwards()) {
            adjust = 0;
        }
        exit.setYaw(traveller.getYaw() - origin.getRotation() + this.getRotation() + adjust);

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
     * Teleports a vehicle to this portal
     * @param vehicle <p>The vehicle to teleport</p>
     */
    public void teleport(final Vehicle vehicle) {
        Location traveller = new Location(this.world, vehicle.getLocation().getX(), vehicle.getLocation().getY(),
                vehicle.getLocation().getZ());
        Stargate.log.info(Stargate.getString("prefix") + "Location of vehicle is " + traveller);
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
                putPlayerInNewVehicle(vehicle, passengers, vehicleWorld, exit, newVelocity);
                return;
            }
            vehicle.eject();
            handleVehiclePassengers(vehicle, passengers, vehicle, exit);
            vehicle.teleport(exit);
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> vehicle.setVelocity(newVelocity), 3);
        } else {
            Stargate.log.info(Stargate.getString("prefix") + "Teleported vehicle to " + exit);
            vehicle.teleport(exit);
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> {
                vehicle.setVelocity(newVelocity);
            }, 1);
        }
    }

    private void putPlayerInNewVehicle(Vehicle vehicle, List<Entity> passengers, World vehicleWorld, Location exit, Vector newVelocity) {
        Vehicle newVehicle = vehicleWorld.spawn(exit, vehicle.getClass());
        vehicle.eject();
        vehicle.remove();
        handleVehiclePassengers(vehicle, passengers, newVehicle, exit);
        Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> newVehicle.setVelocity(newVelocity), 1);
    }

    private void handleVehiclePassengers(Vehicle sourceVehicle, List<Entity> passengers, Vehicle targetVehicle, Location exit) {
        for (Entity passenger : passengers) {
            passenger.eject();
            Stargate.log.info("Teleporting passenger" + passenger + " to " + exit);
            if (!passenger.teleport(exit)) {
                Stargate.log.info("Failed to teleport passenger");
            }
            Stargate.server.getScheduler().scheduleSyncDelayedTask(Stargate.stargate, () -> targetVehicle.addPassenger(passenger), 1);
        }
    }

    /**
     * Gets the exit location for a given entity and current location
     * @param entity <p>The entity to teleport (used to determine distance from portal to avoid suffocation)</p>
     * @param traveller <p>The location of the entity travelling</p>
     * @return <p>The location the entity should be teleported to.</p>
     */
    public Location getExit(Entity entity, Location traveller) {
        Location exitLocation = null;
        // Check if the gate has an exit block
        if (gate.getExit() != null) {
            BlockLocation exit = getBlockAt(gate.getExit());
            int back = (isBackwards()) ? -1 : 1;
            double entitySize = Math.ceil((float) Math.max(entity.getBoundingBox().getWidthX(), entity.getBoundingBox().getWidthZ()));
            exitLocation = exit.modRelativeLoc(0D, 0D, entitySize, traveller.getYaw(), traveller.getPitch(), modX * back, 1, modZ * back);
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
     * @return <p>True if the chunk containing the portal is loaded</p>
     */
    public boolean isChunkLoaded() {
        //TODO: Improve this in the case where the portal sits between two chunks
        return getWorld().isChunkLoaded(topLeft.getBlock().getChunk());
    }

    /**
     * Gets the identity (sign) location of the portal
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
     * @return <p>The location of the top-left portal block</p>
     */
    public BlockLocation getTopLeft() {
        return this.topLeft;
    }

    /**
     * Verifies that all control blocks in this portal follows its gate template
     * @return <p>True if all control blocks were verified</p>
     */
    public boolean isVerified() {
        verified = true;
        if (!Stargate.verifyPortals) {
            return true;
        }
        for (RelativeBlockVector control : gate.getControls()) {
            verified = verified && getBlockAt(control).getBlock().getType().equals(gate.getControlBlock());
        }
        return verified;
    }

    /**
     * Gets the result of the last portal verification
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

    public final void drawSign() {
        BlockState state = id.getBlock().getState();
        if (!(state instanceof Sign)) {
            Stargate.log.warning(Stargate.getString("prefix") + "Sign block is not a Sign object");
            Stargate.debug("Portal::drawSign", "Block: " + id.getBlock().getType() + " @ " + id.getBlock().getLocation());
            return;
        }
        Sign sign = (Sign) state;
        Stargate.setLine(sign, 0, "-" + name + "-");
        int max = destinations.size() - 1;
        int done = 0;

        if (!isActive()) {
            Stargate.setLine(sign, ++done, Stargate.getString("signRightClick"));
            Stargate.setLine(sign, ++done, Stargate.getString("signToUse"));
            if (!noNetwork) {
                Stargate.setLine(sign, ++done, "(" + network + ")");
            }
        } else {
            // Awesome new logic for Bungee gates
            if (isBungee()) {
                Stargate.setLine(sign, ++done, Stargate.getString("bungeeSign"));
                Stargate.setLine(sign, ++done, ">" + destination + "<");
                Stargate.setLine(sign, ++done, "[" + network + "]");
            } else if (isFixed()) {
                if (isRandom()) {
                    Stargate.setLine(sign, ++done, "> " + Stargate.getString("signRandom") + " <");
                } else {
                    Stargate.setLine(sign, ++done, ">" + destination + "<");
                }
                if (noNetwork) {
                    Stargate.setLine(sign, ++done, "");
                } else {
                    Stargate.setLine(sign, ++done, "(" + network + ")");
                }
                Portal dest = PortalHandler.getByName(destination, network);
                if (dest == null && !isRandom()) {
                    Stargate.setLine(sign, ++done, Stargate.getString("signDisconnected"));
                } else {
                    Stargate.setLine(sign, ++done, "");
                }
            } else {
                int index = destinations.indexOf(destination);
                if ((index == max) && (max > 1) && (++done <= 3)) {
                    if (EconomyHandler.useEconomy() && EconomyHandler.freeGatesGreen) {
                        Portal dest = PortalHandler.getByName(destinations.get(index - 2), network);
                        boolean green = Stargate.isFree(activePlayer, this, dest);
                        Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + destinations.get(index - 2));
                    } else {
                        Stargate.setLine(sign, done, destinations.get(index - 2));
                    }
                }
                if ((index > 0) && (++done <= 3)) {
                    if (EconomyHandler.useEconomy() && EconomyHandler.freeGatesGreen) {
                        Portal dest = PortalHandler.getByName(destinations.get(index - 1), network);
                        boolean green = Stargate.isFree(activePlayer, this, dest);
                        Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + destinations.get(index - 1));
                    } else {
                        Stargate.setLine(sign, done, destinations.get(index - 1));
                    }
                }
                if (++done <= 3) {
                    if (EconomyHandler.useEconomy() && EconomyHandler.freeGatesGreen) {
                        Portal dest = PortalHandler.getByName(destination, network);
                        boolean green = Stargate.isFree(activePlayer, this, dest);
                        Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + ">" + destination + "<");
                    } else {
                        Stargate.setLine(sign, done, " >" + destination + "< ");
                    }
                }
                if ((max >= index + 1) && (++done <= 3)) {
                    if (EconomyHandler.useEconomy() && EconomyHandler.freeGatesGreen) {
                        Portal dest = PortalHandler.getByName(destinations.get(index + 1), network);
                        boolean green = Stargate.isFree(activePlayer, this, dest);
                        Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + destinations.get(index + 1));
                    } else {
                        Stargate.setLine(sign, done, destinations.get(index + 1));
                    }
                }
                if ((max >= index + 2) && (++done <= 3)) {
                    if (EconomyHandler.useEconomy() && EconomyHandler.freeGatesGreen) {
                        Portal dest = PortalHandler.getByName(destinations.get(index + 2), network);
                        boolean green = Stargate.isFree(activePlayer, this, dest);
                        Stargate.setLine(sign, done, (green ? ChatColor.DARK_GREEN : "") + destinations.get(index + 2));
                    } else {
                        Stargate.setLine(sign, done, destinations.get(index + 2));
                    }
                }
            }
        }

        for (done++; done <= 3; done++) {
            sign.setLine(done, "");
        }

        sign.update();
    }

    /**
     * Gets the block at a relative block vector location
     * @param vector <p>The relative block vector</p>
     * @return <p>The block at the given relative position</p>
     */
    BlockLocation getBlockAt(RelativeBlockVector vector) {
        return topLeft.modRelative(vector.getRight(), vector.getDepth(), vector.getDistance(), modX, 1, modZ);
    }

    /**
     * Removes the special characters |, : and # from a portal name
     * @param input <p>The name to filter</p>
     * @return <p>The filtered name</p>
     */
    public static String filterName(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("[|:#]", "").trim();
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Portal other = (Portal) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equalsIgnoreCase(other.name))
            return false;
        if (network == null) {
            return other.network == null;
        } else {
            return network.equalsIgnoreCase(other.network);
        }
    }
}
