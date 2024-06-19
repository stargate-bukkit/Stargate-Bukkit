package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.flag.StargateFlag;

import java.util.*;

public class LegacyDataHandler {

    private LegacyDataHandler() {
        throw new IllegalStateException("Utility class");
    }


    private static final Map<StargateFlag, Integer> LEGACY_FLAG_INDICES = loadFlagIndices();

    /**
     * Gets the facing direction from the given x and z values
     *
     * @param modX <p>The x modifier used in legacy</p>
     * @param modZ <p>The z modifier used in legacy</p>
     * @return <p>The corresponding block face, or null if on a fork without modX, modZ</p>
     */
    public static BlockFace getFacing(int modX, int modZ) {
        if (modZ < 0) {
            return BlockFace.WEST;
        } else if (modZ > 0) {
            return BlockFace.EAST;
        } else if (modX < 0) {
            return BlockFace.SOUTH;
        } else if (modX > 0) {
            return BlockFace.NORTH;
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
    public static BlockFace getFacing(double rotation) {
        double radians = Math.toRadians(rotation);
        return getFacing(-(int) Math.round(Math.cos(radians)), -(int) Math.round(Math.sin(radians)));
    }

    /**
     * Gets the UUID from an owner string
     *
     * <p>An owner string might be a UUID or a username.</p>
     *
     * @param ownerString <p>The owner string to get a UUID from</p>
     * @return <p>A UUID</p>
     */
    public static UUID getPlayerUUID(String ownerString) {
        if (ownerString.length() > 16) {
            return UUID.fromString(ownerString);
        } else {
            return Bukkit.getOfflinePlayer(ownerString).getUniqueId();
        }
    }

    /**
     * Loads a location from the given input string
     *
     * @param world <p>The world the location belongs to</p>
     * @param input <p>The input string to parse to coordinates</p>
     * @return <p>The loaded location</p>
     */
    public static Location loadLocation(World world, String input) {
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
     * Parses the flags found in a portal file
     *
     * @param splitLine <p>The split portal save line</p>
     * @return <p>The parsed flags</p>
     */
    public static Set<PortalFlag> parseFlags(String[] splitLine) {
        Set<PortalFlag> flags = new HashSet<>();
        for (Map.Entry<StargateFlag, Integer> entry : LEGACY_FLAG_INDICES.entrySet()) {
            int position = entry.getValue();
            if (splitLine.length > position && splitLine[position].equalsIgnoreCase("true")) {
                flags.add(entry.getKey());
            }
        }
        return flags;
    }

    /**
     * Loads the map containing all known legacy flag indices
     */
    private static Map<StargateFlag, Integer> loadFlagIndices() {
        Map<StargateFlag, Integer> output = new EnumMap<>(StargateFlag.class);
        output.put(StargateFlag.HIDDEN, 11);
        output.put(StargateFlag.ALWAYS_ON, 12);
        output.put(StargateFlag.PRIVATE, 13);
        output.put(StargateFlag.FREE, 15);
        output.put(StargateFlag.BACKWARDS, 16);
        output.put(StargateFlag.FORCE_SHOW, 17);
        output.put(StargateFlag.HIDE_NETWORK, 18);
        output.put(StargateFlag.RANDOM, 19);
        output.put(StargateFlag.LEGACY_INTERSERVER, 20);
        output.put(StargateFlag.SILENT, 21);
        output.put(StargateFlag.NO_SIGN, 22);
        return output;
    }

    /**
     * Utility method to find any of the given possible keys in the config
     *
     * @param possibleKeys <p>name variations of possible keys</p>
     * @param oldConfig    <p>The config to fetch data from</p>
     * @return <p>The key that had a value in the config, or null if none matched</p>
     */
    public static @Nullable String findConfigKey(String[] possibleKeys, Map<String, Object> oldConfig) {
        for (String possibleKey : possibleKeys) {
            if (oldConfig.get(possibleKey) != null) {
                return possibleKey;
            }
        }
        return null;
    }
}
