package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * This class helps with direction-dependent (modX, modZ) calculations
 */
public final class DirectionHelper {

    private DirectionHelper() {

    }

    /**
     * Gets a yaw by comparing two locations
     *
     * <p>The yaw here is the direction an observer a the first location has to look to face the second location.
     * The yaw is only meant to be calculated for locations with equal x or equal z.</p>
     *
     * @param location1 <p>The first location, which works as the origin</p>
     * @param location2 <p>The second location, which the yaw will point at</p>
     * @return <p>The yaw</p>
     */
    public static float getYawFromLocationDifference(Location location1, Location location2) {
        Location difference = location1.clone().subtract(location2.clone());
        if (difference.getX() > 0) {
            return 90;
        } else if (difference.getX() < 0) {
            return 270;
        } else if (difference.getZ() > 0) {
            return 180;
        } else if (difference.getZ() < 0) {
            return 0;
        }
        throw new IllegalArgumentException("Locations given are equal or at the same x and y axis");
    }

    /**
     * Gets a block face given a yaw
     *
     * @param yaw <p>The yaw to use</p>
     * @return <p>The block face the yaw corresponds to</p>
     */
    public static BlockFace getBlockFaceFromYaw(double yaw) {
        //Make sure the yaw is between 0 and 360
        yaw = normalizeYaw(yaw);

        if (yaw == 0) {
            return BlockFace.SOUTH;
        } else if (yaw == 90) {
            return BlockFace.WEST;
        } else if (yaw == 180) {
            return BlockFace.NORTH;
        } else if (yaw == 270) {
            return BlockFace.EAST;
        } else {
            throw new IllegalArgumentException("Invalid yaw given");
        }
    }

    /**
     * Gets a direction vector given a yaw
     *
     * @param yaw <p>The yaw to use</p>
     * @return <p>The direction vector of the yaw</p>
     */
    public static Vector getDirectionVectorFromYaw(double yaw) {
        //Make sure the yaw is between 0 and 360
        yaw = normalizeYaw(yaw);

        if (yaw == 0) {
            return new Vector(0, 0, 1);
        } else if (yaw == 90) {
            return new Vector(-1, 0, 0);
        } else if (yaw == 180) {
            return new Vector(0, 0, -1);
        } else if (yaw == 270) {
            return new Vector(1, 0, 0);
        } else {
            throw new IllegalArgumentException("Invalid yaw given");
        }
    }

    /**
     * Gets a block location relative to another
     *
     * @param topLeft <p>The block location to start at (Usually the top-left block of a portal)</p>
     * @param vector  <p>The relative vector describing the relative location</p>
     * @param yaw     <p>The yaw pointing outwards from the portal</p>
     * @return <p>A block location relative to the given location</p>
     */
    public static BlockLocation getBlockAt(BlockLocation topLeft, RelativeBlockVector vector, double yaw) {
        return topLeft.getRelativeLocation(vector, yaw);
    }

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
     * Moves a location relatively
     *
     * @param location <p>The location to move</p>
     * @param right    <p>The amount to go right (When looking at the front of a portal)</p>
     * @param depth    <p>The amount to go downward (When looking at the front of a portal)</p>
     * @param distance <p>The amount to go outward (When looking a the front of a portal)</p>
     * @param yaw      <p>The yaw when looking directly outwards from a portal</p>
     * @return <p>A location relative to the given location</p>
     */
    public static Location moveLocation(Location location, double right, double depth, double distance, double yaw) {
        return location.add(getCoordinateVectorFromRelativeVector(right, depth, distance, yaw));
    }

    /**
     * Gets a vector in Minecraft's normal X,Y,Z-space from a relative block vector
     *
     * @param right    <p>The amount of right steps from the top-left origin</p>
     * @param depth    <p>The amount of downward steps from the top-left origin</p>
     * @param distance <p>The distance outward from the top-left origin</p>
     * @param yaw      <p>The yaw when looking directly outwards from a portal</p>
     * @return <p>A normal vector</p>
     */
    public static Vector getCoordinateVectorFromRelativeVector(double right, double depth, double distance, double yaw) {
        Vector distanceVector = DirectionHelper.getDirectionVectorFromYaw(yaw);
        distanceVector.multiply(distance);

        Vector rightVector = DirectionHelper.getDirectionVectorFromYaw(yaw - 90);
        rightVector.multiply(right);

        Vector depthVector = new Vector(0, -1, 0);
        depthVector.multiply(depth);

        return distanceVector.add(rightVector).add(depthVector);
    }

    /**
     * Normalizes a yaw to make it positive and no larger than 360 degrees
     *
     * @param yaw <p>The yaw to normalize</p>
     * @return <p>The normalized yaw</p>
     */
    private static double normalizeYaw(double yaw) {
        while (yaw < 0) {
            yaw += 360;
        }
        yaw = yaw % 360;
        return yaw;
    }

}
