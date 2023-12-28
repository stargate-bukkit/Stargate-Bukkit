package net.knarcraft.stargate.utility;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * This class helps with direction-related calculations
 */
public final class DirectionHelper {

    private DirectionHelper() {

    }

    /**
     * Gets a yaw by comparing two locations
     *
     * <p>The yaw here is the direction an observer a the first location has to look to face the second location.
     * The yaw is only meant to be calculated for locations where both have either the same x value or the same z value.
     * Equal locations, or locations with equal x and equal z will throw an exception.</p>
     *
     * @param location1 <p>The first location, which works as the origin</p>
     * @param location2 <p>The second location, which the yaw will point towards</p>
     * @return <p>The yaw pointing from the first location to the second location</p>
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
     * Gets a block face given a yaw value
     *
     * <p>The supplied yaw must be a value such that (yaw mod 90) = 0. If not, an exception is thrown.</p>
     *
     * @param yaw <p>The yaw value to convert</p>
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
            throw new IllegalArgumentException("Invalid yaw given. Yaw must be divisible by 90.");
        }
    }

    /**
     * Gets a direction vector given a yaw
     *
     * @param yaw <p>The yaw to convert to a direction vector</p>
     * @return <p>The direction vector pointing in the same direction as the yaw</p>
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
            throw new IllegalArgumentException(String.format("Invalid yaw %f given", yaw));
        }
    }

    /**
     * Moves a location by the given amounts
     *
     * <p>The right, down and out work the same as for the relative block vector. Looking a the front of a portal,
     * right goes rightwards, down goes downwards and out goes towards the observer.</p>
     *
     * @param location <p>The location to start at</p>
     * @param right    <p>The amount to go right</p>
     * @param down     <p>The amount to go downward</p>
     * @param out      <p>The amount to go outward</p>
     * @param yaw      <p>The yaw when looking directly outwards from a portal</p>
     * @return <p>A location relative to the given location</p>
     */
    public static Location moveLocation(Location location, double right, double down, double out, double yaw) {
        return location.add(getCoordinateVectorFromRelativeVector(right, down, out, yaw));
    }

    /**
     * Gets a vector in Minecraft's normal X,Y,Z-space from a relative block vector
     *
     * @param right <p>The amount of rightward steps from the top-left origin</p>
     * @param down  <p>The amount of downward steps from the top-left origin</p>
     * @param out   <p>The distance outward from the top-left origin</p>
     * @param yaw   <p>The yaw when looking directly outwards from a portal</p>
     * @return <p>A normal vector</p>
     */
    public static Vector getCoordinateVectorFromRelativeVector(double right, double down, double out, double yaw) {
        //Make sure the yaw is between 0 and 360
        yaw = normalizeYaw(yaw);

        if (yaw == 0) {
            //South
            return new Vector(right, -down, out);
        } else if (yaw == 90) {
            //West
            return new Vector(-out, -down, right);
        } else if (yaw == 180) {
            //North
            return new Vector(-right, -down, -out);
        } else if (yaw == 270) {
            //East
            return new Vector(out, -down, -right);
        } else {
            throw new IllegalArgumentException(String.format("Invalid yaw %f given", yaw));
        }
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
