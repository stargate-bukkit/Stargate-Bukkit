package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.container.TwoTuple;
import net.knarcraft.stargate.event.StargateCreateEvent;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EconomyHandler;
import net.knarcraft.stargate.utility.EconomyHelper;
import net.knarcraft.stargate.utility.PermissionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.util.Vector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Keeps track of all loaded portals, and handles portal creation
 */
public class PortalHandler {
    // Static variables used to store portal lists
    private static final Map<BlockLocation, Portal> lookupBlocks = new HashMap<>();
    private static final Map<BlockLocation, Portal> lookupEntrances = new HashMap<>();
    private static final Map<BlockLocation, Portal> lookupControls = new HashMap<>();
    private static final List<Portal> allPortals = new ArrayList<>();
    private static final HashMap<String, List<String>> allPortalNetworks = new HashMap<>();
    private static final HashMap<String, HashMap<String, Portal>> portalLookupByNetwork = new HashMap<>();

    // A list of Bungee gates
    private static final Map<String, Portal> bungeePortals = new HashMap<>();

    private PortalHandler() {

    }

    public static List<String> getNetwork(String network) {
        return allPortalNetworks.get(network.toLowerCase());
    }

    /**
     * Gets all destinations in the network viewable by the given player
     *
     * @param entrancePortal <p>The portal the user is entering from</p>
     * @param player         <p>The player who wants to see destinations</p>
     * @param network        <p>The network to get destinations from</p>
     * @return <p>All destinations the player can go to</p>
     */
    public static List<String> getDestinations(Portal entrancePortal, Player player, String network) {
        List<String> destinations = new ArrayList<>();
        for (String destination : allPortalNetworks.get(network.toLowerCase())) {
            Portal portal = getByName(destination, network);
            if (portal == null) {
                continue;
            }
            // Check if destination is a random gate
            if (portal.isRandom()) {
                continue;
            }
            // Check if destination is always open (Don't show if so)
            if (portal.isAlwaysOn() && !portal.isShown()) {
                continue;
            }
            // Check if destination is this portal
            if (destination.equalsIgnoreCase(entrancePortal.getName())) {
                continue;
            }
            // Check if destination is a fixed gate not pointing to this gate
            if (portal.isFixed() && !portal.getDestinationName().equalsIgnoreCase(entrancePortal.getName())) {
                continue;
            }
            // Allow random use by non-players (Minecarts)
            if (player == null) {
                destinations.add(portal.getName());
                continue;
            }
            // Check if this player can access the dest world
            if (PermissionHelper.cannotAccessWorld(player, portal.getWorld().getName())) {
                Stargate.logger.info("cannot access world");
                continue;
            }
            // Visible to this player.
            if (PermissionHelper.canSeePortal(player, portal)) {
                destinations.add(portal.getName());
            }
        }
        return destinations;
    }

    /**
     * Un-registers the given portal
     *
     * @param portal    <p>The portal to un-register</p>
     * @param removeAll <p>Whether to remove the portal from the list of all portals</p>
     */
    public static void unregisterPortal(Portal portal, boolean removeAll) {
        Stargate.debug("Unregister", "Unregistering gate " + portal.getName());
        portal.close(true);

        String portalName = portal.getName().toLowerCase();
        String networkName = portal.getNetwork().toLowerCase();

        //Remove portal from lookup blocks
        for (BlockLocation block : portal.getFrame()) {
            lookupBlocks.remove(block);
        }

        //Remove registered info about the lookup controls and blocks
        lookupBlocks.remove(portal.getId());
        lookupControls.remove(portal.getId());
        if (portal.getButton() != null) {
            lookupBlocks.remove(portal.getButton());
            lookupControls.remove(portal.getButton());
        }

        //Remove entrances
        for (BlockLocation entrance : portal.getEntrances()) {
            lookupEntrances.remove(entrance);
        }

        //Remove the portal from the list of all portals
        if (removeAll) {
            allPortals.remove(portal);
        }

        if (portal.isBungee()) {
            //Remove the bungee listing
            bungeePortals.remove(portalName);
        } else {
            //Remove from network lists
            portalLookupByNetwork.get(networkName).remove(portalName);
            allPortalNetworks.get(networkName).remove(portalName);

            //Update all portals in the same network with this portal as its destination
            for (String originName : allPortalNetworks.get(networkName)) {
                Portal origin = getByName(originName, portal.getNetwork());
                if (origin == null || !origin.getDestinationName().equalsIgnoreCase(portalName) || !origin.isVerified()) {
                    continue;
                }
                //Update the portal's sign
                if (origin.isFixed()) {
                    origin.drawSign();
                }
                //Close portal without destination
                if (origin.isAlwaysOn()) {
                    origin.close(true);
                }
            }
        }

        //Clear sign data
        if (portal.getId().getBlock().getBlockData() instanceof WallSign) {
            Sign sign = (Sign) portal.getId().getBlock().getState();
            sign.setLine(0, portal.getName());
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");
            sign.update();
        }

        saveAllGates(portal.getWorld());
    }

    /**
     * Registers a portal
     *
     * @param portal <p>The portal to register</p>
     */
    private static void registerPortal(Portal portal) {
        portal.setFixed(portal.getDestinationName().length() > 0 || portal.isRandom() || portal.isBungee());

        String portalName = portal.getName().toLowerCase();
        String networkName = portal.getNetwork().toLowerCase();

        // Bungee gates are stored in their own list
        if (portal.isBungee()) {
            bungeePortals.put(portalName, portal);
        } else {
            //Check if network exists in the lookup list. If not, register the new network
            if (!portalLookupByNetwork.containsKey(networkName)) {
                Stargate.debug("register", "Network " + portal.getNetwork() + " not in lookupNamesNet, adding");
                portalLookupByNetwork.put(networkName, new HashMap<>());
            }
            //Check if this network exists in the network list. If not, register the network
            if (!allPortalNetworks.containsKey(networkName)) {
                Stargate.debug("register", "Network " + portal.getNetwork() + " not in allPortalsNet, adding");
                allPortalNetworks.put(networkName, new ArrayList<>());
            }

            //Register the portal
            portalLookupByNetwork.get(networkName).put(portalName, portal);
            allPortalNetworks.get(networkName).add(portalName);
        }

        //Register all frame blocks to the lookup list
        for (BlockLocation block : portal.getFrame()) {
            lookupBlocks.put(block, portal);
        }
        //Register the sign and button to the lookup lists
        lookupBlocks.put(portal.getId(), portal);
        lookupControls.put(portal.getId(), portal);
        if (portal.getButton() != null) {
            lookupBlocks.put(portal.getButton(), portal);
            lookupControls.put(portal.getButton(), portal);
        }

        //Register entrances to the lookup list
        for (BlockLocation entrance : portal.getEntrances()) {
            lookupEntrances.put(entrance, portal);
        }

        allPortals.add(portal);
    }

    /**
     * Creates a new portal
     *
     * @param event  <p>The sign change event which initialized the creation</p>
     * @param player <p>The player who's creating the portal</p>
     * @return <p>The created portal</p>
     */
    public static Portal createPortal(SignChangeEvent event, Player player) {
        BlockLocation id = new BlockLocation(event.getBlock());
        Block idParent = id.getParent();
        if (idParent == null) {
            return null;
        }

        if (GateHandler.getGatesByControlBlock(idParent).length == 0) {
            return null;
        }

        if (getByBlock(idParent) != null) {
            Stargate.debug("createPortal", "idParent belongs to existing gate");
            return null;
        }

        //Get necessary information from the gate's sign
        BlockLocation parent = new BlockLocation(player.getWorld(), idParent.getX(), idParent.getY(), idParent.getZ());
        BlockLocation topLeft = null;
        String name = filterName(event.getLine(0));
        String destinationName = filterName(event.getLine(1));
        String network = filterName(event.getLine(2));
        String options = filterName(event.getLine(3)).toLowerCase();

        //Get portal options available to the player creating the portal
        Map<PortalOption, Boolean> portalOptions = getPortalOptions(player, destinationName, options);

        //Get the yaw
        float yaw = DirectionHelper.getYawFromLocationDifference(idParent.getLocation(), id.getLocation());

        //Get the direction the button should be facing
        BlockFace buttonFacing = DirectionHelper.getBlockFaceFromYaw(yaw);

        //Get the x and z modifiers
        Vector direction = DirectionHelper.getDirectionVectorFromYaw(yaw);
        int modX = -direction.getBlockZ();
        int modZ = direction.getBlockX();

        //Get all gates with the used type of control blocks
        Gate[] possibleGates = GateHandler.getGatesByControlBlock(idParent);
        Gate gate = null;
        RelativeBlockVector buttonVector = null;
        RelativeBlockVector signVector = null;

        //Try to find a matching gate configuration
        for (Gate possibleGate : possibleGates) {
            //Get gate controls
            RelativeBlockVector[] vectors = possibleGate.getLayout().getControls();

            for (RelativeBlockVector controlVector : vectors) {
                //Assuming the top-left location is pointing to the gate's top-left location, check if it's a match
                BlockLocation possibleTopLocation = parent.modRelative(-controlVector.getRight(),
                        -controlVector.getDepth(), -controlVector.getDistance(), modX, 1, modZ);
                if (possibleGate.matches(possibleTopLocation, modX, modZ, true)) {
                    gate = possibleGate;
                    topLeft = possibleTopLocation;
                    signVector = controlVector;
                    break;
                }
            }
        }

        //Find the button position if a match was found
        if (gate != null) {
            RelativeBlockVector[] vectors = gate.getLayout().getControls();
            for (RelativeBlockVector controlVector : vectors) {
                if (!controlVector.equals(signVector)) {
                    buttonVector = controlVector;
                    break;
                }
            }
        }

        if ((gate == null) || (buttonVector == null)) {
            Stargate.debug("createPortal", "Could not find matching gate layout");
            return null;
        }

        //If the player is trying to create a Bungee gate without permissions, drop out here
        if (options.indexOf(PortalOption.BUNGEE.getCharacterRepresentation()) != -1) {
            if (!PermissionHelper.hasPermission(player, "stargate.admin.bungee")) {
                Stargate.sendErrorMessage(player, Stargate.getString("bungeeDeny"));
                return null;
            }
        }
        if (portalOptions.get(PortalOption.BUNGEE)) {
            if (!Stargate.enableBungee) {
                Stargate.sendErrorMessage(player, Stargate.getString("bungeeDisabled"));
                return null;
            } else if (destinationName.isEmpty() || network.isEmpty()) {
                Stargate.sendErrorMessage(player, Stargate.getString("bungeeEmpty"));
                return null;
            }
        }

        // Debug
        StringBuilder builder = new StringBuilder();
        for (PortalOption option : portalOptions.keySet()) {
            builder.append(option.getCharacterRepresentation()).append(" = ").append(portalOptions.get(option)).append(" ");
        }
        Stargate.debug("createPortal", builder.toString());

        if (!portalOptions.get(PortalOption.BUNGEE) && (network.length() < 1 || network.length() > 11)) {
            network = Stargate.getDefaultNetwork();
        }

        boolean deny = false;
        String denyMsg = "";

        // Check if the player can create gates on this network
        if (!portalOptions.get(PortalOption.BUNGEE) && !PermissionHelper.canCreateNetworkGate(player, network)) {
            Stargate.debug("createPortal", "Player doesn't have create permissions on network. Trying personal");
            if (PermissionHelper.canCreatePersonalGate(player)) {
                network = player.getName();
                if (network.length() > 11) network = network.substring(0, 11);
                Stargate.debug("createPortal", "Creating personal portal");
                Stargate.sendErrorMessage(player, Stargate.getString("createPersonal"));
            } else {
                Stargate.debug("createPortal", "Player does not have access to network");
                deny = true;
                denyMsg = Stargate.getString("createNetDeny");
                //return null;
            }
        }

        // Check if the player can create this gate layout
        String gateName = gate.getFilename();
        gateName = gateName.substring(0, gateName.indexOf('.'));
        if (!deny && !PermissionHelper.canCreateGate(player, gateName)) {
            Stargate.debug("createPortal", "Player does not have access to gate layout");
            deny = true;
            denyMsg = Stargate.getString("createGateDeny");
        }

        // Check if the user can create gates to this world.
        if (!portalOptions.get(PortalOption.BUNGEE) && !deny && destinationName.length() > 0) {
            Portal p = getByName(destinationName, network);
            if (p != null) {
                String world = p.getWorld().getName();
                if (PermissionHelper.cannotAccessWorld(player, world)) {
                    Stargate.debug("canCreateNetworkGate", "Player does not have access to destination world");
                    deny = true;
                    denyMsg = Stargate.getString("createWorldDeny");
                }
            }
        }

        // Bleh, gotta check to make sure none of this gate belongs to another gate. Boo slow.
        for (RelativeBlockVector v : gate.getLayout().getBorder()) {
            BlockLocation b = topLeft.modRelative(v.getRight(), v.getDepth(), v.getDistance(), modX, 1, modZ);
            if (getByBlock(b.getBlock()) != null) {
                Stargate.debug("createPortal", "Gate conflicts with existing gate");
                Stargate.sendErrorMessage(player, Stargate.getString("createConflict"));
                return null;
            }
        }

        BlockLocation button = null;
        Portal portal;
        portal = new Portal(topLeft, modX, modZ, yaw, id, button, destinationName, name, false, network,
                gate, player.getUniqueId(), player.getName(), portalOptions);

        int cost = EconomyHandler.getCreateCost(player, gate);

        // Call StargateCreateEvent
        StargateCreateEvent cEvent = new StargateCreateEvent(player, portal, event.getLines(), deny, denyMsg, cost);
        Stargate.server.getPluginManager().callEvent(cEvent);
        if (cEvent.isCancelled()) {
            return null;
        }
        if (cEvent.getDeny()) {
            Stargate.sendErrorMessage(player, cEvent.getDenyReason());
            return null;
        }

        cost = cEvent.getCost();

        // Name & Network can be changed in the event, so do these checks here.
        if (portal.getName().length() < 1 || portal.getName().length() > 11) {
            Stargate.debug("createPortal", "Name length error");
            Stargate.sendErrorMessage(player, Stargate.getString("createNameLength"));
            return null;
        }

        // Don't do network checks for bungee gates
        if (portal.isBungee()) {
            if (bungeePortals.get(portal.getName().toLowerCase()) != null) {
                Stargate.debug("createPortal::Bungee", "Gate Exists");
                Stargate.sendErrorMessage(player, Stargate.getString("createExists"));
                return null;
            }
        } else {
            if (getByName(portal.getName(), portal.getNetwork()) != null) {
                Stargate.debug("createPortal", "Name Error");
                Stargate.sendErrorMessage(player, Stargate.getString("createExists"));
                return null;
            }

            // Check if there are too many gates in this network
            List<String> netList = allPortalNetworks.get(portal.getNetwork().toLowerCase());
            if (Stargate.maxGatesEachNetwork > 0 && netList != null && netList.size() >= Stargate.maxGatesEachNetwork) {
                Stargate.sendErrorMessage(player, Stargate.getString("createFull"));
                return null;
            }
        }

        if (cost > 0) {
            if (!EconomyHandler.chargePlayerIfNecessary(player, cost)) {
                EconomyHelper.sendInsufficientFundsMessage(name, player, cost);
                Stargate.debug("createPortal", "Insufficient Funds");
                return null;
            }
            EconomyHelper.sendDeductMessage(name, player, cost);
        }

        // No button on an always-open gate.
        if (!portalOptions.get(PortalOption.ALWAYS_ON)) {
            button = topLeft.modRelative(buttonVector.getRight(), buttonVector.getDepth(), buttonVector.getDistance() + 1, modX, 1, modZ);
            Directional buttonData = (Directional) Bukkit.createBlockData(gate.getPortalButton());
            buttonData.setFacing(buttonFacing);
            button.getBlock().setBlockData(buttonData);
            portal.setButton(button);
        }

        registerPortal(portal);
        portal.drawSign();
        // Open always on gate
        if (portal.isRandom() || portal.isBungee()) {
            portal.open(true);
        } else if (portal.isAlwaysOn()) {
            Portal dest = getByName(destinationName, portal.getNetwork());
            if (dest != null) {
                portal.open(true);
                dest.drawSign();
            }
            // Set the inside of the gate to its closed material
        } else {
            for (BlockLocation inside : portal.getEntrances()) {
                inside.setType(portal.getGate().getPortalClosedBlock());
            }
        }

        // Don't do network stuff for bungee gates
        if (!portal.isBungee()) {
            // Open any always on gate pointing at this gate
            for (String originName : allPortalNetworks.get(portal.getNetwork().toLowerCase())) {
                Portal origin = getByName(originName, portal.getNetwork());
                if (origin == null) continue;
                if (!origin.getDestinationName().equalsIgnoreCase(portal.getName())) continue;
                if (!origin.isVerified()) continue;
                if (origin.isFixed()) origin.drawSign();
                if (origin.isAlwaysOn()) origin.open(true);
            }
        }

        saveAllGates(portal.getWorld());

        return portal;
    }

    /**
     * Gets all portal options to be applied to a new gate
     *
     * @param player          <p>The player creating the portal</p>
     * @param destinationName <p>The destination of the portal</p>
     * @param options         <p>The string on the option line of the sign</p>
     * @return <p>A map containing all portal options and their values</p>
     */
    private static Map<PortalOption, Boolean> getPortalOptions(Player player, String destinationName, String options) {
        Map<PortalOption, Boolean> portalOptions = new HashMap<>();
        for (PortalOption option : PortalOption.values()) {
            portalOptions.put(option, options.indexOf(option.getCharacterRepresentation()) != -1 &&
                    PermissionHelper.canUseOption(player, option));
        }

        // Can not create a non-fixed always-on gate.
        if (portalOptions.get(PortalOption.ALWAYS_ON) && destinationName.length() == 0) {
            portalOptions.put(PortalOption.ALWAYS_ON, false);
        }

        // Show isn't useful if always on is false
        if (portalOptions.get(PortalOption.SHOW) && !portalOptions.get(PortalOption.ALWAYS_ON)) {
            portalOptions.put(PortalOption.SHOW, false);
        }

        // Random gates are always on and can't be shown
        if (portalOptions.get(PortalOption.RANDOM)) {
            portalOptions.put(PortalOption.ALWAYS_ON, true);
            portalOptions.put(PortalOption.SHOW, false);
        }

        // Bungee gates are always on and don't support Random
        if (portalOptions.get(PortalOption.BUNGEE)) {
            portalOptions.put(PortalOption.ALWAYS_ON, true);
            portalOptions.put(PortalOption.RANDOM, false);
        }
        return portalOptions;
    }

    /**
     * Gets a portal given its name
     *
     * @param name    <p>The name of the portal</p>
     * @param network <p>The network the portal is connected to</p>
     * @return <p>The portal with the given name or null</p>
     */
    public static Portal getByName(String name, String network) {
        if (!portalLookupByNetwork.containsKey(network.toLowerCase())) {
            return null;
        }
        return portalLookupByNetwork.get(network.toLowerCase()).get(name.toLowerCase());

    }

    /**
     * Gets a portal given its entrance
     *
     * @param location <p>The location of the portal's entrance</p>
     * @return <p>The portal at the given location</p>
     */
    public static Portal getByEntrance(Location location) {
        return lookupEntrances.get(new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ()));
    }

    /**
     * Gets a portal given its entrance
     *
     * @param block <p>The block at the portal's entrance</p>
     * @return <p>The portal at the given block's location</p>
     */
    public static Portal getByEntrance(Block block) {
        return lookupEntrances.get(new BlockLocation(block));
    }

    /**
     * Gets a portal given a location adjacent to its entrance
     *
     * @param location <p>A location adjacent to the portal's entrance</p>
     * @return <p>The portal adjacent to the given location</p>
     */
    public static Portal getByAdjacentEntrance(Location location) {
        return getByAdjacentEntrance(location, 1);
    }

    /**
     * Gets a portal given a location adjacent to its entrance
     *
     * @param location <p>A location adjacent to the portal's entrance</p>
     * @param range    <p>The range to scan for portals</p>
     * @return <p>The portal adjacent to the given location</p>
     */
    public static Portal getByAdjacentEntrance(Location location, int range) {
        List<BlockLocation> adjacentPositions = new ArrayList<>();
        BlockLocation centerLocation = new BlockLocation(location.getBlock());
        adjacentPositions.add(centerLocation);

        for (int index = 1; index <= range; index++) {
            adjacentPositions.add(centerLocation.makeRelative(index, 0, 0));
            adjacentPositions.add(centerLocation.makeRelative(-index, 0, 0));
            adjacentPositions.add(centerLocation.makeRelative(0, 0, index));
            adjacentPositions.add(centerLocation.makeRelative(0, 0, -index));
            if (index < range) {
                adjacentPositions.add(centerLocation.makeRelative(index, 0, index));
                adjacentPositions.add(centerLocation.makeRelative(-index, 0, -index));
                adjacentPositions.add(centerLocation.makeRelative(index, 0, -index));
                adjacentPositions.add(centerLocation.makeRelative(-index, 0, index));
            }
        }

        for (BlockLocation adjacentPosition : adjacentPositions) {
            Portal portal = lookupEntrances.get(adjacentPosition);
            if (portal != null) {
                return portal;
            }
        }
        return null;
    }

    /**
     * Gets a portal given its control block (the block type used for the sign and button)
     *
     * @param block <p>The portal's control block</p>
     * @return <p>The gate with the given control block</p>
     */
    public static Portal getByControl(Block block) {
        return lookupControls.get(new BlockLocation(block));
    }

    /**
     * Gets a portal given a block
     *
     * @param block <p>One of the loaded lookup blocks</p>
     * @return <p>The portal corresponding to the block</p>
     */
    public static Portal getByBlock(Block block) {
        return lookupBlocks.get(new BlockLocation(block));
    }

    /**
     * Gets a bungee gate given its name
     *
     * @param name <p>The name of the bungee gate to get</p>
     * @return <p>A bungee gate</p>
     */
    public static Portal getBungeeGate(String name) {
        return bungeePortals.get(name.toLowerCase());
    }

    /**
     * Saves all gates for the given world
     *
     * @param world <p>The world to save gates for</p>
     */
    public static void saveAllGates(World world) {
        Stargate.managedWorlds.add(world.getName());
        String loc = Stargate.getSaveLocation() + "/" + world.getName() + ".db";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(loc, false));

            for (Portal portal : allPortals) {
                String wName = portal.getWorld().getName();
                if (!wName.equalsIgnoreCase(world.getName())) continue;
                StringBuilder builder = new StringBuilder();
                BlockLocation button = portal.getButton();

                builder.append(portal.getName());
                builder.append(':');
                builder.append(portal.getId().toString());
                builder.append(':');
                builder.append((button != null) ? button.toString() : "");
                builder.append(':');
                builder.append(portal.getModX());
                builder.append(':');
                builder.append(portal.getModZ());
                builder.append(':');
                builder.append(portal.getYaw());
                builder.append(':');
                builder.append(portal.getTopLeft().toString());
                builder.append(':');
                builder.append(portal.getGate().getFilename());
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
                builder.append(portal.getWorld().getName());
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
            Stargate.logger.log(Level.SEVERE, "Exception while writing stargates to " + loc + ": " + e);
        }
    }

    /**
     * Clears all loaded gates and gate data from all worlds
     */
    public static void clearGates() {
        lookupBlocks.clear();
        portalLookupByNetwork.clear();
        lookupEntrances.clear();
        lookupControls.clear();
        allPortals.clear();
        allPortalNetworks.clear();
    }

    /**
     * Clears all gates loaded in a given world
     *
     * @param world <p>The world containing the portals to clear</p>
     */
    public static void clearGates(World world) {
        //This is necessary
        List<Portal> portalsToRemove = new ArrayList<>();
        allPortals.forEach((portal) -> {
            if (portal.getWorld().equals(world)) {
                portalsToRemove.add(portal);
            }
        });

        clearGates(portalsToRemove);
    }

    /**
     * Clears a given list of portals from all relevant variables
     *
     * @param portalsToRemove <p>A list of portals to remove</p>
     */
    private static void clearGates(List<Portal> portalsToRemove) {
        List<String> portalNames = new ArrayList<>();
        portalsToRemove.forEach((portal) -> portalNames.add(portal.getName()));
        lookupBlocks.keySet().removeIf((key) -> portalsToRemove.contains(lookupBlocks.get(key)));
        portalLookupByNetwork.keySet().forEach((network) -> portalLookupByNetwork.get(network).keySet().removeIf((key) ->
                portalsToRemove.contains(portalLookupByNetwork.get(network).get(key))));
        //Remove any networks with no portals
        portalLookupByNetwork.keySet().removeIf((key) -> portalLookupByNetwork.get(key).isEmpty());
        lookupEntrances.keySet().removeIf((key) -> portalsToRemove.contains(lookupEntrances.get(key)));
        lookupControls.keySet().removeIf((key) -> portalsToRemove.contains(lookupControls.get(key)));
        allPortals.removeIf(portalsToRemove::contains);
        allPortalNetworks.keySet().forEach((network) -> allPortalNetworks.get(network).removeIf(portalNames::contains));
        //Remove any networks with no portals
        allPortalNetworks.keySet().removeIf((network) -> allPortalNetworks.get(network).isEmpty());
    }

    /**
     * Loads all gates for the given world
     *
     * @param world <p>The world to load gates for</p>
     * @return <p>True if gates could be loaded</p>
     */
    public static boolean loadAllGates(World world) {
        String location = Stargate.getSaveLocation();

        File database = new File(location, world.getName() + ".db");

        if (database.exists()) {
            return loadGates(world, database);
        } else {
            Stargate.logger.info(Stargate.getString("prefix") + "{" + world.getName() + "} No stargates for world ");
        }
        return false;
    }

    /**
     * Loads all the given gates
     *
     * @param world    <p>The world to load gates for</p>
     * @param database <p>The database file containing the gates</p>
     * @return <p>True if the gates were loaded successfully</p>
     */
    private static boolean loadGates(World world, File database) {
        int lineIndex = 0;
        try {
            Scanner scanner = new Scanner(database);
            while (scanner.hasNextLine()) {
                lineIndex++;
                String line = scanner.nextLine().trim();

                //Ignore empty and comment lines
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                //Check if the min. required portal data is present
                String[] portalData = line.split(":");
                if (portalData.length < 8) {
                    Stargate.logger.info(Stargate.getString("prefix") + "Invalid line - " + lineIndex);
                    continue;
                }

                loadGate(portalData, world, lineIndex);
            }
            scanner.close();

            // Open any always-on gates. Do this here as it should be more efficient than in the loop.
            TwoTuple<Integer, Integer> portalCounts = openAlwaysOpenGates();

            Stargate.logger.info(String.format("%s{%s} Loaded %d stargates with %d set as always-on",
                    Stargate.getString("prefix"), world.getName(), portalCounts.getSecondValue(),
                    portalCounts.getFirstValue()));
            return true;
        } catch (Exception e) {
            Stargate.logger.log(Level.SEVERE, "Exception while reading stargates from " + database.getName() + ": " + lineIndex);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Loads one gate from a data array
     *
     * @param portalData <p>The array describing the portal</p>
     * @param world      <p>The world to create the portal in</p>
     * @param lineIndex  <p>The line index to report in case the user needs to fix an error</p>
     */
    private static void loadGate(String[] portalData, World world, int lineIndex) {
        //Load min. required portal data
        String name = portalData[0];
        BlockLocation sign = new BlockLocation(world, portalData[1]);
        BlockLocation button = (portalData[2].length() > 0) ? new BlockLocation(world, portalData[2]) : null;
        int modX = Integer.parseInt(portalData[3]);
        int modZ = Integer.parseInt(portalData[4]);
        float yaw = Float.parseFloat(portalData[5]);
        BlockLocation topLeft = new BlockLocation(world, portalData[6]);
        Gate gate = GateHandler.getGateByName(portalData[7]);
        if (gate == null) {
            Stargate.logger.info(Stargate.getString("prefix") + "Gate layout on line " + lineIndex +
                    " does not exist [" + portalData[7] + "]");
            return;
        }

        //Load extra portal data
        String destination = (portalData.length > 8) ? portalData[8] : "";
        String network = (portalData.length > 9) ? portalData[9] : Stargate.getDefaultNetwork();
        if (network.isEmpty()) {
            network = Stargate.getDefaultNetwork();
        }
        String ownerString = (portalData.length > 10) ? portalData[10] : "";

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
                Stargate.debug("loadAllGates", "Invalid stargate owner string: " + ownerString);
            }
        } else {
            ownerName = ownerString;
        }

        //Creates the new portal
        Portal portal = new Portal(topLeft, modX, modZ, yaw, sign, button, destination, name, false,
                network, gate, ownerUUID, ownerName, getPortalOptions(portalData));

        registerPortal(portal);
        portal.close(true);
    }

    /**
     * Gets all portal options stored in the portal data
     *
     * @param portalData <p>The string list containing all information about a portal</p>
     * @return <p>A map between portal options and booleans</p>
     */
    private static Map<PortalOption, Boolean> getPortalOptions(String[] portalData) {
        Map<PortalOption, Boolean> portalOptions = new HashMap<>();
        for (PortalOption option : PortalOption.values()) {
            int saveIndex = option.getSaveIndex();
            portalOptions.put(option, portalData.length > saveIndex && Boolean.parseBoolean(portalData[saveIndex]));
        }
        return portalOptions;
    }

    /**
     * Opens all always open gates
     *
     * @return <p>A TwoTuple where the first value is the number of always open gates and the second value is the total number of gates</p>
     */
    private static TwoTuple<Integer, Integer> openAlwaysOpenGates() {
        int portalCount = 0;
        int openCount = 0;
        for (Iterator<Portal> iterator = allPortals.iterator(); iterator.hasNext(); ) {
            Portal portal = iterator.next();
            if (portal == null) {
                continue;
            }

            // Verify portal integrity/register portal
            if (!portal.wasVerified() && (!portal.isVerified() || !portal.checkIntegrity())) {
                destroyInvalidStarGate(portal);
                iterator.remove();
                continue;
            }
            portalCount++;

            //Open the gate if it's set as always open or if it's a bungee gate
            if (portal.isFixed() && (Stargate.enableBungee && portal.isBungee() || portal.getDestination() != null &&
                    portal.isAlwaysOn())) {
                portal.open(true);
                openCount++;
            }
        }
        return new TwoTuple<>(openCount, portalCount);
    }

    /**
     * Destroys a star gate which has failed its integrity test
     *
     * @param portal <p>The portal of the star gate</p>
     */
    private static void destroyInvalidStarGate(Portal portal) {
        // DEBUG
        for (RelativeBlockVector control : portal.getGate().getLayout().getControls()) {
            if (!portal.getBlockAt(control).getBlock().getType().equals(portal.getGate().getControlBlock())) {
                Stargate.debug("loadAllGates", "Control Block Type == " + portal.getBlockAt(control).getBlock().getType().name());
            }
        }
        PortalHandler.unregisterPortal(portal, false);
        Stargate.logger.info(Stargate.getString("prefix") + "Destroying stargate at " + portal);
    }

    /**
     * Closes all star gate portals
     */
    public static void closeAllGates() {
        Stargate.logger.info("Closing all stargates.");
        for (Portal portal : allPortals) {
            if (portal != null) {
                portal.close(true);
            }
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
}
