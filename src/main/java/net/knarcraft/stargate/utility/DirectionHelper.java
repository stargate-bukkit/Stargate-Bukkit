package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.BlockLocation;
import net.knarcraft.stargate.RelativeBlockVector;
import org.bukkit.Location;

/**
 * This class helps with direction-dependent (modX, modZ) calculations
 */
public class DirectionHelper {

    /**
     * Gets the block at a relative block vector location
     *
     * @param vector <p>The relative block vector</p>
     * @return <p>The block at the given relative position</p>
     */
    public static BlockLocation getBlockAt(BlockLocation topLeft, RelativeBlockVector vector, int modX, int modZ) {
        return topLeft.modRelative(vector.getRight(), vector.getDepth(), vector.getDistance(), modX, 1, modZ);
    }

    /**
     * Adds a relative block vector to a location, accounting for direction
     * @param location <p>The location to adjust</p>
     * @param right <p>The amount of blocks to the right to adjust</p>
     * @param depth <p>The amount of blocks upward to adjust</p>
     * @param distance <p>The distance outward to adjust</p>
     * @param modX <p>The x modifier to use</p>
     * @param modZ <p>The z modifier to use</p>
     * @return <p>The altered location</p>
     */
    public static Location adjustLocation(Location location, double right, double depth, double distance, int modX,
                                          int modZ) {
        return location.add(-right * modX + distance * modZ, depth, -right * modZ + -distance * modX);
    }

}
