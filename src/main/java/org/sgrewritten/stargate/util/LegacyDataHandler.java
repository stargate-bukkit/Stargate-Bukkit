package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LegacyDataHandler {


    private static Map<PortalFlag, Integer> LEGACY_FLAG_INDICES;

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
        if (LEGACY_FLAG_INDICES == null) {
            loadFlagIndices();
        }

        Set<PortalFlag> flags = new HashSet<>();
        for (PortalFlag flag : LEGACY_FLAG_INDICES.keySet()) {
            int position = LEGACY_FLAG_INDICES.get(flag);
            if (splitLine.length > position && splitLine[position].equalsIgnoreCase("true")) {
                flags.add(flag);
            }
        }

        return flags;
    }

    /**
     * Loads the map containing all known legacy flag indices
     */
    private static void loadFlagIndices() {
        LEGACY_FLAG_INDICES = new HashMap<>();
        LEGACY_FLAG_INDICES.put(PortalFlag.HIDDEN, 11);
        LEGACY_FLAG_INDICES.put(PortalFlag.ALWAYS_ON, 12);
        LEGACY_FLAG_INDICES.put(PortalFlag.PRIVATE, 13);
        LEGACY_FLAG_INDICES.put(PortalFlag.FREE, 15);
        LEGACY_FLAG_INDICES.put(PortalFlag.BACKWARDS, 16);
        LEGACY_FLAG_INDICES.put(PortalFlag.FORCE_SHOW, 17);
        LEGACY_FLAG_INDICES.put(PortalFlag.HIDE_NETWORK, 18);
        LEGACY_FLAG_INDICES.put(PortalFlag.RANDOM, 19);
        LEGACY_FLAG_INDICES.put(PortalFlag.BUNGEE, 20);
        LEGACY_FLAG_INDICES.put(PortalFlag.SILENT, 21);
        LEGACY_FLAG_INDICES.put(PortalFlag.NO_SIGN, 22);
    }
    

    
    public static String findConfigKey(String[] possibleKeys, Map<String,Object> oldConfig) {
        for(String possibleKey : possibleKeys) {
            if (oldConfig.get(possibleKey) != null) {
                return possibleKey;
            }
        }
        return null;
    }
}
