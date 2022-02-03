package net.TheDgtl.Stargate.refactoring.retcons;

import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.StargateFactory;
import net.TheDgtl.Stargate.network.portal.FixedPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.PortalPosition;
import net.TheDgtl.Stargate.network.portal.PositionType;
import net.TheDgtl.Stargate.util.FileHelper;
import net.TheDgtl.Stargate.util.PortalCreationHelper;
import net.TheDgtl.Stargate.vectorlogic.SimpleVectorOperation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A helper tool for loading legacy portals from legacy storage
 */
public class LegacyPortalStorageLoader {

    private final static Map<PortalFlag, Integer> LEGACY_FLAGS_POS_MAP = new EnumMap<>(PortalFlag.class);

    static {
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.HIDDEN, 11);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.ALWAYS_ON, 12);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.PRIVATE, 13);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.FREE, 15);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.BACKWARDS, 16);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.FORCE_SHOW, 17);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.HIDE_NETWORK, 18);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.RANDOM, 19);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.BUNGEE, 20);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.SILENT, 21);
        LEGACY_FLAGS_POS_MAP.put(PortalFlag.NO_SIGN, 22);
    }

    /**
     * Loads legacy portals in .db files from the given folder
     *
     * @param portalSaveLocation <p>The folder containing legacy portals</p>
     * @param server             <p>The server this plugin is running on</p>
     * @param factory            <p>The stargate factory to save portals to</p>
     * @param logger             <p>The logger used for logging</p>
     * @return <p>The list of loaded and saved portals</p>
     * @throws IOException               <p>If unable to read one or more .db files</p>
     * @throws InvalidStructureException <p>If an encountered portal's structure is invalid</p>
     * @throws NameErrorException        <p>If the name of a portal is invalid</p>
     */
    public static List<Portal> loadPortalsFromStorage(String portalSaveLocation, Server server, StargateFactory factory,
                                                      StargateLogger logger) throws IOException, InvalidStructureException, NameErrorException {
        List<Portal> portals = new ArrayList<>();
        File dir = new File(portalSaveLocation);
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".db")) : new File[0];
        if (files == null) {
            return null;
        }

        for (File file : files) {
            String worldName = file.getName().replaceAll("\\.db$", "");
            BufferedReader reader = FileHelper.getBufferedReader(file);
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                portals.add(readPortal(line, server.getWorld(worldName), factory, logger));

                line = reader.readLine();
            }
        }
        return portals;
    }

    /**
     * Reads a portal line from a legacy portal .db file
     *
     * @param line    <p>The line to read</p>
     * @param world   <p>The world the portal belongs to</p>
     * @param factory <p>The portal factory to save the portal to</p>
     * @param logger  <p>The logger used for logging</p>
     * @return <p>The loaded portal</p>
     * @throws InvalidStructureException <p>If the portal's structure is invalid</p>
     * @throws NameErrorException        <p>If the name of the portal is invalid</p>
     */
    static private Portal readPortal(String line, World world, StargateFactory factory,
                                     StargateLogger logger) throws InvalidStructureException, NameErrorException {
        String[] portalProperties = line.split(":");
        String name = portalProperties[0];
        Location signLocation = loadLocation(world, portalProperties[1]);
        Location buttonLocation = loadLocation(world, portalProperties[2]);
        int modX = Integer.parseInt(portalProperties[3]);
        int modZ = Integer.parseInt(portalProperties[4]);
        logger.logMessage(Level.FINEST, String.format("modX = %d, modZ = %d", modX, modZ));
        BlockFace facing = getFacing(modX, modZ);
        if (facing == null) {
            facing = getFacing(Double.parseDouble(portalProperties[5]));
        }

        Location topLeft = loadLocation(world, portalProperties[6]);
        String gateFormatName = portalProperties[7];
        String destination = (portalProperties.length > 8) ? portalProperties[8] : "";
        String networkName = (portalProperties.length > 9) ? portalProperties[9] : Settings.getString(Setting.DEFAULT_NETWORK);
        String ownerString = (portalProperties.length > 10) ? portalProperties[10] : "";
        UUID ownerUUID = getPlayerUUID(ownerString);
        Set<PortalFlag> flags = parseFlags(portalProperties);
        if (destination == null || destination.trim().isEmpty()) {
            flags.add(PortalFlag.NETWORKED);
        }
        try {
            factory.createNetwork(networkName, flags);
        } catch (NameErrorException ignored) {
        }
        if (topLeft == null) {
            throw new InvalidStructureException();
        }

        //TODO: Need to make sure whether this way of calculating the relative location works, and fix it if it doesn't
        List<PortalPosition> portalPositions = new ArrayList<>();
        PortalPosition signPosition = getRelativePosition(topLeft, signLocation, facing, logger);
        if (signPosition != null) {
            portalPositions.add(signPosition);
        }
        PortalPosition buttonPosition = getRelativePosition(topLeft, buttonLocation, facing, logger);
        if (buttonPosition != null) {
            portalPositions.add(buttonPosition);
        }
        Network network = factory.getNetwork(networkName, flags.contains(PortalFlag.FANCY_INTER_SERVER));

        GateFormat format = GateFormat.getFormat(gateFormatName);
        Gate gate = new Gate(topLeft, facing, false, format, portalPositions, logger);
        Portal portal = PortalCreationHelper.createPortal(network, name, destination, networkName, flags, gate, ownerUUID);

        //Add the portal to its network and store it to the database
        logger.logMessage(Level.FINE, "Saved portal %s in network %s from old storage ");
        network.addPortal(portal, true);

        return portal;
    }

    /**
     * Gets the portal position from a location, relative to the top-left location
     *
     * @param topLeft       <p>The top-left location of the portal the block position belongs to</p>
     * @param blockPosition <p>The position of the block to get the portal position for</p>
     * @param facing        <p>The direction the portal in question is facing</p>
     * @param logger        <p>The logger to used for debug logging</p>
     * @return <p>The portal position relative to the top-left location</p>
     * @throws InvalidStructureException <p>If the portal's direction is invalid</p>
     */
    private static PortalPosition getRelativePosition(Location topLeft, Location blockPosition, BlockFace facing,
                                                      StargateLogger logger) throws InvalidStructureException {
        if (blockPosition != null) {
            Location relativeBlockPosition = blockPosition.subtract(topLeft);
            BlockVector relativeBlockVector = new BlockVector(relativeBlockPosition.getBlockX(),
                    relativeBlockPosition.getBlockY(), relativeBlockPosition.getBlockZ());
            SimpleVectorOperation operation = new SimpleVectorOperation(facing);
            BlockVector normalizedVector = operation.performInverseOperation(relativeBlockVector);
            logger.logMessage(Level.FINER, "Calculated portal position: " + normalizedVector +
                    " from top-left: " + topLeft + ", block position: " + blockPosition + " and facing: " + facing);
            return new PortalPosition(PositionType.SIGN, normalizedVector);
        } else {
            return null;
        }
    }

    /**
     * Loads a location from the given input string
     *
     * @param world <p>The world the location belongs to</p>
     * @param input <p>The input string to parse to coordinates</p>
     * @return <p>The loaded location</p>
     */
    private static Location loadLocation(World world, String input) {
        if (input.trim().isEmpty()) {
            return null;
        }
        String[] coordinates = input.split(",");
        return new Location(
                world,
                Double.parseDouble(coordinates[0]),
                Double.parseDouble(coordinates[1]),
                Double.parseDouble(coordinates[2]));
    }

    /**
     * Gets the UUID from an owner string
     *
     * <p>An owner string might be a UUID or a username.</p>
     *
     * @param ownerString <p>The owner string to get a UUID from</p>
     * @return <p>A UUID</p>
     */
    @SuppressWarnings("deprecation")
    private static UUID getPlayerUUID(String ownerString) {
        if (ownerString.length() > 16) {
            return UUID.fromString(ownerString);
        } else {
            return Bukkit.getOfflinePlayer(ownerString).getUniqueId();
        }
    }

    /**
     * Parses the flags found in a portal file
     *
     * @param splitLine <p>The split portal save line</p>
     * @return <p>The parsed flags</p>
     */
    private static Set<PortalFlag> parseFlags(String[] splitLine) {
        Set<PortalFlag> flags = EnumSet.noneOf(PortalFlag.class);
        for (PortalFlag flag : LEGACY_FLAGS_POS_MAP.keySet()) {
            int position = LEGACY_FLAGS_POS_MAP.get(flag);
            if (splitLine.length > position && splitLine[position].equalsIgnoreCase("true")) {
                flags.add(flag);
            }
        }

        return flags;
    }

    /**
     * Gets the facing direction from the given x and z values
     *
     * @param modX <p>The x modifier used in legacy</p>
     * @param modZ <p>The z modifier used in legacy</p>
     * @return <p>The corresponding block face, or null if on a fork without modX, modZ</p>
     */
    private static BlockFace getFacing(int modX, int modZ) {
        if (modX < 0) {
            return BlockFace.WEST;
        } else if (modX > 0) {
            return BlockFace.EAST;
        } else if (modZ < 0) {
            return BlockFace.NORTH;
        } else if (modZ > 0) {
            return BlockFace.EAST;
        } else {
            return null;
        }
    }

    /**
     * Gets the facing direction from the given rotation
     *
     * @param rotation <p>The rotation to get the direction from</p>
     * @return <p>The corresponding block face</p>
     */
    private static BlockFace getFacing(double rotation) {
        return getFacing(-(int) Math.round(Math.cos(rotation)), -(int) Math.round(Math.sin(rotation)));
    }

}
