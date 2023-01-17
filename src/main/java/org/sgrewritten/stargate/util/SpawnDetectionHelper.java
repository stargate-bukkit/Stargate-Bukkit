package org.sgrewritten.stargate.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.gate.Gate;
import org.sgrewritten.stargate.gate.GateFormat;
import org.sgrewritten.stargate.property.NonLegacyMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A helper class for deciding if a given gate is interfering with the spawn protection area
 *
 * @author Kristian Knarvik (EpicKnarvik97)
 */
public final class SpawnDetectionHelper {

    private SpawnDetectionHelper() {

    }

    /**
     * Checks whether the given gate has any block inside the spawn protection area
     *
     * @param gate         <p>The gate to check for interference</p>
     * @param someLocation <p>Some location of the gate (used to get the world)</p>
     * @return <p>True if the gate has at least one block inside the spawn protection area</p>
     */
    public static boolean isInterferingWithSpawnProtection(Gate gate, Location someLocation) {
        Location spawnLocation = Objects.requireNonNull(someLocation.getWorld()).getSpawnLocation();
        int spawnRadius = Bukkit.getSpawnRadius();

        Location spawnMinLocation = new Location(spawnLocation.getWorld(), spawnLocation.getBlockX() - spawnRadius,
                getWorldMinHeight(someLocation.getWorld()), spawnLocation.getBlockZ() - spawnRadius);
        Location spawnMaxLocation = new Location(spawnLocation.getWorld(), spawnLocation.getBlockX() + spawnRadius,
                getWorldMaxHeight(someLocation.getWorld()), spawnLocation.getBlockZ() + spawnRadius);
        GateFormat format = gate.getFormat();
        //TODO: Once 3D gates are implemented, set depth to the depth of the Stargate
        Location gateMinLocation = getStargateMinCorner(gate, format.getHeight(), format.getWidth(), 0);
        Location gateMaxLocation = getStargateMaxCorner(gate, format.getWidth(), 0);

        //Check if the stargate's frame is intersecting with the spawn area
        if (areIntersecting(gateMinLocation, gateMaxLocation, spawnMinLocation, spawnMaxLocation)) {
            return true;
        }
        //Check if any portal positions are intersecting with the spawn area
        //TODO: Might want to change this to test all control blocks if we allow add-ons to add new controls after 
        // creation
        for (GatePosition position : gate.getPortalPositions()) {
            if (areIntersecting(gate.getLocation(position.getPositionLocation()), spawnMinLocation, spawnMaxLocation)) {
                return true;
            }
        }
        return false;
    }


    private static int getWorldMaxHeight(World world) {
        if (NonLegacyMethod.GET_WORLD_MAX.isImplemented()) {
            return world.getMaxHeight();
        }
        return 255;
    }

    private static int getWorldMinHeight(World world) {
        if (NonLegacyMethod.GET_WORLD_MIN.isImplemented()) {
            return world.getMinHeight();
        }
        return 0;
    }

    /**
     * Checks if the given location is intersecting with the hit-box defined by the given location pair
     *
     * @param point       <p>The point to check for intersection</p>
     * @param locationMin <p>The minimum corner of the hit-box to check against</p>
     * @param locationMax <p>The maximum corner of the hit-box to check against</p>
     * @return <p>True if the point is intersecting with the hit-box</p>
     */
    private static boolean areIntersecting(Location point, Location locationMin, Location locationMax) {
        return (point.getBlockX() >= locationMin.getBlockX() && point.getBlockX() <= locationMax.getBlockX()) &&
                (point.getBlockY() >= locationMin.getBlockY() && point.getBlockY() <= locationMax.getBlockY()) &&
                (point.getBlockZ() >= locationMin.getBlockZ() && point.getBlockZ() <= locationMax.getBlockZ());
    }

    /**
     * Checks if the hit-boxes defined by the two location pairs are intersecting
     *
     * @param location1min <p>The minimum corner of the first hit-box</p>
     * @param location1max <p>The maximum corner of the first hit-box</p>
     * @param location2min <p>The minimum corner of the second hit-box</p>
     * @param location2max <p>The maximum corner of the second hit-box</p>
     * @return <p>True if the two hit-boxes are intersecting</p>
     */
    private static boolean areIntersecting(Location location1min, Location location1max, Location location2min,
                                           Location location2max) {
        return (location1min.getBlockX() <= location2max.getBlockX() &&
                location1max.getBlockX() >= location2min.getBlockX()) &&
                (location1min.getBlockY() <= location2max.getBlockY() &&
                        location1max.getBlockY() >= location2min.getBlockY()) &&
                (location1min.getBlockZ() <= location2max.getBlockZ() &&
                        location1max.getBlockZ() >= location2min.getBlockZ());
    }

    /**
     * Gets the minimum corner for the given gate
     *
     * <p>The depth is the amount outwards or inwards to go. This is generally intended for 3D Stargate support.</p>
     *
     * @param gate           <p>The gate to find the minimum corner of</p>
     * @param stargateHeight <p>The height of the gate</p>
     * @param stargateWidth  <p>The width of the gate</p>
     * @param depth          <p>The depth of the gate</p>
     * @return <p>The minimum corner of the gate</p>
     */
    private static Location getStargateMinCorner(Gate gate, int stargateHeight, int stargateWidth, int depth) {
        Location minLocation = null;
        List<Vector> stargateCorners = new ArrayList<>();
        stargateCorners.add(new Vector(0, -stargateHeight, 0));
        stargateCorners.add(new Vector(0, -stargateHeight, -stargateWidth));
        if (depth != 0) {
            stargateCorners.add(new Vector(depth, -stargateHeight, 0));
            stargateCorners.add(new Vector(depth, -stargateHeight, -stargateWidth));
        }

        for (Vector vector : stargateCorners) {
            Location realLocation = gate.getLocation(vector);
            if (minLocation == null ||
                    realLocation.getBlockX() < minLocation.getBlockX() ||
                    realLocation.getBlockZ() < minLocation.getBlockZ()) {
                minLocation = realLocation;
            }
        }
        return minLocation;
    }

    /**
     * Gets the maximum corner of the given gate
     *
     * <p>The depth is the amount outwards or inwards to go. This is generally intended for 3D Stargate support.</p>
     *
     * @param gate          <p>The gate to find the maximum corner of</p>
     * @param stargateWidth <p>The width of the gate</p>
     * @param depth         <p>The depth of the gate</p>
     * @return <p>The maximum corner of the gate</p>
     */
    private static Location getStargateMaxCorner(Gate gate, int stargateWidth, int depth) {
        Location maxLocation = null;
        List<Vector> stargateCorners = new ArrayList<>();
        stargateCorners.add(new Vector(0, 0, 0));
        stargateCorners.add(new Vector(0, 0, -stargateWidth));
        if (depth != 0) {
            stargateCorners.add(new Vector(depth, 0, 0));
            stargateCorners.add(new Vector(depth, 0, -stargateWidth));
        }

        for (Vector vector : stargateCorners) {
            Location realLocation = gate.getLocation(vector);
            if (maxLocation == null ||
                    realLocation.getBlockX() > maxLocation.getBlockX() ||
                    realLocation.getBlockZ() > maxLocation.getBlockZ()) {
                maxLocation = realLocation;
            }
        }
        return maxLocation;
    }

}
