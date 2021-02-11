package net.knarcraft.stargate;

import net.knarcraft.stargate.event.StargateCreateEvent;
import net.knarcraft.stargate.utility.EconomyHelper;
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

public class PortalHandler {
    // Static variables used to store portal lists
    private static final Map<BlockLocation, Portal> lookupBlocks = new HashMap<>();
    private static final Map<BlockLocation, Portal> lookupEntrances = new HashMap<>();
    private static final Map<BlockLocation, Portal> lookupControls = new HashMap<>();
    private static final List<Portal> allPortals = new ArrayList<>();
    private static final HashMap<String, List<String>> allPortalsNet = new HashMap<>();
    private static final HashMap<String, HashMap<String, Portal>> lookupNamesNet = new HashMap<>();

    // A list of Bungee gates
    private static final Map<String, Portal> bungeePortals = new HashMap<>();

    public static List<String> getNetwork(String network) {
        return allPortalsNet.get(network.toLowerCase());
    }

    /**
     * Gets all destinations in the network viewable by the given player
     * @param player <p>The player who wants to see destinations</p>
     * @param network <p>The network to get destinations from</p>
     * @return <p>All destinations the player can go to</p>
     */
    public static ArrayList<String> getDestinations(Player player, String network) {
        ArrayList<String> destinations = new ArrayList<>();
        for (String dest : allPortalsNet.get(network.toLowerCase())) {
            Portal portal = getByName(dest, network);
            if (portal == null) continue;
            // Check if dest is a random gate
            if (portal.isRandom()) continue;
            // Check if dest is always open (Don't show if so)
            if (portal.isAlwaysOn() && !portal.isShown()) continue;
            // Check if dest is this portal
            if (dest.equalsIgnoreCase(portal.getName())) continue;
            // Check if dest is a fixed gate not pointing to this gate
            if (portal.isFixed() && !portal.getDestinationName().equalsIgnoreCase(portal.getName())) continue;
            // Allow random use by non-players (Minecarts)
            if (player == null) {
                destinations.add(portal.getName());
                continue;
            }
            // Check if this player can access the dest world
            if (!Stargate.canAccessWorld(player, portal.getWorld().getName())) continue;
            // Visible to this player.
            if (Stargate.canSee(player, portal)) {
                destinations.add(portal.getName());
            }
        }
        return destinations;
    }

    /**
     * Un-registers the given portal
     * @param portal <p>The portal to un-register</p>
     * @param removeAll <p>Whether to remove the portal from the list of all portals</p>
     */
    public static void unregister(Portal portal, boolean removeAll) {
        Stargate.debug("Unregister", "Unregistering gate " + portal.getName());
        portal.close(true);

        for (BlockLocation block : portal.getFrame()) {
            lookupBlocks.remove(block);
        }
        // Include the sign and button
        lookupBlocks.remove(portal.getId());
        lookupControls.remove(portal.getId());
        if (portal.getButton() != null) {
            lookupBlocks.remove(portal.getButton());
            lookupControls.remove(portal.getButton());
        }

        for (BlockLocation entrance : portal.getEntrances()) {
            lookupEntrances.remove(entrance);
        }

        if (removeAll) {
            allPortals.remove(portal);
        }

        if (portal.isBungee()) {
            bungeePortals.remove(portal.getName().toLowerCase());
        } else {
            lookupNamesNet.get(portal.getNetwork().toLowerCase()).remove(portal.getName().toLowerCase());
            allPortalsNet.get(portal.getNetwork().toLowerCase()).remove(portal.getName().toLowerCase());

            for (String originName : allPortalsNet.get(portal.getNetwork().toLowerCase())) {
                Portal origin = getByName(originName, portal.getNetwork());
                if (origin == null) continue;
                if (!origin.getDestinationName().equalsIgnoreCase(portal.getName())) continue;
                if (!origin.isVerified()) continue;
                if (origin.isFixed()) origin.drawSign();
                if (origin.isAlwaysOn()) origin.close(true);
            }
        }

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
     * @param portal <p>The portal to register</p>
     */
    static void register(Portal portal) {
        portal.setFixed(portal.getDestinationName().length() > 0 || portal.isRandom() || portal.isBungee());

        // Bungee gates are stored in their own list
        if (portal.isBungee()) {
            bungeePortals.put(portal.getName().toLowerCase(), portal);
        } else {
            // Check if network exists in our network list
            if (!lookupNamesNet.containsKey(portal.getNetwork().toLowerCase())) {
                Stargate.debug("register", "Network " + portal.getNetwork() + " not in lookupNamesNet, adding");
                lookupNamesNet.put(portal.getNetwork().toLowerCase(), new HashMap<>());
            }
            lookupNamesNet.get(portal.getNetwork().toLowerCase()).put(portal.getName().toLowerCase(), portal);

            // Check if this network exists
            if (!allPortalsNet.containsKey(portal.getNetwork().toLowerCase())) {
                Stargate.debug("register", "Network " + portal.getNetwork() + " not in allPortalsNet, adding");
                allPortalsNet.put(portal.getNetwork().toLowerCase(), new ArrayList<>());
            }
            allPortalsNet.get(portal.getNetwork().toLowerCase()).add(portal.getName().toLowerCase());
        }

        for (BlockLocation block : portal.getFrame()) {
            lookupBlocks.put(block, portal);
        }
        // Include the sign and button
        lookupBlocks.put(portal.getId(), portal);
        lookupControls.put(portal.getId(), portal);
        if (portal.getButton() != null) {
            lookupBlocks.put(portal.getButton(), portal);
            lookupControls.put(portal.getButton(), portal);
        }


        for (BlockLocation entrance : portal.getEntrances()) {
            lookupEntrances.put(entrance, portal);
        }

        allPortals.add(portal);
    }

    /**
     * Creates a new portal
     * @param event <p>The sign change event which initialized the creation</p>
     * @param player <p>The player who's creating the portal</p>
     * @return <p>The created portal</p>
     */
    public static Portal createPortal(SignChangeEvent event, Player player) {
        BlockLocation id = new BlockLocation(event.getBlock());
        Block idParent = id.getParent();
        if (idParent == null) {
            return null;
        }

        if (Gate.getGatesByControlBlock(idParent).length == 0) {
            return null;
        }

        if (getByBlock(idParent) != null) {
            Stargate.debug("createPortal", "idParent belongs to existing gate");
            return null;
        }

        BlockLocation parent = new BlockLocation(player.getWorld(), idParent.getX(), idParent.getY(), idParent.getZ());
        BlockLocation topleft = null;
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
        if (hidden && !Stargate.canOption(player, "hidden")) {
            hidden = false;
        }
        if (alwaysOn && !Stargate.canOption(player, "alwayson")) {
            alwaysOn = false;
        }
        if (priv && !Stargate.canOption(player, "private")) {
            priv = false;
        }
        if (free && !Stargate.canOption(player, "free")) {
            free = false;
        }
        if (backwards && !Stargate.canOption(player, "backwards")) {
            backwards = false;
        }
        if (show && !Stargate.canOption(player, "show")) {
            show = false;
        }
        if (noNetwork && !Stargate.canOption(player, "nonetwork")) {
            noNetwork = false;
        }
        if (random && !Stargate.canOption(player, "random")) {
            random = false;
        }

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
            if (gate != null || buttonVector != null) {
                break;
            }
            RelativeBlockVector[] vectors = possibility.getControls();
            RelativeBlockVector otherControl = null;

            for (RelativeBlockVector vector : vectors) {
                BlockLocation tl = parent.modRelative(-vector.getRight(), -vector.getDepth(), -vector.getDistance(), modX, 1, modZ);

                if (gate == null) {
                    if (possibility.matches(tl, modX, modZ, true)) {
                        gate = possibility;
                        topleft = tl;

                        if (otherControl != null) {
                            buttonVector = otherControl;
                        }
                    }
                } else if (otherControl != null) {
                    buttonVector = vector;
                }

                otherControl = vector;
            }
        }

        if ((gate == null) || (buttonVector == null)) {
            Stargate.debug("createPortal", "Could not find matching gate layout");
            return null;
        }

        // If the player is trying to create a Bungee gate without permissions, drop out here
        // Do this after the gate layout check, in the least
        if (bungee) {
            if (!Stargate.enableBungee) {
                Stargate.sendMessage(player, Stargate.getString("bungeeDisabled"));
                return null;
            } else if (!Stargate.hasPerm(player, "stargate.admin.bungee")) {
                Stargate.sendMessage(player, Stargate.getString("bungeeDeny"));
                return null;
            } else if (destName.isEmpty() || network.isEmpty()) {
                Stargate.sendMessage(player, Stargate.getString("bungeeEmpty"));
                return null;
            }
        }

        // Debug
        Stargate.debug("createPortal", "h = " + hidden + " a = " + alwaysOn + " p = " + priv + " f = " + free + " b = " + backwards + " s = " + show + " n = " + noNetwork + " r = " + random + " u = " + bungee);

        if (!bungee && (network.length() < 1 || network.length() > 11)) {
            network = Stargate.getDefaultNetwork();
        }

        boolean deny = false;
        String denyMsg = "";

        // Check if the player can create gates on this network
        if (!bungee && !Stargate.canCreate(player, network)) {
            Stargate.debug("createPortal", "Player doesn't have create permissions on network. Trying personal");
            if (Stargate.canCreatePersonal(player)) {
                network = player.getName();
                if (network.length() > 11) network = network.substring(0, 11);
                Stargate.debug("createPortal", "Creating personal portal");
                Stargate.sendMessage(player, Stargate.getString("createPersonal"));
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
        if (!deny && !Stargate.canCreateGate(player, gateName)) {
            Stargate.debug("createPortal", "Player does not have access to gate layout");
            deny = true;
            denyMsg = Stargate.getString("createGateDeny");
        }

        // Check if the user can create gates to this world.
        if (!bungee && !deny && destName.length() > 0) {
            Portal p = getByName(destName, network);
            if (p != null) {
                String world = p.getWorld().getName();
                if (!Stargate.canAccessWorld(player, world)) {
                    Stargate.debug("canCreate", "Player does not have access to destination world");
                    deny = true;
                    denyMsg = Stargate.getString("createWorldDeny");
                }
            }
        }

        // Bleh, gotta check to make sure none of this gate belongs to another gate. Boo slow.
        for (RelativeBlockVector v : gate.getBorder()) {
            BlockLocation b = topleft.modRelative(v.getRight(), v.getDepth(), v.getDistance(), modX, 1, modZ);
            if (getByBlock(b.getBlock()) != null) {
                Stargate.debug("createPortal", "Gate conflicts with existing gate");
                Stargate.sendMessage(player, Stargate.getString("createConflict"));
                return null;
            }
        }

        BlockLocation button = null;
        Portal portal;
        portal = new Portal(topleft, modX, modZ, rotX, id, button, destName, name, false, network, gate, player.getUniqueId(), player.getName(), hidden, alwaysOn, priv, free, backwards, show, noNetwork, random, bungee);

        int cost = Stargate.getCreateCost(player, gate);

        // Call StargateCreateEvent
        StargateCreateEvent cEvent = new StargateCreateEvent(player, portal, event.getLines(), deny, denyMsg, cost);
        Stargate.server.getPluginManager().callEvent(cEvent);
        if (cEvent.isCancelled()) {
            return null;
        }
        if (cEvent.getDeny()) {
            Stargate.sendMessage(player, cEvent.getDenyReason());
            return null;
        }

        cost = cEvent.getCost();

        // Name & Network can be changed in the event, so do these checks here.
        if (portal.getName().length() < 1 || portal.getName().length() > 11) {
            Stargate.debug("createPortal", "Name length error");
            Stargate.sendMessage(player, Stargate.getString("createNameLength"));
            return null;
        }

        // Don't do network checks for bungee gates
        if (portal.isBungee()) {
            if (bungeePortals.get(portal.getName().toLowerCase()) != null) {
                Stargate.debug("createPortal::Bungee", "Gate Exists");
                Stargate.sendMessage(player, Stargate.getString("createExists"));
                return null;
            }
        } else {
            if (getByName(portal.getName(), portal.getNetwork()) != null) {
                Stargate.debug("createPortal", "Name Error");
                Stargate.sendMessage(player, Stargate.getString("createExists"));
                return null;
            }

            // Check if there are too many gates in this network
            List<String> netList = allPortalsNet.get(portal.getNetwork().toLowerCase());
            if (Stargate.maxGates > 0 && netList != null && netList.size() >= Stargate.maxGates) {
                Stargate.sendMessage(player, Stargate.getString("createFull"));
                return null;
            }
        }

        if (cost > 0) {
            if (!Stargate.chargePlayer(player, cost)) {
                EconomyHelper.sendInsufficientFundsMessage(name, player, cost);
                Stargate.debug("createPortal", "Insufficient Funds");
                return null;
            }
            EconomyHelper.sendDeductMessage(name, player, cost);
        }

        // No button on an always-open gate.
        if (!alwaysOn) {
            button = topleft.modRelative(buttonVector.getRight(), buttonVector.getDepth(), buttonVector.getDistance() + 1, modX, 1, modZ);
            Directional buttondata = (Directional) Bukkit.createBlockData(gate.getButton());
            buttondata.setFacing(buttonfacing);
            button.getBlock().setBlockData(buttondata);
            portal.setButton(button);
        }

        register(portal);
        portal.drawSign();
        // Open always on gate
        if (portal.isRandom() || portal.isBungee()) {
            portal.open(true);
        } else if (portal.isAlwaysOn()) {
            Portal dest = getByName(destName, portal.getNetwork());
            if (dest != null) {
                portal.open(true);
                dest.drawSign();
            }
            // Set the inside of the gate to its closed material
        } else {
            for (BlockLocation inside : portal.getEntrances()) {
                inside.setType(portal.getGate().getPortalBlockClosed());
            }
        }

        // Don't do network stuff for bungee gates
        if (!portal.isBungee()) {
            // Open any always on gate pointing at this gate
            for (String originName : allPortalsNet.get(portal.getNetwork().toLowerCase())) {
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
     * Gets a portal given its name
     * @param name <p>The name of the portal</p>
     * @param network <p>The network the portal is connected to</p>
     * @return <p>The portal with the given name or null</p>
     */
    public static Portal getByName(String name, String network) {
        if (!lookupNamesNet.containsKey(network.toLowerCase())) {
            return null;
        }
        return lookupNamesNet.get(network.toLowerCase()).get(name.toLowerCase());

    }

    /**
     * Gets a portal given its entrance
     * @param location <p>The location of the portal's entrance</p>
     * @return <p>The portal at the given location</p>
     */
    public static Portal getByEntrance(Location location) {
        return lookupEntrances.get(new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ()));
    }

    /**
     * Gets a portal given its entrance
     * @param block <p>The block at the portal's entrance</p>
     * @return <p>The portal at the given block's location</p>
     */
    public static Portal getByEntrance(Block block) {
        return lookupEntrances.get(new BlockLocation(block));
    }

    /**
     * Gets a portal given a location adjacent to its entrance
     * @param loc <p>A location adjacent to the portal's entrance</p>
     * @return <p>The portal adjacent to the given location</p>
     */
    public static Portal getByAdjacentEntrance(Location loc) {
        int centerX = loc.getBlockX();
        int centerY = loc.getBlockY();
        int centerZ = loc.getBlockZ();
        World world = loc.getWorld();
        Portal portal = lookupEntrances.get(new BlockLocation(world, centerX, centerY, centerZ));
        if (portal != null) {
            return portal;
        }
        portal = lookupEntrances.get(new BlockLocation(world, centerX + 1, centerY, centerZ));
        if (portal != null) {
            return portal;
        }
        portal = lookupEntrances.get(new BlockLocation(world, centerX - 1, centerY, centerZ));
        if (portal != null) {
            return portal;
        }
        portal = lookupEntrances.get(new BlockLocation(world, centerX, centerY, centerZ + 1));
        if (portal != null) {
            return portal;
        }
        portal = lookupEntrances.get(new BlockLocation(world, centerX, centerY, centerZ - 1));
        if (portal != null) {
            return portal;
        }
        return null;
    }

    /**
     * Gets a portal given its control block (the block type used for the sign and button)
     * @param block <p>The portal's control block</p>
     * @return <p>The gate with the given control block</p>
     */
    public static Portal getByControl(Block block) {
        return lookupControls.get(new BlockLocation(block));
    }

    /**
     * Gets a portal given a block
     * @param block <p>One of the loaded lookup blocks</p>
     * @return <p>The portal corresponding to the block</p>
     */
    public static Portal getByBlock(Block block) {
        return lookupBlocks.get(new BlockLocation(block));
    }

    /**
     * Gets a bungee gate given its name
     * @param name <p>The name of the bungee gate to get</p>
     * @return <p>A bungee gate</p>
     */
    public static Portal getBungeeGate(String name) {
        return bungeePortals.get(name.toLowerCase());
    }

    /**
     * Saves all gates for the given world
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
                builder.append(portal.getRotX());
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
            Stargate.log.log(Level.SEVERE, "Exception while writing stargates to " + loc + ": " + e);
        }
    }

    /**
     * Clears all loaded gates and gate data
     */
    public static void clearGates() {
        lookupBlocks.clear();
        lookupNamesNet.clear();
        lookupEntrances.clear();
        lookupControls.clear();
        allPortals.clear();
        allPortalsNet.clear();
    }

    /**
     * Loads all gates for the given world
     * @param world <p>The world to load gates for</p>
     * @return <p>True if gates could be loaded</p>
     */
    public static boolean loadAllGates(World world) {
        String location = Stargate.getSaveLocation();

        File database = new File(location, world.getName() + ".db");

        if (database.exists()) {
            return loadGates(world, database);
        } else {
            Stargate.log.info(Stargate.getString("prefix") + "{" + world.getName() + "} No stargates for world ");
        }
        return false;
    }

    /**
     * Loads all the given gates
     * @param world <p>The world to load gates for</p>
     * @param database <p>The database file containing the gates</p>
     * @return <p>True if the gates were loaded successfully</p>
     */
    private static boolean loadGates(World world, File database) {
        int l = 0;
        try {
            Scanner scanner = new Scanner(database);
            while (scanner.hasNextLine()) {
                l++;
                String line = scanner.nextLine().trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                String[] portalData = line.split(":");
                if (portalData.length < 8) {
                    Stargate.log.info(Stargate.getString("prefix") + "Invalid line - " + l);
                    continue;
                }
                String name = portalData[0];
                BlockLocation sign = new BlockLocation(world, portalData[1]);
                BlockLocation button = (portalData[2].length() > 0) ? new BlockLocation(world, portalData[2]) : null;
                int modX = Integer.parseInt(portalData[3]);
                int modZ = Integer.parseInt(portalData[4]);
                float rotX = Float.parseFloat(portalData[5]);
                BlockLocation topLeft = new BlockLocation(world, portalData[6]);
                Gate gate = Gate.getGateByName(portalData[7]);
                if (gate == null) {
                    Stargate.log.info(Stargate.getString("prefix") + "Gate layout on line " + l + " does not exist [" + portalData[7] + "]");
                    continue;
                }

                String destination = (portalData.length > 8) ? portalData[8] : "";
                String network = (portalData.length > 9) ? portalData[9] : Stargate.getDefaultNetwork();
                if (network.isEmpty()) network = Stargate.getDefaultNetwork();
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
                Portal portal = new Portal(topLeft, modX, modZ, rotX, sign, button, destination, name, false, network,
                        gate, ownerUUID, ownerName);
                loadPortalOptions(portal, portalData);

                register(portal);
                portal.close(true);
            }
            scanner.close();

            // Open any always-on gates. Do this here as it should be more efficient than in the loop.
            TwoTuple<Integer, Integer> portalCounts = openAlwaysOpenGates();

            Stargate.log.info(Stargate.getString("prefix") + "{" + world.getName() + "} Loaded " + portalCounts.getSecondValue() + " stargates with " + portalCounts.getFirstValue() + " set as always-on");
            return true;
        } catch (Exception e) {
            Stargate.log.log(Level.SEVERE, "Exception while reading stargates from " + database.getName() + ": " + l);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Loads all portal options and updates the given portal
     * @param portal <p>The portal to apply the options to</p>
     * @param portalData <p>The string list containing all information about a portal</p>
     */
    private static void loadPortalOptions(Portal portal, String[] portalData) {
        boolean hidden = (portalData.length > 11) && portalData[11].equalsIgnoreCase("true");
        boolean alwaysOn = (portalData.length > 12) && portalData[12].equalsIgnoreCase("true");
        boolean isPrivate = (portalData.length > 13) && portalData[13].equalsIgnoreCase("true");
        boolean free = (portalData.length > 15) && portalData[15].equalsIgnoreCase("true");
        boolean backwards = (portalData.length > 16) && portalData[16].equalsIgnoreCase("true");
        boolean show = (portalData.length > 17) && portalData[17].equalsIgnoreCase("true");
        boolean noNetwork = (portalData.length > 18) && portalData[18].equalsIgnoreCase("true");
        boolean random = (portalData.length > 19) && portalData[19].equalsIgnoreCase("true");
        boolean bungee = (portalData.length > 20) && portalData[20].equalsIgnoreCase("true");
        portal.setHidden(hidden).setAlwaysOn(alwaysOn).setPrivate(isPrivate).setFree(free).setBungee(bungee);
        portal.setBackwards(backwards).setShown(show).setNoNetwork(noNetwork).setRandom(random);
    }

    /**
     * Opens all always open gates
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
     * @param portal <p>The portal of the star gate</p>
     */
    private static void destroyInvalidStarGate(Portal portal) {
        // DEBUG
        for (RelativeBlockVector control : portal.getGate().getControls()) {
            if (!portal.getBlockAt(control).getBlock().getType().equals(portal.getGate().getControlBlock())) {
                Stargate.debug("loadAllGates", "Control Block Type == " + portal.getBlockAt(control).getBlock().getType().name());
            }
        }
        PortalHandler.unregister(portal, false);
        Stargate.log.info(Stargate.getString("prefix") + "Destroying stargate at " + portal.toString());
    }

    /**
     * Closes all star gate portals
     */
    public static void closeAllGates() {
        Stargate.log.info("Closing all stargates.");
        for (Portal portal : allPortals) {
            if (portal != null) {
                portal.close(true);
            }
        }
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
}
