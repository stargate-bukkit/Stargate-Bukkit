package net.TheDgtl.Stargate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.logging.Level;

import net.TheDgtl.Stargate.event.StargateActivateEvent;
import net.TheDgtl.Stargate.event.StargateCloseEvent;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.event.StargateDeactivateEvent;
import net.TheDgtl.Stargate.event.StargateOpenEvent;
import net.TheDgtl.Stargate.event.StargatePortalEvent;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011 Shaun (sturmeh)
 * Copyright (C) 2011 Dinnerbone
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Portal {

    // Static variables used to store portal lists
    private static final HashMap<Blox, Portal> lookupBlocks = new HashMap<>();
    private static final HashMap<Blox, Portal> lookupEntrances = new HashMap<>();
    private static final HashMap<Blox, Portal> lookupControls = new HashMap<>();
    private static final ArrayList<Portal> allPortals = new ArrayList<>();
    private static final HashMap<String, ArrayList<String>> allPortalsNet = new HashMap<>();
    private static final HashMap<String, HashMap<String, Portal>> lookupNamesNet = new HashMap<>();

    private final Random randomNumber = new Random();

    // A list of Bungee gates
    private static final HashMap<String, Portal> bungeePortals = new HashMap<>();

    // Gate location block info
    private final Blox topLeft;
    private final int modX;
    private final int modZ;
    private final float rotX;
    private final Axis rot;

    // Block references
    private final Blox id;
    private Blox button;
    private Blox[] frame;
    private Blox[] entrances;

    // Gate information
    private String name;
    private String destination;
    private String lastDest = "";
    private String network;
    private final Gate gate;
    private final String ownerName;
    private UUID ownerUUID;
    private final World world;
    private boolean verified;
    private boolean fixed;

    // Options
    private boolean hidden;
    private boolean alwaysOn;
    private boolean priv;
    private boolean free;
    private boolean backwards;
    private boolean show;
    private boolean noNetwork;
    private boolean random;
    private final boolean bungee;

    // In-use information
    private Player player;
    private Player activePlayer;
    private List<String> destinations = new ArrayList<>();
    private boolean isOpen = false;
    private long openTime;

    // di
    private final Stargate stargate;

    // todo Woh

    private Portal(Stargate stargate, Blox topLeft, int modX, int modZ,
                   float rotX, Blox id, Blox button,
                   String dest, String name,
                   boolean verified, String network, Gate gate, UUID ownerUUID, String ownerName,
                   boolean hidden, boolean alwaysOn, boolean priv, boolean free, boolean backwards, boolean show, boolean noNetwork, boolean random, boolean bungee) {
        this.stargate = stargate;
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
        this.priv = priv;
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
        return priv;
    }

    public boolean isFree() {
        return free;
    }

    public boolean isBackwards() {
        return backwards;
    }

    private boolean isShown() {
        return show;
    }

    private boolean isNoNetwork() {
        return noNetwork;
    }

    public boolean isRandom() {
        return random;
    }

    public boolean isBungee() {
        return bungee;
    }

    /**
     * Getters and Setters
     */

    public float getRotation() {
        return rotX;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public String getNetwork() {
        return network;
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
        if (!isRandom()) {
            return Portal.getByName(destination, getNetwork());
        }

        destinations = getDestinations(player, getNetwork());

        if (destinations.isEmpty()) {
            return null;
        }

        int randomize = randomNumber.nextInt(destinations.size());
        String dest = destinations.get(randomize);
        destinations.clear();

        return Portal.getByName(dest, getNetwork());
    }

    public Portal getDestination() {
        return getDestination(null);
    }

    private void setDestination(Portal destination) {
        setDestination(destination.getName());
    }

    private void setDestination(String destination) {
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

    public boolean isOwner(Player player) {
        if (this.ownerUUID != null) {
            return player.getUniqueId().compareTo(this.ownerUUID) == 0;
        } else {
            return player.getName().equalsIgnoreCase(this.ownerName);
        }
    }

    private Blox[] getEntrances() {
        if (entrances == null) {
            RelativeBlockVector[] space = gate.getEntrances();
            entrances = new Blox[space.length];
            int i = 0;

            for (RelativeBlockVector vector : space) {
                entrances[i++] = getBlockAt(vector);
            }
        }

        return entrances;
    }

    private Blox[] getFrame() {
        if (frame == null) {
            RelativeBlockVector[] border = gate.getBorder();
            frame = new Blox[border.length];

            int i = 0;
            for (RelativeBlockVector vector : border) {
                frame[i++] = getBlockAt(vector);
            }
        }

        return frame;
    }

    public World getWorld() {
        return world;
    }

    private void setButton(Blox button) {
        this.button = button;
    }

    
    /**
     * Makes some checks before a open sequence 
     * @param player
     * @param force
     */
    public void innitateOpen(Player player,boolean force) {
    	Portal destination = getDestination();

        // Always-open gate -- Do nothing
        if (isAlwaysOn()) return;

        // Random gate -- Do nothing
        if (isRandom()) return;

        // Invalid destination
        if ((destination == null) || (destination == this)) {
            stargate.sendMessage(player, stargate.getString("invalidMsg"));
            return;
        }

        // Gate is already open
        if (isOpen()) {
            // Close if this player opened the gate
            if (getActivePlayer() == player) {
                close(false);
            }
            return;
        }

        // Gate that someone else is using -- Deny access
        if ((!isFixed()) && isActive() && (getActivePlayer() != player)) {
        	stargate.sendMessage(player, stargate.getString("denyMsg"));
            return;
        }

        // Check if the player can use the private gate
        if (isPrivate() && !canPrivate(player)) {
            stargate.sendMessage(player, stargate.getString("denyMsg"));
            return;
        }

        // Destination blocked
        if ((destination.isOpen()) && (!destination.isAlwaysOn())) {
            stargate.sendMessage(player, stargate.getString("blockMsg"));
            return;
        }

        // Open gate
        open(player, false);
        
    }

    /**
     * Check if the player can use this private gate
     */
    public boolean canPrivate(Player player) {
        // Check if the player is the owner of the gate
        if (this.isOwner(player)) return true;
        // The player is an admin with the ability to use private gates
        return PermissionManager.hasPerm(player, "stargate.admin") || PermissionManager.hasPerm(player, "stargate.admin.private");
    }
    
    public boolean open(boolean force) {
        return open(null, force);
    }
    public boolean open(Player openFor, boolean force) {
        // Call the StargateOpenEvent
        StargateOpenEvent event = new StargateOpenEvent(openFor, this, force);
        stargate.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        force = event.getForce();

        if (isOpen() && !force) return false;

        Material openType = gate.getPortalBlockOpen();
        Axis ax = openType == Material.NETHER_PORTAL ? rot : null;
        for (Blox inside : getEntrances()) {
            stargate.getBlockPopulatorQueue().add(new BloxPopulator(inside, openType, ax));
        }

        isOpen = true;
        openTime = System.currentTimeMillis() / 1000;
        stargate.getOpenList().add(this);
        stargate.getActiveList().remove(this);

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
        stargate.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        force = event.getForce();

        if (isAlwaysOn() && !force) return; // Only close always-open if forced

        // Close this gate, then the dest gate.
        Material closedType = gate.getPortalBlockClosed();
        for (Blox inside : getEntrances()) {
            stargate.getBlockPopulatorQueue().add(new BloxPopulator(inside, closedType));
        }

        player = null;
        isOpen = false;
        stargate.getOpenList().remove(this);
        stargate.getActiveList().remove(this);

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

    public boolean isFixed() {
        return fixed;
    }
    
    /**
     * Not used anywhere, but might be of use in later implementations
     * @return
     */
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

    public void teleport(Player player, Portal origin, PlayerMoveEvent event) {
        Location traveller = player.getLocation();
        Location exit = getExit(traveller);

        // Handle backwards gates
        int adjust = 180;
        if (isBackwards() != origin.isBackwards())
            adjust = 0;
        exit.setYaw(traveller.getYaw() - origin.getRotation() + this.getRotation() + adjust);

        // Call the StargatePortalEvent to allow plugins to change destination
        if (!origin.equals(this)) {
            StargatePortalEvent pEvent = new StargatePortalEvent(player, origin, this, exit);
            stargate.getServer().getPluginManager().callEvent(pEvent);
            // Teleport is cancelled
            if (pEvent.isCancelled()) {
                origin.teleport(player, origin, event);
                return;
            }
            // Update exit if needed
            exit = pEvent.getExit();
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

    public void teleport(final Vehicle vehicle) {
        Location traveller = new Location(this.world, vehicle.getLocation().getX(), vehicle.getLocation().getY(), vehicle.getLocation().getZ());
        Location exit = getExit(traveller);

        double velocity = vehicle.getVelocity().length();

        // Stop and teleport
        vehicle.setVelocity(new Vector());

        // Get new velocity
        final Vector newVelocity = new Vector(modX, 0.0F, modZ);
        newVelocity.multiply(velocity);

        List<Entity> passengers = vehicle.getPassengers();
        if (!passengers.isEmpty()) {
            final Vehicle v = exit.getWorld().spawn(exit, vehicle.getClass());
            final Entity passenger = passengers.get(0);
            vehicle.eject();
            vehicle.remove();
            passenger.eject();
            passenger.teleport(exit);
            stargate.getServer().getScheduler().scheduleSyncDelayedTask(stargate, () -> {
                v.addPassenger(passenger);
                v.setVelocity(newVelocity);
            }, 1);
        } else {
            Vehicle mc = Objects.requireNonNull(exit.getWorld()).spawn(exit, vehicle.getClass());
            if (mc instanceof StorageMinecart) {
                StorageMinecart smc = (StorageMinecart) mc;
                smc.getInventory().setContents(((StorageMinecart) vehicle).getInventory().getContents());
            }
            mc.setVelocity(newVelocity);
            vehicle.remove();
        }
    }

    private Location getExit(Location traveller) {
        Location loc = null;
        // Check if the gate has an exit block
        if (gate.getExit() != null) {
            Blox exit = getBlockAt(gate.getExit());
            int back = (isBackwards()) ? -1 : 1;
            loc = exit.modRelativeLoc(0D, 0D, 1D, traveller.getYaw(), traveller.getPitch(), modX * back, 1, modZ * back);
        } else {
            stargate.getStargateLogger().log(Level.WARNING, "[Stargate] Missing destination point in .gate file " + gate.getFilename());
        }

        if (loc != null) {
            BlockData bd = getWorld().getBlockAt(loc).getBlockData();
            if (bd instanceof Bisected && ((Bisected) bd).getHalf() == Bisected.Half.BOTTOM) {
                loc.add(0, 0.5, 0);
            }

            loc.setPitch(traveller.getPitch());
            return loc;
        }
        return traveller;
    }

    private boolean isVerified() {
        verified = true;

        if (!stargate.isVerifyPortals()) {
            return true;
        }

        for (RelativeBlockVector control : gate.getControls()) {
            verified = verified && getBlockAt(control).getBlock().getType().equals(gate.getControlBlock());
        }

        return verified;
    }

    private boolean wasVerified() {
        return !stargate.isVerifyPortals() || verified;
    }

    private boolean checkIntegrity() {
        return !stargate.isVerifyPortals() || gate.matches(topLeft, modX, modZ);
    }

    private List<String> getDestinations(Player player, String network) {
        for (String dest : allPortalsNet.get(network.toLowerCase())) {
            Portal portal = getByName(dest, network);
            if (portal == null) continue;

            // Check if dest is a random gate
            if (portal.isRandom()) continue;

            // Check if dest is always open (Don't show if so)
            if (portal.isAlwaysOn() && !portal.isShown()) continue;

            // Check if dest is this portal
            if (dest.equalsIgnoreCase(getName())) continue;

            // Check if dest is a fixed gate not pointing to this gate
            // Removed per issue 7
            //if (portal.isFixed() && !portal.getDestinationName().equalsIgnoreCase(getName())) continue;

            // Allow random use by non-players (Minecarts)
            if (player == null) {
                destinations.add(portal.getName());
                continue;
            }

            // Check if this player can access the dest world
            if (!PermissionManager.canAccessWorld(player, portal.getWorld().getName())) continue;

            // Visible to this player.
            if (stargate.canSee(player, portal)) {
                destinations.add(portal.getName());
            }
        }

        return destinations;
    }

    private ArrayList<String> getSignLines() {
        ArrayList<String> lines = new ArrayList<>();

        int max = destinations.size() - 1;

        if (!isActive()) {
            lines.add(stargate.getString("signRightClick"));
            lines.add(stargate.getString("signToUse"));

            if (!noNetwork) {
                lines.add("(" + network + ")");
            }

            return lines;
        }

        if (isBungee()) {
            lines.add(stargate.getString("bungeeSign"));
            lines.add(">" + destination + "<");
            lines.add("[" + network + "]");

            return lines;
        }

        if (isFixed()) {
            String str = isRandom() ? stargate.getString("signRandom") : destination;

            lines.add("> " + str + " <");
            lines.add(noNetwork ? "" : "(" + network + ")");

            Portal dest = Portal.getByName(destination, network);
            lines.add(dest == null && !isRandom() ? stargate.getString("signDisconnected") : "");

            return lines;
        }

        int curIndex = destinations.indexOf(destination);
        int i = Math.min(max, Math.max(0, curIndex - (curIndex == max ? 2 : 1)));

        while (lines.size() < 3 && i <= max) {
            String drawDestination = destinations.get(i);
            String msg = drawDestination;

            if (stargate.getEconomyHandler().useEconomy() && stargate.getEconomyHandler().isFreeGatesGreen()) {
                Portal destPortal = Portal.getByName(drawDestination, network);
                boolean isFree = stargate.isFree(activePlayer, this, destPortal);

                msg = (isFree ? ChatColor.DARK_GREEN : "") + msg + ChatColor.RESET;
            }

            if (i == curIndex)
                msg = " >" + msg + "< ";

            lines.add(msg);
            i++;
        }

        return lines;
    }

    private boolean activate(Player player) {
        destinations.clear();
        destination = "";
        activePlayer = player;

        stargate.getActiveList().add(this);

        String network = getNetwork();
        destinations = getDestinations(player, network);

        if (stargate.isSortLists()) {
            Collections.sort(destinations);
        }

        if (stargate.isDestMemory() && !lastDest.isEmpty() && destinations.contains(lastDest)) {
            destination = lastDest;
        }

        StargateActivateEvent event = new StargateActivateEvent(this, player, destinations, destination);
        stargate.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            stargate.getActiveList().remove(this);
            return false;
        }

        destination = event.getDestination();
        destinations = event.getDestinations();
        drawSign();

        return true;
    }

    public void deactivate() {
        StargateDeactivateEvent event = new StargateDeactivateEvent(this);
        stargate.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        stargate.getActiveList().remove(this);

        if (isFixed()) return;

        destinations.clear();
        destination = "";
        activePlayer = null;

        drawSign();
    }

    public boolean isActive() {
        return isFixed() || (destinations.size() > 0);
    }

    public void cycleDestination(Player player, int dir) {
        boolean activate = false;

        if (!isActive() || getActivePlayer() != player) {
            // If the event is cancelled, return
            if (!activate(player)) {
                return;
            }

            Stargate.debug("cycleDestination", "Network Size: " + allPortalsNet.get(network.toLowerCase()).size());
            Stargate.debug("cycleDestination", "Player has access to: " + destinations.size());

            activate = true;
        }

        if (destinations.isEmpty()) {
            stargate.sendMessage(player, stargate.getString("destEmpty"));
            return;
        }

        if (!stargate.isDestMemory() || !activate || lastDest.isEmpty()) {
            int index = destinations.indexOf(destination);
            index += dir;

            if (index >= destinations.size())
                index = 0;
            else if (index < 0)
                index = destinations.size() - 1;

            destination = destinations.get(index);
            lastDest = destination;
        }

        openTime = System.currentTimeMillis() / 1000;
        drawSign();
    }

    public final void drawSign() {
        BlockState state = id.getBlock().getState();

        if (!(state instanceof Sign)) {
            stargate.getStargateLogger().warning("[Stargate] Sign block is not a Sign object");
            Stargate.debug("Portal::drawSign", "Block: " + id.getBlock().getType() + " @ " + id.getBlock().getLocation());
            return;
        }

        Sign sign = (Sign) state;
        stargate.setLine(sign, 0, "-" + name + "-");

        ArrayList<String> lines = getSignLines();

        for (int i = 1; i < 4; i++) {
            String line = "";

            try {
                line = lines.get(i - 1);
            } catch (IndexOutOfBoundsException ignored) {
            }

            stargate.setLine(sign, i, line);
        }

        sign.update();
    }

    public void unregister(boolean removeAll) {
        Stargate.debug("Unregister", "Unregistering gate " + getName());
        close(true);

        for (Blox block : getFrame()) {
            lookupBlocks.remove(block);
        }

        lookupBlocks.remove(id);
        lookupControls.remove(id);

        if (button != null) {
            lookupBlocks.remove(button);
            lookupControls.remove(button);
        }

        for (Blox entrance : getEntrances()) {
            lookupEntrances.remove(entrance);
        }

        if (removeAll)
            allPortals.remove(this);

        if (bungee) {
            bungeePortals.remove(getName().toLowerCase());
        } else {
            String network = getNetwork().toLowerCase();
            String name = getName().toLowerCase();

            lookupNamesNet.get(network).remove(name);
            allPortalsNet.get(network).remove(name);

            for (String originName : allPortalsNet.get(network)) {
                Portal origin = Portal.getByName(originName, getNetwork());

                if (origin == null || !origin.getDestinationName().equalsIgnoreCase(getName()) || !origin.isVerified())
                    continue;

                if (origin.isFixed()) origin.drawSign();
                if (origin.isAlwaysOn()) origin.close(true);
            }
        }

        Block idBlock = id.getBlock();

        if (idBlock.getBlockData() instanceof WallSign) {
            Sign sign = (Sign) idBlock.getState();

            sign.setLine(0, getName());
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");

            sign.update();
        }

        saveAllGates(stargate, getWorld());
    }

    private Blox getBlockAt(RelativeBlockVector vector) {
        return topLeft.modRelative(vector.getRight(), vector.getDepth(), vector.getDistance(), modX, 1, modZ);
    }

    private void register() {
        fixed = destination.length() > 0 || random || bungee;

        // Bungee gates are stored in their own list
        if (isBungee()) {
            bungeePortals.put(getName().toLowerCase(), this);
        } else {
            // Check if network exists in our network list
            if (!lookupNamesNet.containsKey(getNetwork().toLowerCase())) {
            	Stargate.debug("register", "Network " + getNetwork() + " not in lookupNamesNet, adding");
                lookupNamesNet.put(getNetwork().toLowerCase(), new HashMap<>());
            }
            lookupNamesNet.get(getNetwork().toLowerCase()).put(getName().toLowerCase(), this);

            // Check if this network exists
            if (!allPortalsNet.containsKey(getNetwork().toLowerCase())) {
            	Stargate.debug("register", "Network " + getNetwork() + " not in allPortalsNet, adding");
                allPortalsNet.put(getNetwork().toLowerCase(), new ArrayList<>());
            }
            allPortalsNet.get(getNetwork().toLowerCase()).add(getName().toLowerCase());
        }

        for (Blox block : getFrame()) {
            lookupBlocks.put(block, this);
        }

        lookupBlocks.put(id, this);
        lookupControls.put(id, this);

        if (button != null) {
            lookupBlocks.put(button, this);
            lookupControls.put(button, this);
        }

        for (Blox entrance : getEntrances()) {
            lookupEntrances.put(entrance, this);
        }

        allPortals.add(this);
    }

    public static Portal createPortal(Stargate stargate, SignChangeEvent event, Player player) {
        Blox id = new Blox(event.getBlock());
        Block idParent = id.getParent();
        if (idParent == null) {
            return null;
        }

        if (Gate.getGatesByControlBlock(idParent).length == 0) return null;

        if (Portal.getByBlock(idParent) != null) {
        	Stargate.debug("createPortal", "idParent belongs to existing gate");
            return null;
        }

        Blox parent = new Blox(player.getWorld(), idParent.getX(), idParent.getY(), idParent.getZ());
        Blox topleft = null;

        String name = filterName(event.getLine(0));
        String destName = filterName(event.getLine(1));
        String network = filterName(event.getLine(2));
        String options = filterName(event.getLine(3)).toLowerCase();

        boolean hidden = (options.indexOf('h') != -1);
        boolean alwaysOn = (options.indexOf('a') != -1);
        boolean priv = (options.indexOf('p') != -1);
        boolean free = (options.indexOf('f') != -1);
        boolean backwards = (options.indexOf('b') != -1);
        boolean show = (options.indexOf('s') != -1);
        boolean noNetwork = (options.indexOf('n') != -1);
        boolean random = (options.indexOf('r') != -1);
        boolean bungee = (options.indexOf('u') != -1);

        // Check permissions for options.
        if (hidden && !stargate.canOption(player, "hidden")) hidden = false;
        if (alwaysOn && !stargate.canOption(player, "alwayson")) alwaysOn = false;
        if (priv && !stargate.canOption(player, "private")) priv = false;
        if (free && !stargate.canOption(player, "free")) free = false;
        if (backwards && !stargate.canOption(player, "backwards")) backwards = false;
        if (show && !stargate.canOption(player, "show")) show = false;
        if (noNetwork && !stargate.canOption(player, "nonetwork")) noNetwork = false;
        if (random && !stargate.canOption(player, "random")) random = false;

        // Can not create a non-fixed always-on gate.
        if (alwaysOn && destName.length() == 0) {
            alwaysOn = false;
        }

        // Show isn't useful if A is false
        if (show && !alwaysOn) {
            show = false;
        }

        // Random gates are always on and can't be shown
        if (random) {
            alwaysOn = true;
            show = false;
        }

        // Bungee gates are always on and don't support Random
        if (bungee) {
            alwaysOn = true;
            random = false;
        }

        // Moved the layout check so as to avoid invalid messages when not making a gate
        int modX = 0;
        int modZ = 0;
        float rotX = 0f;
        BlockFace buttonfacing = BlockFace.DOWN;

        if (idParent.getX() > id.getBlock().getX()) {
            modZ -= 1;
            rotX = 90f;
            buttonfacing = BlockFace.WEST;
        } else if (idParent.getX() < id.getBlock().getX()) {
            modZ += 1;
            rotX = 270f;
            buttonfacing = BlockFace.EAST;
        } else if (idParent.getZ() > id.getBlock().getZ()) {
            modX += 1;
            rotX = 180f;
            buttonfacing = BlockFace.NORTH;
        } else if (idParent.getZ() < id.getBlock().getZ()) {
            modX -= 1;
            rotX = 0f;
            buttonfacing = BlockFace.SOUTH;
        }
        Gate[] possibleGates = Gate.getGatesByControlBlock(idParent);
        Gate gate = null;
        RelativeBlockVector buttonVector = null;

        for (Gate possibility : possibleGates) {
            if (gate != null) {
                break;
            }

            RelativeBlockVector[] vectors = possibility.getControls();
            RelativeBlockVector otherControl = null;

            for (RelativeBlockVector vector : vectors) {
                Blox tl = parent.modRelative(-vector.getRight(), -vector.getDepth(), -vector.getDistance(), modX, 1, modZ);

                if (gate == null) {
                    if (possibility.matches(tl, modX, modZ, true)) {
                        gate = possibility;
                        topleft = tl;

                        if (otherControl != null) {
                            buttonVector = otherControl;
                        }
                    }
                } else {
                    buttonVector = vector;
                }

                otherControl = vector;
            }
        }
        if (gate == null || buttonVector == null) {
        	Stargate.debug("createPortal", "Could not find matching gate layout");
            return null;
        }

        // If the player is trying to create a Bungee gate without permissions, drop out here
        // Do this after the gate layout check, in the least
        if (bungee) {
            if (!stargate.isEnableBungee()) {
                stargate.sendMessage(player, stargate.getString("bungeeDisabled"));
                return null;
            } else if (!PermissionManager.hasPerm(player, "stargate.admin.bungee")) {
                stargate.sendMessage(player, stargate.getString("bungeeDeny"));
                return null;
            } else if (destName.isEmpty() || network.isEmpty()) {
                stargate.sendMessage(player, stargate.getString("bungeeEmpty"));
                return null;
            }
        }

        // Debug
        Stargate.debug("createPortal", "h = " + hidden + " a = " + alwaysOn + " p = " + priv + " f = " + free + " b = " + backwards + " s = " + show + " n = " + noNetwork + " r = " + random + " u = " + bungee);

        if (!bungee && (network.length() < 1 || network.length() > 11)) {
            network = stargate.getDefaultNetwork();
        }

        boolean deny = false;
        String denyMsg = "";

        // Check if the player can create gates on this network
        if (!bungee && !stargate.canCreate(player, network)) {
        	Stargate.debug("createPortal", "Player doesn't have create permissions on network. Trying personal");

            if (stargate.canCreatePersonal(player)) {
                network = player.getName();

                // TODO ; this substring can potentially cause two players to share personal networks
                if (network.length() > 11) network = network.substring(0, 11);

                Stargate.debug("createPortal", "Creating personal portal");
                stargate.sendMessage(player, stargate.getString("createPersonal"));
            } else {
            	Stargate.debug("createPortal", "Player does not have access to network");

                deny = true;
                denyMsg = stargate.getString("createNetDeny");
            }
        }
        // Check if the player can create this gate layout
        String gateName = gate.getFilename();
        gateName = gateName.substring(0, gateName.indexOf('.'));

        if (!deny && !stargate.canCreateGate(player, gateName)) {
        	Stargate.debug("createPortal", "Player does not have access to gate layout");

            deny = true;
            denyMsg = stargate.getString("createGateDeny");
        }

        // Check if the user can create gates to this world.
        // Todo ; To? in? Both?
        if (!bungee && !deny && destName.length() > 0) {
            Portal p = Portal.getByName(destName, network);

            if (p != null) {
                String world = p.getWorld().getName();

                if (!PermissionManager.canAccessWorld(player, world)) {
                	Stargate.debug("canCreate", "Player does not have access to destination world");

                    deny = true;
                    denyMsg = stargate.getString("createWorldDeny");
                }
            }
        }

        // Bleh, gotta check to make sure none of this gate belongs to another gate. Boo slow.
        for (RelativeBlockVector v : gate.getBorder()) {
            Blox b = topleft.modRelative(v.getRight(), v.getDepth(), v.getDistance(), modX, 1, modZ);

            if (Portal.getByBlock(b.getBlock()) != null) {
            	Stargate.debug("createPortal", "Gate conflicts with existing gate");
                stargate.sendMessage(player, stargate.getString("createConflict"));
                return null;
            }
        }

        Blox button = null;
        Portal portal;
        portal = new Portal(stargate, topleft, modX, modZ, rotX, id, button, destName, name, false, network, gate, player.getUniqueId(), player.getName(), hidden, alwaysOn, priv, free, backwards, show, noNetwork, random, bungee);

        int cost = stargate.getCreateCost(player, gate);

        // Call StargateCreateEvent
        StargateCreateEvent cEvent = new StargateCreateEvent(player, portal, event.getLines(), deny, denyMsg, cost);
        stargate.getServer().getPluginManager().callEvent(cEvent);

        if (cEvent.isCancelled()) {
            return null;
        }

        if (cEvent.getDeny()) {
            stargate.sendMessage(player, cEvent.getDenyReason());
            return null;
        }

        cost = cEvent.getCost();

        // Name & Network can be changed in the event, so do these checks here.
        if (portal.getName().length() < 1 || portal.getName().length() > 11) {
        	Stargate.debug("createPortal", "Name length error");
            stargate.sendMessage(player, stargate.getString("createNameLength"));
            return null;
        }

        // Don't do network checks for bungee gates
        if (portal.isBungee()) {
            if (bungeePortals.get(portal.getName().toLowerCase()) != null) {
            	Stargate.debug("createPortal::Bungee", "Gate Exists");
                stargate.sendMessage(player, stargate.getString("createExists"));
                return null;
            }
        } else {
            if (getByName(portal.getName(), portal.getNetwork()) != null) {
            	Stargate.debug("createPortal", "Name Error");
                stargate.sendMessage(player, stargate.getString("createExists"));
                return null;
            }

            // Check if there are too many gates in this network
            ArrayList<String> netList = allPortalsNet.get(portal.getNetwork().toLowerCase());
            if (stargate.getMaxGates() > 0 && netList != null && netList.size() >= stargate.getMaxGates()) {
                stargate.sendMessage(player, stargate.getString("createFull"));
                return null;
            }
        }

        if (cost > 0) {
            if (!stargate.chargePlayer(player, cost)) {
                String inFundMsg = stargate.getString("ecoInFunds");
                inFundMsg = stargate.replaceVars(inFundMsg, new String[]{"%cost%", "%portal%"}, new String[]{stargate.getEconomyHandler().format(cost), name});

                stargate.sendMessage(player, inFundMsg);
                Stargate.debug("createPortal", "Insufficient Funds");

                return null;
            }

            String deductMsg = stargate.getString("ecoDeduct");
            deductMsg = stargate.replaceVars(deductMsg, new String[]{"%cost%", "%portal%"}, new String[]{stargate.getEconomyHandler().format(cost), name});
            stargate.sendMessage(player, deductMsg, false);
        }

        // No button on an always-open gate.
        if (!alwaysOn) {
            Material buttonMat = Material.STONE_BUTTON;

            if (gate.getPortalBlockClosed() == Material.WATER) {
                buttonMat = Material.DEAD_TUBE_CORAL_WALL_FAN;
            }
            
            
            button = topleft.modRelative(buttonVector.getRight(), buttonVector.getDepth(), buttonVector.getDistance() + 1, modX, 1, modZ);
            
            
            //generates a Blockdata
            Directional buttonData = (Directional) Bukkit.createBlockData(buttonMat);
            //manipulate the data
            buttonData.setFacing(buttonfacing);
            
            //Sets the blockdata into the world
            button.getBlock().setBlockData(buttonData);

            portal.setButton(button);
        }

        portal.register();
        portal.drawSign();

        // Open always on gate
        if (portal.isRandom() || portal.isBungee()) {
            portal.open(true);
        } else if (portal.isAlwaysOn()) {
            Portal dest = Portal.getByName(destName, portal.getNetwork());

            if (dest != null) {
                portal.open(true);
                dest.drawSign();
            }
        } else {
            // Set the inside of the gate to its closed material
            for (Blox inside : portal.getEntrances()) {
                inside.setType(portal.getGate().getPortalBlockClosed());
            }
        }

        // Don't do network stuff for bungee gates
        if (!portal.isBungee()) {
            // Open any always on gate pointing at this gate
            for (String originName : allPortalsNet.get(portal.getNetwork().toLowerCase())) {
                Portal origin = Portal.getByName(originName, portal.getNetwork());

                if (origin == null || !origin.getDestinationName().equalsIgnoreCase(portal.getName()) || !origin.isVerified())
                    continue;

                if (origin.isFixed()) origin.drawSign();
                if (origin.isAlwaysOn()) origin.open(true);
            }
        }

        saveAllGates(stargate, portal.getWorld());

        return portal;
    }

    private static Portal getByName(String name, String network) {
        if (!lookupNamesNet.containsKey(network.toLowerCase())) return null;
        return lookupNamesNet.get(network.toLowerCase()).get(name.toLowerCase());
    }

    public static Portal getByEntrance(Location location) {
        return lookupEntrances.get(new Blox(location));
    }

    public static Portal getByEntrance(Block block) {
        return lookupEntrances.get(new Blox(block));
    }

    public static Portal getByAdjacentEntrance(Location loc) {
        int centerX = loc.getBlockX();
        int centerY = loc.getBlockY();
        int centerZ = loc.getBlockZ();
        World world = loc.getWorld();

        int i = 0;

        Portal portal = null;

        while (portal == null && i < 5) {
            int xMod = 0;
            int zMod = 0;

            switch (i) {
                case 1:
                    xMod = 1;
                    break;
                case 2:
                    xMod = -1;
                    break;
                case 3:
                    zMod = 1;
                    break;
                case 4:
                    zMod = -1;
                    break;
            }

            portal = lookupEntrances.get(new Blox(world, centerX + xMod, centerY, centerZ + zMod));

            i++;
        }

        return portal;
    }

    public static Portal getByControl(Block block) {
        return lookupControls.get(new Blox(block));
    }

    public static Portal getByBlock(Block block) {
        return lookupBlocks.get(new Blox(block));
    }

    public static Portal getBungeeGate(String name) {
        return bungeePortals.get(name.toLowerCase());
    }

    // TODO ; And I thought that Stargates was good for using Databases...
    private static void saveAllGates(Stargate stargate, World world) {
        stargate.getManagedWorlds().add(world.getName());
        String loc = stargate.getSaveLocation() + "/" + world.getName() + ".db";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(loc, false));

            for (Portal portal : allPortals) {
                String wName = portal.world.getName();
                if (!wName.equalsIgnoreCase(world.getName())) continue;
                StringBuilder builder = new StringBuilder();
                Blox button = portal.button;

                builder.append(portal.name);
                builder.append(':');
                builder.append(portal.id.toString());
                builder.append(':');
                builder.append((button != null) ? button.toString() : "");
                builder.append(':');
                builder.append(portal.modX);
                builder.append(':');
                builder.append(portal.modZ);
                builder.append(':');
                builder.append(portal.rotX);
                builder.append(':');
                builder.append(portal.topLeft.toString());
                builder.append(':');
                builder.append(portal.gate.getFilename());
                builder.append(':');
                builder.append(portal.isFixed() ? portal.getDestinationName() : "");
                builder.append(':');
                builder.append(portal.getNetwork());
                builder.append(':');

                UUID owner = portal.getOwnerUUID();
                if (owner != null) {
                    builder.append(portal.getOwnerUUID().toString());
                } else {
                    builder.append(portal.getOwnerName());
                }

                builder.append(':');
                builder.append(portal.isHidden());
                builder.append(':');
                builder.append(portal.isAlwaysOn());
                builder.append(':');
                builder.append(portal.isPrivate());
                builder.append(':');
                builder.append(portal.world.getName());
                builder.append(':');
                builder.append(portal.isFree());
                builder.append(':');
                builder.append(portal.isBackwards());
                builder.append(':');
                builder.append(portal.isShown());
                builder.append(':');
                builder.append(portal.isNoNetwork());
                builder.append(':');
                builder.append(portal.isRandom());
                builder.append(':');
                builder.append(portal.isBungee());

                bw.append(builder.toString());
                bw.newLine();
            }

            bw.close();
        } catch (Exception e) {
            stargate.getStargateLogger().log(Level.SEVERE, "Exception while writing stargates to " + loc + ": " + e);
        }
    }

    public static void clearGates() {
        lookupBlocks.clear();
        lookupNamesNet.clear();
        lookupEntrances.clear();
        lookupControls.clear();
        allPortals.clear();
        allPortalsNet.clear();
    }

    // TODO ; fucking gross
    public static boolean loadAllGates(Stargate stargate, World world) {
        String location = stargate.getSaveLocation();

        File db = new File(location, world.getName() + ".db");

        if (db.exists()) {
            int l = 0;
            int portalCount = 0;
            try {
                Scanner scanner = new Scanner(db);
                while (scanner.hasNextLine()) {
                    l++;
                    String line = scanner.nextLine().trim();
                    if (line.startsWith("#") || line.isEmpty()) {
                        continue;
                    }
                    String[] split = line.split(":");
                    if (split.length < 8) {
                        stargate.getStargateLogger().info("[Stargate] Invalid line - " + l);
                        continue;
                    }
                    String name = split[0];
                    Blox sign = new Blox(world, split[1]);
                    Blox button = (split[2].length() > 0) ? new Blox(world, split[2]) : null;
                    int modX = Integer.parseInt(split[3]);
                    int modZ = Integer.parseInt(split[4]);
                    float rotX = Float.parseFloat(split[5]);
                    Blox topLeft = new Blox(world, split[6]);
                    Gate gate = Gate.getGateByName(split[7]);
                    if (gate == null) {
                        stargate.getStargateLogger().info("[Stargate] Gate layout on line " + l + " does not exist [" + split[7] + "]");
                        continue;
                    }

                    String dest = (split.length > 8) ? split[8] : "";
                    String network = (split.length > 9) ? split[9] : stargate.getDefaultNetwork();
                    if (network.isEmpty()) {
                        network = stargate.getDefaultNetwork();
                    }
                    String ownerString = (split.length > 10) ? split[10] : "";
                    boolean hidden = (split.length > 11) && split[11].equalsIgnoreCase("true");
                    boolean alwaysOn = (split.length > 12) && split[12].equalsIgnoreCase("true");
                    boolean priv = (split.length > 13) && split[13].equalsIgnoreCase("true");
                    boolean free = (split.length > 15) && split[15].equalsIgnoreCase("true");
                    boolean backwards = (split.length > 16) && split[16].equalsIgnoreCase("true");
                    boolean show = (split.length > 17) && split[17].equalsIgnoreCase("true");
                    boolean noNetwork = (split.length > 18) && split[18].equalsIgnoreCase("true");
                    boolean random = (split.length > 19) && split[19].equalsIgnoreCase("true");
                    boolean bungee = (split.length > 20) && split[20].equalsIgnoreCase("true");

                    // Attempt to get owner as UUID
                    UUID ownerUUID = null;
                    String ownerName;
                    if (ownerString.length() > 16) {
                        try {
                            ownerUUID = UUID.fromString(ownerString);
                            OfflinePlayer offlineOwner = Bukkit.getServer().getOfflinePlayer(ownerUUID);
                            ownerName = offlineOwner.getName();
                        } catch (IllegalArgumentException ex) {
                            // neither name nor UUID, so keep it as-is
                            ownerName = ownerString;
                            Stargate.debug("loadAllGates", "Invalid Stargate owner string: " + ownerString);
                        }
                    } else {
                        ownerName = ownerString;
                    }

                    Portal portal = new Portal(stargate, topLeft, modX, modZ, rotX, sign, button, dest, name, false, network, gate, ownerUUID, ownerName, hidden, alwaysOn, priv, free, backwards, show, noNetwork, random, bungee);
                    portal.register();
                    portal.close(true);
                }
                scanner.close();

                // Open any always-on gates. Do this here as it should be more efficient than in the loop.
                int OpenCount = 0;
                for (Iterator<Portal> iter = allPortals.iterator(); iter.hasNext(); ) {
                    Portal portal = iter.next();
                    if (portal == null) continue;

                    // Verify portal integrity/register portal
                    if (!portal.wasVerified()) {
                        if (!portal.isVerified() || !portal.checkIntegrity()) {
                            // DEBUG
                            for (RelativeBlockVector control : portal.getGate().getControls()) {
                                if (!portal.getBlockAt(control).getBlock().getType().equals(portal.getGate().getControlBlock())) {
                                	Stargate.debug("loadAllGates", "Control Block Type == " + portal.getBlockAt(control).getBlock().getType().name());
                                }
                            }
                            portal.unregister(false);
                            iter.remove();
                            stargate.getStargateLogger().info("[Stargate] Destroying stargate at " + portal.toString());
                            continue;
                        }
                    }

                    portalCount++;

                    if (portal.isFixed() && (stargate.isEnableBungee() && portal.isBungee()
                            || portal.getDestination() != null && portal.isAlwaysOn())) {
                        portal.open(true);
                        OpenCount++;
                    }
                }
                stargate.getStargateLogger().info("[Stargate] {" + world.getName() + "} Loaded " + portalCount + " stargates with " + OpenCount + " set as always-on");
                return true;
            } catch (Exception e) {
                stargate.getStargateLogger().log(Level.SEVERE, "Exception while reading stargates from " + db.getName() + ": " + l);
                e.printStackTrace();
            }
        } else {
            stargate.getStargateLogger().info("[Stargate] {" + world.getName() + "} No stargates for world ");
        }

        return false;
    }

    public static void closeAllGates(Stargate stargate) {
        stargate.getStargateLogger().info("Closing all stargates.");

        for (Portal p : allPortals) {
            if (p == null) continue;
            p.close(true);
        }
    }

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
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Portal other = (Portal) obj;

        if ((name == null && other.name != null) || (name != null && !name.equalsIgnoreCase(other.name)))
            return false;

        if (network == null) return other.network == null;
        else return network.equalsIgnoreCase(other.network);
    }
}
