package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.portal.property.PortalLocation;
import net.knarcraft.stargate.portal.property.PortalOption;
import net.knarcraft.stargate.portal.property.PortalStructure;
import net.knarcraft.stargate.portal.property.gate.Gate;
import net.knarcraft.stargate.portal.property.gate.GateHandler;
import net.knarcraft.stargate.utility.PermissionHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of all loaded portals, and handles portal creation
 */
public class PortalHandler {

    private PortalHandler() {

    }

    /**
     * Gets a copy of all portal networks
     *
     * @return <p>A copy of all portal networks</p>
     */
    public static Map<String, List<String>> getAllPortalNetworks() {
        return PortalRegistry.getAllPortalNetworks();
    }

    /**
     * Gets a copy of all bungee portals
     *
     * @return <p>A copy of all bungee portals</p>
     */
    public static Map<String, Portal> getBungeePortals() {
        return PortalRegistry.getBungeePortals();
    }

    /**
     * Gets names of all portals within a network
     *
     * @param network <p>The network to get portals from</p>
     * @return <p>A list of portal names</p>
     */
    public static List<String> getNetwork(String network) {
        return PortalRegistry.getNetwork(network);
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
        for (String destination : PortalRegistry.getAllPortalNetworks().get(network)) {
            Portal portal = getByName(destination, network);
            if (portal == null) {
                continue;
            }
            //Check if destination is a random portal
            if (portal.getOptions().isRandom()) {
                continue;
            }
            //Check if destination is always open (Don't show if so)
            if (portal.getOptions().isAlwaysOn() && !portal.getOptions().isShown()) {
                continue;
            }
            //Check if destination is this portal
            if (destination.equals(entrancePortal.getCleanName())) {
                continue;
            }
            //Check if destination is a fixed portal not pointing to this portal
            if (portal.getOptions().isFixed() &&
                    !Portal.cleanString(portal.getDestinationName()).equals(entrancePortal.getCleanName())) {
                continue;
            }
            //Allow random use by non-players (Minecarts)
            if (player == null) {
                destinations.add(portal.getName());
                continue;
            }
            //Check if this player can access the destination world
            if (PermissionHelper.cannotAccessWorld(player, portal.getWorld().getName())) {
                continue;
            }
            //The portal is visible to the player
            if (PermissionHelper.canSeePortal(player, portal)) {
                destinations.add(portal.getName());
            }
        }
        return destinations;
    }

    /**
     * Registers a portal
     *
     * @param portal <p>The portal to register</p>
     */
    public static void registerPortal(Portal portal) {
        PortalRegistry.registerPortal(portal);
    }

    /**
     * Checks if the new portal is a valid bungee portal
     *
     * @param portalOptions   <p>The enabled portal options</p>
     * @param player          <p>The player trying to create the new portal</p>
     * @param destinationName <p>The name of the portal's destination</p>
     * @param network         <p>The name of the portal's network</p>
     * @return <p>False if the portal is an invalid bungee portal. True otherwise</p>
     */
    static boolean isValidBungeePortal(Map<PortalOption, Boolean> portalOptions, Player player,
                                       String destinationName, String network) {
        if (portalOptions.get(PortalOption.BUNGEE)) {
            if (!PermissionHelper.hasPermission(player, "stargate.admin.bungee")) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("bungeeDeny"));
                return false;
            } else if (!Stargate.getGateConfig().enableBungee()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("bungeeDisabled"));
                return false;
            } else if (destinationName.isEmpty() || network.isEmpty()) {
                Stargate.getMessageSender().sendErrorMessage(player, Stargate.getString("bungeeEmpty"));
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to find a gate matching the portal the user is trying to create
     *
     * @param portalLocation <p>The location data for the new portal</p>
     * @param world          <p>The world the player is located in</p>
     * @return <p>The matching gate type, or null if no such gate could be found</p>
     */
    static Gate findMatchingGate(PortalLocation portalLocation, World world) {
        Block signParent = portalLocation.getSignLocation().getParent();
        BlockLocation parent = new BlockLocation(world, signParent.getX(), signParent.getY(),
                signParent.getZ());

        //Get all gates with the used type of control blocks
        Gate[] possibleGates = GateHandler.getGatesByControlBlock(signParent);
        double yaw = portalLocation.getYaw();
        Gate gate = null;

        for (Gate possibleGate : possibleGates) {
            //Get gate controls
            RelativeBlockVector[] vectors = possibleGate.getLayout().getControls();

            portalLocation.setButtonVector(null);
            for (RelativeBlockVector controlVector : vectors) {
                //Assuming the top-left location is pointing to the gate's top-left location, check if it's a match
                BlockLocation possibleTopLocation = parent.getRelativeLocation(controlVector.invert(), yaw);
                if (possibleGate.matches(possibleTopLocation, portalLocation.getYaw(), true)) {
                    gate = possibleGate;
                    portalLocation.setTopLeft(possibleTopLocation);
                } else {
                    portalLocation.setButtonVector(controlVector);
                }
            }
        }

        return gate;
    }

    /**
     * Updates the sign and open state of portals pointing at the newly created portal
     *
     * @param portal <p>The newly created portal</p>
     */
    static void updatePortalsPointingAtNewPortal(Portal portal) {
        for (String originName : PortalRegistry.getAllPortalNetworks().get(portal.getCleanNetwork())) {
            Portal origin = getByName(originName, portal.getCleanNetwork());
            if (origin == null ||
                    !Portal.cleanString(origin.getDestinationName()).equals(portal.getCleanName()) ||
                    !origin.getStructure().isVerified()) {
                continue;
            }
            //Update sign of fixed gates pointing at this gate
            if (origin.getOptions().isFixed()) {
                origin.drawSign();
            }
            //Open any always on portal pointing at this portal
            if (origin.getOptions().isAlwaysOn()) {
                origin.getPortalOpener().openPortal(true);
            }
        }
    }

    /**
     * Gets all portal options to be applied to a new portal
     *
     * @param player          <p>The player creating the portal</p>
     * @param destinationName <p>The destination of the portal</p>
     * @param options         <p>The string on the option line of the sign</p>
     * @return <p>A map containing all portal options and their values</p>
     */
    static Map<PortalOption, Boolean> getPortalOptions(Player player, String destinationName, String options) {
        Map<PortalOption, Boolean> portalOptions = new HashMap<>();
        for (PortalOption option : PortalOption.values()) {
            portalOptions.put(option, options.indexOf(option.getCharacterRepresentation()) != -1 &&
                    PermissionHelper.canUseOption(player, option));
        }

        //Can not create a non-fixed always-on portal
        if (portalOptions.get(PortalOption.ALWAYS_ON) && destinationName.length() == 0) {
            portalOptions.put(PortalOption.ALWAYS_ON, false);
        }

        //Show isn't useful if always on is false
        if (portalOptions.get(PortalOption.SHOW) && !portalOptions.get(PortalOption.ALWAYS_ON)) {
            portalOptions.put(PortalOption.SHOW, false);
        }

        //Random portals are always on and can't be shown
        if (portalOptions.get(PortalOption.RANDOM)) {
            portalOptions.put(PortalOption.ALWAYS_ON, true);
            portalOptions.put(PortalOption.SHOW, false);
        }

        //Bungee portals are always on and don't support Random
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
        Map<String, Map<String, Portal>> lookupMap = PortalRegistry.getPortalLookupByNetwork();
        if (!lookupMap.containsKey(network.toLowerCase())) {
            return null;
        }
        return lookupMap.get(network.toLowerCase()).get(name.toLowerCase());

    }

    /**
     * Gets a portal given its entrance
     *
     * @param location <p>The location of the portal's entrance</p>
     * @return <p>The portal at the given location</p>
     */
    public static Portal getByEntrance(Location location) {
        return PortalRegistry.getLookupEntrances().get(new BlockLocation(location.getWorld(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ()));
    }

    /**
     * Gets a portal given its entrance
     *
     * @param block <p>The block at the portal's entrance</p>
     * @return <p>The portal at the given block's location</p>
     */
    public static Portal getByEntrance(Block block) {
        return PortalRegistry.getLookupEntrances().get(new BlockLocation(block));
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
            adjacentPositions.add(centerLocation.makeRelativeBlockLocation(index, 0, 0));
            adjacentPositions.add(centerLocation.makeRelativeBlockLocation(-index, 0, 0));
            adjacentPositions.add(centerLocation.makeRelativeBlockLocation(0, 0, index));
            adjacentPositions.add(centerLocation.makeRelativeBlockLocation(0, 0, -index));
            if (index < range) {
                adjacentPositions.add(centerLocation.makeRelativeBlockLocation(index, 0, index));
                adjacentPositions.add(centerLocation.makeRelativeBlockLocation(-index, 0, -index));
                adjacentPositions.add(centerLocation.makeRelativeBlockLocation(index, 0, -index));
                adjacentPositions.add(centerLocation.makeRelativeBlockLocation(-index, 0, index));
            }
        }

        for (BlockLocation adjacentPosition : adjacentPositions) {
            Portal portal = PortalRegistry.getLookupEntrances().get(adjacentPosition);
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
     * @return <p>The portal with the given control block</p>
     */
    public static Portal getByControl(Block block) {
        return PortalRegistry.getLookupControls().get(new BlockLocation(block));
    }

    /**
     * Gets a portal given a block
     *
     * @param block <p>One of the loaded lookup blocks</p>
     * @return <p>The portal corresponding to the block</p>
     */
    public static Portal getByBlock(Block block) {
        return PortalRegistry.getLookupBlocks().get(new BlockLocation(block));
    }

    /**
     * Gets a bungee portal given its name
     *
     * @param name <p>The name of the bungee portal to get</p>
     * @return <p>A bungee portal</p>
     */
    public static Portal getBungeePortal(String name) {
        return PortalRegistry.getBungeePortals().get(name.toLowerCase());
    }

    /**
     * Gets all portal options stored in the portal data
     *
     * @param portalData <p>The string list containing all information about a portal</p>
     * @return <p>A map between portal options and booleans</p>
     */
    public static Map<PortalOption, Boolean> getPortalOptions(String[] portalData) {
        Map<PortalOption, Boolean> portalOptions = new HashMap<>();
        for (PortalOption option : PortalOption.values()) {
            int saveIndex = option.getSaveIndex();
            portalOptions.put(option, portalData.length > saveIndex && Boolean.parseBoolean(portalData[saveIndex]));
        }
        return portalOptions;
    }

    /**
     * Opens all always-on portals
     *
     * @return <p>The number of always open portals enabled</p>
     */
    public static int openAlwaysOpenPortals() {
        int alwaysOpenCount = 0;

        for (Portal portal : PortalRegistry.getAllPortals()) {
            //Open the gate if it's set as always open or if it's a bungee gate
            if (portal.getOptions().isFixed() && (Stargate.getGateConfig().enableBungee() &&
                    portal.getOptions().isBungee() || portal.getPortalActivator().getDestination() != null &&
                    portal.getOptions().isAlwaysOn())) {
                portal.getPortalOpener().openPortal(true);
                alwaysOpenCount++;
            }
        }
        return alwaysOpenCount;
    }

    /**
     * Tries to verify all portals and un-registers non-verifiable portals
     */
    public static void verifyAllPortals() {
        List<Portal> invalidPortals = new ArrayList<>();
        for (Portal portal : PortalRegistry.getAllPortals()) {
            //Try and verify the portal. Invalidate it if it cannot be validated
            PortalStructure structure = portal.getStructure();
            if (!structure.wasVerified() && (!structure.isVerified() || !structure.checkIntegrity())) {
                invalidPortals.add(portal);
            }
        }

        //Un-register any invalid portals found
        for (Portal portal : invalidPortals) {
            unregisterInvalidPortal(portal);
        }
    }

    /**
     * Un-registers a portal which has failed its integrity tests
     *
     * @param portal <p>The portal of the star portal</p>
     */
    private static void unregisterInvalidPortal(Portal portal) {
        //Show debug information
        for (RelativeBlockVector control : portal.getGate().getLayout().getControls()) {
            Block block = portal.getBlockAt(control).getBlock();
            //Log control blocks not matching the gate layout
            if (!block.getType().equals(portal.getGate().getControlBlock())) {
                Stargate.debug("PortalHandler::destroyInvalidPortal", "Control Block Type == " +
                        block.getType().name());
            }
        }
        PortalRegistry.unregisterPortal(portal, false);
        Stargate.logInfo(String.format("Destroying stargate at %s", portal));
    }

    /**
     * Closes all portals
     */
    public static void closeAllPortals() {
        Stargate.logInfo("Closing all stargates.");
        for (Portal portal : PortalRegistry.getAllPortals()) {
            if (portal != null) {
                portal.getPortalOpener().closePortal(true);
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
