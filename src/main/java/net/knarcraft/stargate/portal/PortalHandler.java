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

    /**
     * Gets names of all portals within a network
     *
     * @param network <p>The network to get portals from</p>
     * @return <p>A list of portal names</p>
     */
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
            //Check if destination is a random portal
            if (portal.getOptions().isRandom()) {
                continue;
            }
            //Check if destination is always open (Don't show if so)
            if (portal.getOptions().isAlwaysOn() && !portal.getOptions().isShown()) {
                continue;
            }
            //Check if destination is this portal
            if (destination.equalsIgnoreCase(entrancePortal.getName())) {
                continue;
            }
            //Check if destination is a fixed portal not pointing to this portal
            if (portal.getOptions().isFixed() && !portal.getDestinationName().equalsIgnoreCase(entrancePortal.getName())) {
                continue;
            }
            //Allow random use by non-players (Minecarts)
            if (player == null) {
                destinations.add(portal.getName());
                continue;
            }
            //Check if this player can access the dest world
            if (PermissionHelper.cannotAccessWorld(player, portal.getWorld().getName())) {
                Stargate.logger.info("cannot access world");
                continue;
            }
            //Visible to this player.
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
        portal.getPortalOpener().closePortal(true);

        String portalName = portal.getName().toLowerCase();
        String networkName = portal.getNetwork().toLowerCase();

        //Remove portal from lookup blocks
        for (BlockLocation block : portal.getStructure().getFrame()) {
            lookupBlocks.remove(block);
        }

        //Remove registered info about the lookup controls and blocks
        lookupBlocks.remove(portal.getSignLocation());
        lookupControls.remove(portal.getSignLocation());

        BlockLocation button = portal.getStructure().getButton();
        if (button != null) {
            lookupBlocks.remove(button);
            lookupControls.remove(button);
        }

        //Remove entrances
        for (BlockLocation entrance : portal.getStructure().getEntrances()) {
            lookupEntrances.remove(entrance);
        }

        //Remove the portal from the list of all portals
        if (removeAll) {
            allPortals.remove(portal);
        }

        if (portal.getOptions().isBungee()) {
            //Remove the bungee listing
            bungeePortals.remove(portalName);
        } else {
            //Remove from network lists
            portalLookupByNetwork.get(networkName).remove(portalName);
            allPortalNetworks.get(networkName).remove(portalName);

            //Update all portals in the same network with this portal as its destination
            for (String originName : allPortalNetworks.get(networkName)) {
                Portal origin = getByName(originName, portal.getNetwork());
                if (origin == null || !origin.getDestinationName().equalsIgnoreCase(portalName) ||
                        !origin.getStructure().isVerified()) {
                    continue;
                }
                //Update the portal's sign
                if (origin.getOptions().isFixed()) {
                    origin.drawSign();
                }
                //Close portal without destination
                if (origin.getOptions().isAlwaysOn()) {
                    origin.getPortalOpener().closePortal(true);
                }
            }
        }

        //Clear sign data
        if (portal.getSignLocation().getBlock().getBlockData() instanceof WallSign) {
            Sign sign = (Sign) portal.getSignLocation().getBlock().getState();
            sign.setLine(0, portal.getName());
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");
            sign.update();
        }

        saveAllPortals(portal.getWorld());
    }

    /**
     * Registers a portal
     *
     * @param portal <p>The portal to register</p>
     */
    private static void registerPortal(Portal portal) {
        portal.getOptions().setFixed(portal.getDestinationName().length() > 0 || portal.getOptions().isRandom() ||
                portal.getOptions().isBungee());

        String portalName = portal.getName().toLowerCase();
        String networkName = portal.getNetwork().toLowerCase();

        //Bungee portals are stored in their own list
        if (portal.getOptions().isBungee()) {
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
        for (BlockLocation block : portal.getStructure().getFrame()) {
            lookupBlocks.put(block, portal);
        }
        //Register the sign and button to the lookup lists
        lookupBlocks.put(portal.getSignLocation(), portal);
        lookupControls.put(portal.getSignLocation(), portal);

        BlockLocation button = portal.getStructure().getButton();
        if (button != null) {
            lookupBlocks.put(button, portal);
            lookupControls.put(button, portal);
        }

        //Register entrances to the lookup list
        for (BlockLocation entrance : portal.getStructure().getEntrances()) {
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
        BlockLocation signLocation = new BlockLocation(event.getBlock());
        Block idParent = signLocation.getParent();

        //Return early if the sign is not placed on a block, or the block is not a control block
        if (idParent == null || GateHandler.getGatesByControlBlock(idParent).length == 0) {
            Stargate.debug("createPortal", "Control block not registered");
            return null;
        }

        //The control block is already part of another portal
        if (getByBlock(idParent) != null) {
            Stargate.debug("createPortal", "idParent belongs to existing stargate");
            return null;
        }

        //Get necessary information from the gate's sign
        String portalName = filterName(event.getLine(0));
        String destinationName = filterName(event.getLine(1));
        String network = filterName(event.getLine(2));
        String options = filterName(event.getLine(3)).toLowerCase();

        //Get portal options available to the player creating the portal
        Map<PortalOption, Boolean> portalOptions = getPortalOptions(player, destinationName, options);

        //Get the yaw
        float yaw = DirectionHelper.getYawFromLocationDifference(idParent.getLocation(), signLocation.getLocation());

        //Get the direction the button should be facing
        BlockFace buttonFacing = DirectionHelper.getBlockFaceFromYaw(yaw);

        PortalLocation portalLocation = new PortalLocation();
        portalLocation.setButtonFacing(buttonFacing).setYaw(yaw).setSignLocation(signLocation);

        Stargate.debug("createPortal", "Finished getting all portal info");

        //Try and find a gate matching the new portal
        Gate gate = findMatchingGate(portalLocation, player);
        if ((gate == null) || (portalLocation.getButtonVector() == null)) {
            Stargate.debug("createPortal", "Could not find matching gate layout");
            return null;
        }

        //If the portal is a bungee portal and invalid, abort here
        if (!isValidBungeePortal(portalOptions, player, destinationName, network)) {
            Stargate.debug("createPortal", "Portal is an invalid bungee portal");
            return null;
        }

        //Debug
        StringBuilder builder = new StringBuilder();
        for (PortalOption option : portalOptions.keySet()) {
            builder.append(option.getCharacterRepresentation()).append(" = ").append(portalOptions.get(option)).append(" ");
        }
        Stargate.debug("createPortal", builder.toString());

        //Use default network if a proper alternative is not set
        if (!portalOptions.get(PortalOption.BUNGEE) && (network.length() < 1 || network.length() > 11)) {
            network = Stargate.getDefaultNetwork();
        }

        boolean deny = false;
        String denyMessage = "";

        //Check if the player can create portals on this network
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
                denyMessage = Stargate.getString("createNetDeny");
                //return null;
            }
        }

        //Check if the player can create this gate layout
        String gateName = gate.getFilename();
        gateName = gateName.substring(0, gateName.indexOf('.'));
        if (!deny && !PermissionHelper.canCreateGate(player, gateName)) {
            Stargate.debug("createPortal", "Player does not have access to gate layout");
            deny = true;
            denyMessage = Stargate.getString("createGateDeny");
        }

        //Check if the user can create portals to this world.
        if (!portalOptions.get(PortalOption.BUNGEE) && !deny && destinationName.length() > 0) {
            Portal portal = getByName(destinationName, network);
            if (portal != null) {
                String world = portal.getWorld().getName();
                if (PermissionHelper.cannotAccessWorld(player, world)) {
                    Stargate.debug("canCreateNetworkGate", "Player does not have access to destination world");
                    deny = true;
                    denyMessage = Stargate.getString("createWorldDeny");
                }
            }
        }

        //Check if a conflict exists
        if (conflictsWithExistingPortal(gate, portalLocation.getTopLeft(), yaw, player)) {
            return null;
        }

        return createAndValidateNewPortal(portalLocation, destinationName, portalName, network, gate,
                player, portalOptions, denyMessage, event.getLines(), deny);
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
    private static boolean isValidBungeePortal(Map<PortalOption, Boolean> portalOptions, Player player,
                                               String destinationName, String network) {
        if (portalOptions.get(PortalOption.BUNGEE)) {
            if (!PermissionHelper.hasPermission(player, "stargate.admin.bungee")) {
                Stargate.sendErrorMessage(player, Stargate.getString("bungeeDeny"));
                return false;
            } else if (!Stargate.enableBungee) {
                Stargate.sendErrorMessage(player, Stargate.getString("bungeeDisabled"));
                return false;
            } else if (destinationName.isEmpty() || network.isEmpty()) {
                Stargate.sendErrorMessage(player, Stargate.getString("bungeeEmpty"));
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to find a gate matching the portal the user is trying to create
     *
     * @param portalLocation <p>The location data for the new portal</p>
     * @param player         <p>The player trying to create the new portal</p>
     * @return <p>The matching gate type, or null if no such gate could be found</p>
     */
    private static Gate findMatchingGate(PortalLocation portalLocation, Player player) {
        Block signParent = portalLocation.getSignLocation().getParent();
        BlockLocation parent = new BlockLocation(player.getWorld(), signParent.getX(), signParent.getY(),
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
     * Checks whether the new portal conflicts with an existing portal
     *
     * @param gate    <p>The gate type of the new portal</p>
     * @param topLeft <p>The top-left block of the new portal</p>
     * @param yaw     <p>The yaw when looking directly outwards from the portal</p>
     * @param player  <p>The player creating the new portal</p>
     * @return <p>True if a conflict was found. False otherwise</p>
     */
    private static boolean conflictsWithExistingPortal(Gate gate, BlockLocation topLeft, double yaw, Player player) {
        //TODO: Make a quicker check. Could just check for control block conflicts if all code is changed to account for
        //      getting several hits at a single location when checking for the existence of a portal. May make
        //      everything slower overall? Would make for cooler gates though.
        for (RelativeBlockVector borderVector : gate.getLayout().getBorder()) {
            BlockLocation borderBlockLocation = topLeft.getRelativeLocation(borderVector, yaw);
            if (getByBlock(borderBlockLocation.getBlock()) != null) {
                Stargate.debug("createPortal", "Gate conflicts with existing gate");
                Stargate.sendErrorMessage(player, Stargate.getString("createConflict"));
                return true;
            }
        }
        return false;
    }

    /**
     * Creates and validates a new portal
     *
     * @param destinationName <p>The name of the portal's destination</p>
     * @param portalName      <p>The name of the new portal</p>
     * @param network         <p>The name of the new portal's network</p>
     * @param gate            <p>The gate type used in the physical construction of the new portal</p>
     * @param player          <p>The player creating the new portal</p>
     * @param portalOptions   <p>A map of enabled and disabled portal options</p>
     * @param denyMessage     <p>The deny message to display if the portal creation was denied</p>
     * @param lines           <p>All the lines of the sign which initiated the portal creation</p>
     * @param deny            <p>Whether to deny the creation of the new portal</p>
     * @return <p>A new portal, or null if the input cases the creation to be denied</p>
     */
    private static Portal createAndValidateNewPortal(PortalLocation portalLocation, String destinationName,
                                                     String portalName, String network, Gate gate, Player player,
                                                     Map<PortalOption, Boolean> portalOptions, String denyMessage,
                                                     String[] lines, boolean deny) {
        Portal portal = new Portal(portalLocation, null, destinationName, portalName,
                network, gate, player.getUniqueId(), player.getName(), portalOptions);

        int createCost = EconomyHandler.getCreateCost(player, gate);

        //Call StargateCreateEvent to let other plugins cancel or overwrite denial
        StargateCreateEvent stargateCreateEvent = new StargateCreateEvent(player, portal, lines, deny,
                denyMessage, createCost);
        Stargate.server.getPluginManager().callEvent(stargateCreateEvent);
        if (stargateCreateEvent.isCancelled()) {
            return null;
        }

        //Tell the user why it was denied from creating the portal
        if (stargateCreateEvent.getDeny()) {
            Stargate.sendErrorMessage(player, stargateCreateEvent.getDenyReason());
            return null;
        }

        createCost = stargateCreateEvent.getCost();

        //Check if the new portal is valid
        if (!checkIfNewPortalIsValid(portal, player, createCost, portalName)) {
            return null;
        }

        //Add button if the portal is not always on
        if (!portalOptions.get(PortalOption.ALWAYS_ON)) {
            generatePortalButton(portal, portalLocation.getTopLeft(), portalLocation.getButtonVector(),
                    portalLocation.getButtonFacing());
        }

        //Register the new portal
        registerPortal(portal);
        updateNewPortal(portal, destinationName);

        //Update portals pointing at this one if it's not a bungee portal
        if (!portal.getOptions().isBungee()) {
            updatePortalsPointingAtNewPortal(portal);
        }

        saveAllPortals(portal.getWorld());

        return portal;
    }

    /**
     * Checks whether the newly created, but unregistered portal is valid
     *
     * @param portal     <p>The portal to validate</p>
     * @param player     <p>The player creating the portal</p>
     * @param cost       <p>The cost of creating the portal</p>
     * @param portalName <p>The name of the newly created portal</p>
     * @return <p>True if the portal is completely valid</p>
     */
    private static boolean checkIfNewPortalIsValid(Portal portal, Player player, int cost, String portalName) {
        // Name & Network can be changed in the event, so do these checks here.
        if (portal.getName().length() < 1 || portal.getName().length() > 11) {
            Stargate.debug("createPortal", "Name length error");
            Stargate.sendErrorMessage(player, Stargate.getString("createNameLength"));
            return false;
        }

        //Don't do network checks for bungee portals
        if (portal.getOptions().isBungee()) {
            if (bungeePortals.get(portal.getName().toLowerCase()) != null) {
                Stargate.debug("createPortal::Bungee", "Gate name duplicate");
                Stargate.sendErrorMessage(player, Stargate.getString("createExists"));
                return false;
            }
        } else {
            if (getByName(portal.getName(), portal.getNetwork()) != null) {
                Stargate.debug("createPortal", "Gate name duplicate");
                Stargate.sendErrorMessage(player, Stargate.getString("createExists"));
                return false;
            }

            //Check if there are too many gates in this network
            List<String> networkList = allPortalNetworks.get(portal.getNetwork().toLowerCase());
            if (Stargate.maxGatesEachNetwork > 0 && networkList != null && networkList.size() >= Stargate.maxGatesEachNetwork) {
                Stargate.sendErrorMessage(player, Stargate.getString("createFull"));
                return false;
            }
        }

        if (cost > 0) {
            if (!EconomyHandler.chargePlayerIfNecessary(player, cost)) {
                EconomyHelper.sendInsufficientFundsMessage(portalName, player, cost);
                Stargate.debug("createPortal", "Insufficient Funds");
                return false;
            } else {
                EconomyHelper.sendDeductMessage(portalName, player, cost);
            }
        }
        return true;
    }

    /**
     * Generates a button for a portal
     *
     * @param portal       <p>The portal to generate a button for</p>
     * @param topLeft      <p>The top-left block of the portal</p>
     * @param buttonVector <p>A relative vector pointing at the button</p>
     * @param buttonFacing <p>The direction the button should be facing</p>
     */
    private static void generatePortalButton(Portal portal, BlockLocation topLeft, RelativeBlockVector buttonVector,
                                             BlockFace buttonFacing) {
        //Go one block outwards to find the button's location rather than the control block's location
        BlockLocation button = topLeft.getRelativeLocation(buttonVector.addToVector(
                RelativeBlockVector.Property.DISTANCE, 1), portal.getYaw());

        Directional buttonData = (Directional) Bukkit.createBlockData(portal.getGate().getPortalButton());
        buttonData.setFacing(buttonFacing);
        button.getBlock().setBlockData(buttonData);
        portal.getStructure().setButton(button);
    }

    /**
     * Updates the open state of the newly created portal
     *
     * @param portal          <p>The portal newly created</p>
     * @param destinationName <p>The name of the destination portal</p>
     */
    private static void updateNewPortal(Portal portal, String destinationName) {
        portal.drawSign();
        //Open an always on portal
        if (portal.getOptions().isRandom() || portal.getOptions().isBungee()) {
            portal.getPortalOpener().openPortal(true);
        } else if (portal.getOptions().isAlwaysOn()) {
            Portal destinationPortal = getByName(destinationName, portal.getNetwork());
            if (destinationPortal != null) {
                portal.getPortalOpener().openPortal(true);
                destinationPortal.drawSign();
            }
        } else {
            //Update the block type for the portal's opening to the closed block
            for (BlockLocation entrance : portal.getStructure().getEntrances()) {
                entrance.setType(portal.getGate().getPortalClosedBlock());
            }
        }
    }

    /**
     * Updates the sign and open state of portals pointing at the newly created portal
     *
     * @param portal <p>The newly created portal</p>
     */
    private static void updatePortalsPointingAtNewPortal(Portal portal) {
        for (String originName : allPortalNetworks.get(portal.getNetwork().toLowerCase())) {
            Portal origin = getByName(originName, portal.getNetwork());
            if (origin == null ||
                    !origin.getDestinationName().equalsIgnoreCase(portal.getName()) ||
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
    private static Map<PortalOption, Boolean> getPortalOptions(Player player, String destinationName, String options) {
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
     * @return <p>The portal with the given control block</p>
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
     * Gets a bungee portal given its name
     *
     * @param name <p>The name of the bungee portal to get</p>
     * @return <p>A bungee portal</p>
     */
    public static Portal getBungeePortal(String name) {
        return bungeePortals.get(name.toLowerCase());
    }

    /**
     * Saves all portals for the given world
     *
     * @param world <p>The world to save portals for</p>
     */
    public static void saveAllPortals(World world) {
        Stargate.managedWorlds.add(world.getName());
        String loc = Stargate.getSaveLocation() + "/" + world.getName() + ".db";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(loc, false));

            for (Portal portal : allPortals) {
                String wName = portal.getWorld().getName();
                if (!wName.equalsIgnoreCase(world.getName())) continue;
                StringBuilder builder = new StringBuilder();
                BlockLocation button = portal.getStructure().getButton();

                builder.append(portal.getName()).append(':');
                builder.append(portal.getSignLocation().toString()).append(':');
                builder.append((button != null) ? button.toString() : "").append(':');
                builder.append(0).append(':');
                builder.append(0).append(':');
                builder.append(portal.getYaw()).append(':');
                builder.append(portal.getTopLeft().toString()).append(':');
                builder.append(portal.getGate().getFilename()).append(':');
                builder.append(portal.getOptions().isFixed() ? portal.getDestinationName() : "").append(':');
                builder.append(portal.getNetwork()).append(':');
                UUID owner = portal.getOwnerUUID();
                if (owner != null) {
                    builder.append(portal.getOwnerUUID().toString());
                } else {
                    builder.append(portal.getOwnerName());
                }
                builder.append(':');
                builder.append(portal.getOptions().isHidden()).append(':');
                builder.append(portal.getOptions().isAlwaysOn()).append(':');
                builder.append(portal.getOptions().isPrivate()).append(':');
                builder.append(portal.getWorld().getName()).append(':');
                builder.append(portal.getOptions().isFree()).append(':');
                builder.append(portal.getOptions().isBackwards()).append(':');
                builder.append(portal.getOptions().isShown()).append(':');
                builder.append(portal.getOptions().isNoNetwork()).append(':');
                builder.append(portal.getOptions().isRandom()).append(':');
                builder.append(portal.getOptions().isBungee());

                bw.append(builder.toString());
                bw.newLine();
            }

            bw.close();
        } catch (Exception e) {
            Stargate.logger.log(Level.SEVERE, "Exception while writing stargates to " + loc + ": " + e);
        }
    }

    /**
     * Clears all loaded portals and portal data from all worlds
     */
    public static void clearPortals() {
        lookupBlocks.clear();
        portalLookupByNetwork.clear();
        lookupEntrances.clear();
        lookupControls.clear();
        allPortals.clear();
        allPortalNetworks.clear();
    }

    /**
     * Clears all portals loaded in a given world
     *
     * @param world <p>The world containing the portals to clear</p>
     */
    public static void clearPortals(World world) {
        //This is necessary
        List<Portal> portalsToRemove = new ArrayList<>();
        allPortals.forEach((portal) -> {
            if (portal.getWorld().equals(world)) {
                portalsToRemove.add(portal);
            }
        });

        clearPortals(portalsToRemove);
    }

    /**
     * Clears a given list of portals from all relevant variables
     *
     * @param portalsToRemove <p>A list of portals to remove</p>
     */
    private static void clearPortals(List<Portal> portalsToRemove) {
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
     * Loads all portals for the given world
     *
     * @param world <p>The world to load portals for</p>
     * @return <p>True if portals could be loaded</p>
     */
    public static boolean loadAllPortals(World world) {
        String location = Stargate.getSaveLocation();

        File database = new File(location, world.getName() + ".db");

        if (database.exists()) {
            return loadPortals(world, database);
        } else {
            Stargate.logger.info(Stargate.getString("prefix") + "{" + world.getName() + "} No stargates for world ");
        }
        return false;
    }

    /**
     * Loads all the given portals
     *
     * @param world    <p>The world to load portals for</p>
     * @param database <p>The database file containing the portals</p>
     * @return <p>True if the portals were loaded successfully</p>
     */
    private static boolean loadPortals(World world, File database) {
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

                loadPortal(portalData, world, lineIndex);
            }
            scanner.close();

            // Open any always-on gates. Do this here as it should be more efficient than in the loop.
            TwoTuple<Integer, Integer> portalCounts = openAlwaysOpenPortals();

            Stargate.logger.info(String.format("%s{%s} Loaded %d stargates with %d set as always-on",
                    Stargate.getString("prefix"), world.getName(), portalCounts.getSecondValue(),
                    portalCounts.getFirstValue()));

            //Re-draw the signs in case a bug in the config prevented the portal from loading and has been fixed since
            for (Portal portal : allPortals) {
                portal.drawSign();
            }
            return true;
        } catch (Exception e) {
            Stargate.logger.log(Level.SEVERE, "Exception while reading stargates from " + database.getName() + ": " + lineIndex);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Loads one portal from a data array
     *
     * @param portalData <p>The array describing the portal</p>
     * @param world      <p>The world to create the portal in</p>
     * @param lineIndex  <p>The line index to report in case the user needs to fix an error</p>
     */
    private static void loadPortal(String[] portalData, World world, int lineIndex) {
        //Load min. required portal data
        String name = portalData[0];
        PortalLocation portalLocation = new PortalLocation();
        portalLocation.setSignLocation(new BlockLocation(world, portalData[1]));
        BlockLocation button = (portalData[2].length() > 0) ? new BlockLocation(world, portalData[2]) : null;
        portalLocation.setYaw(Float.parseFloat(portalData[5]));
        portalLocation.setTopLeft(new BlockLocation(world, portalData[6]));
        Gate gate = GateHandler.getGateByName(portalData[7]);
        if (gate == null) {
            //Mark the sign as invalid to reduce some player confusion
            Sign sign = (Sign) portalLocation.getSignLocation().getBlock().getState();
            Stargate.setLine(sign, 3, Stargate.getString("signInvalidGate"));
            sign.update();

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
                Stargate.debug("loadAllPortals", "Invalid stargate owner string: " + ownerString);
            }
        } else {
            ownerName = ownerString;
        }

        //Creates the new portal
        Portal portal = new Portal(portalLocation, button, destination, name,
                network, gate, ownerUUID, ownerName, getPortalOptions(portalData));

        registerPortal(portal);
        portal.getPortalOpener().closePortal(true);
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
     * Opens all always open portals
     *
     * @return <p>A TwoTuple where the first value is the number of always open portals and the second value is the total number of portals</p>
     */
    private static TwoTuple<Integer, Integer> openAlwaysOpenPortals() {
        int portalCount = 0;
        int openCount = 0;
        for (Iterator<Portal> iterator = allPortals.iterator(); iterator.hasNext(); ) {
            Portal portal = iterator.next();
            if (portal == null) {
                continue;
            }

            // Verify portal integrity/register portal
            PortalStructure structure = portal.getStructure();
            if (!structure.wasVerified() && (!structure.isVerified() || !structure.checkIntegrity())) {
                destroyInvalidPortal(portal);
                iterator.remove();
                continue;
            }
            portalCount++;

            //Open the gate if it's set as always open or if it's a bungee gate
            if (portal.getOptions().isFixed() && (Stargate.enableBungee && portal.getOptions().isBungee() ||
                    portal.getPortalActivator().getDestination() != null && portal.getOptions().isAlwaysOn())) {
                portal.getPortalOpener().openPortal(true);
                openCount++;
            }
        }
        return new TwoTuple<>(openCount, portalCount);
    }

    /**
     * Destroys a portal which has failed its integrity test
     *
     * @param portal <p>The portal of the star portal</p>
     */
    private static void destroyInvalidPortal(Portal portal) {
        // DEBUG
        for (RelativeBlockVector control : portal.getGate().getLayout().getControls()) {
            if (!portal.getBlockAt(control).getBlock().getType().equals(portal.getGate().getControlBlock())) {
                Stargate.debug("loadAllPortals", "Control Block Type == " + portal.getBlockAt(control).getBlock().getType().name());
            }
        }
        PortalHandler.unregisterPortal(portal, false);
        Stargate.logger.info(Stargate.getString("prefix") + "Destroying stargate at " + portal);
    }

    /**
     * Closes all portals
     */
    public static void closeAllPortals() {
        Stargate.logger.info("Closing all stargates.");
        for (Portal portal : allPortals) {
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
