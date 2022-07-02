package net.TheDgtl.Stargate.util.portal;

import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.RealPortal;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A helper class for teleportation-related checks
 */
public class TeleportationHelper {

    private static final int CONE_LENGTH = 7;
    private static final int MAXIMUM_CONE_EXTENSION = 5;

    public static Location findViableSpawnLocation(Entity entity, RealPortal destinationPortal) {
        Vector forward = destinationPortal.getGate().getFacing().getDirection();
        Vector left = forward.rotateAroundY(Math.PI / 2);
        Vector right = forward.rotateAroundY(-Math.PI / 2);
        Vector up = new Vector(0, 1, 0);
        Vector down = new Vector(0, -1, 0);

        List<Location> irisLocations = new ArrayList<>();
        //Add locations of all iris blocks in the Stargate
        //TODO: Limit checking of iris blocks far from the ground
        destinationPortal.getGate().getLocations(GateStructureType.IRIS).forEach(
                (blockLocation) -> irisLocations.add(blockLocation.getLocation()));
        //TODO: Add the blocks beneath the iris as well
        int width = (int) Math.ceil(entity.getWidth());
        int height = (int) Math.ceil(entity.getHeight());
        Vector centerOffset = width % 2 != 0 ? new Vector(0.5, 0, 0.5) : new Vector();

        //skip first layer as that was the origin of issue https://github.com/stargate-rewritten/Stargate-Bukkit/issues/231
        List<Location> coneLocations = getDirectionalConeLayer(irisLocations, forward, left, right, up, down, 0);
        for (int coneHeight = 1; coneHeight <= CONE_LENGTH; coneHeight++) {
            coneLocations = getDirectionalConeLayer(coneLocations, forward, left, right, up, down, coneHeight);
            for (Location possibleSpawnLocation : coneLocations) {
                Location modifiedPossibleSpawnLocation = possibleSpawnLocation.clone().add(centerOffset);
                if (isViableSpawnLocation(width, height, modifiedPossibleSpawnLocation)) {
                    return modifiedPossibleSpawnLocation;
                }
            }
        }
        return null;
    }

    /**
     * Gets the next layer in a cone going out from the given set of location in the given direction (outwards)
     *
     * @param locations       <p>The locations in the previous cone layer</p>
     * @param outwards        <p>The direction to place the next player of the cone in</p>
     * @param left            <p>The positive or negative direction on the axis (x or z) opposite from forwards</p>
     * @param right           <p>The positive or negative direction on the axis (x or z) opposite from forwards</p>
     * @param up              <p>The upwards direction along the y-axis</p>
     * @param down            <p>The downwards direction along the y-axis</p>
     * @param recursionNumber <p>The number of times this method has been run in the current call chain</p>
     * @return <p>The locations part of the next cone layer</p>
     */
    private static List<Location> getDirectionalConeLayer(List<Location> locations, Vector outwards, Vector left,
                                                          Vector right, Vector up, Vector down, int recursionNumber) {
        Set<Location> relativeLocations = new HashSet<>();
        for (Location location : locations) {
            /* Store relative locations in a new hashset to make sure the upwards and downwards variations are only 
            created for the three relative locations just generated */
            Set<Location> newRelativeLocations = new HashSet<>();
            //Add the three relevant relative locations
            newRelativeLocations.add(location.clone().add(outwards));
            //Stop after the specified number of recursions to prevent way too big search areas
            if (recursionNumber < MAXIMUM_CONE_EXTENSION) {
                newRelativeLocations.add(location.clone().add(outwards).add(left));
                newRelativeLocations.add(location.clone().add(outwards).add(right));
                //Check upwards and downwards for a 3-dimensional search
                Set<Location> relativeLocationsCopy = new HashSet<>(newRelativeLocations);
                for (Location relativeLocation : relativeLocationsCopy) {
                    newRelativeLocations.add(relativeLocation.clone().add(up));
                    newRelativeLocations.add(relativeLocation.clone().add(down));
                }
            }
            relativeLocations.addAll(newRelativeLocations);
        }
        List<Location> relativeLocationsList = new ArrayList<>(relativeLocations.size());
        relativeLocationsList.addAll(relativeLocations);
        //TODO: Sort by closeness (smallest change in x, y and z directions relative to Stargate center)
        return relativeLocationsList;
    }

    /**
     * Check whether an entity will be safe when spawning into specified location
     *
     * @param width  <p>The width of the entity</p>
     * @param height <p>The height of the entity</p>
     * @param center <p>The entity's coordinates</p>
     * @return <p>True if the entity can be safely teleported to the given location</p>
     */
    private static boolean isViableSpawnLocation(int width, int height, Location center) {
        Location corner = center.clone().subtract(width / 2.0, 0, width / 2.0);

        for (Location occupiedLocation : getOccupiedLocations(width, height, corner)) {
            if (occupiedLocation.getBlock().getType().isSolid()) {
                return false;
            }
        }

        for (Location floorLocation : getFloorLocations(width, corner)) {
            if (floorLocation.getBlock().getType().isBlock()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate the locations an entity will occupy based from a corner location of the entity
     *
     * @param width  <p>The width of the entity</p>
     * @param height <p>The height of the entity</p>
     * @param corner <p>A corner of the entities hit-box where y, x, z coordinates are the lowest</p>
     * @return <p>A list of the locations occupied by the entity</p>
     */
    private static List<Location> getOccupiedLocations(int width, int height, Location corner) {
        List<Location> occupiedLocations = new ArrayList<>();
        for (int ix = 0; ix <= width; ix++) {
            for (int iy = 0; iy <= height; iy++) {
                for (int iz = 0; iz <= width; iz++) {
                    occupiedLocations.add(corner.clone().add(new BlockVector(ix, iy, iz)));
                }
            }
        }
        return occupiedLocations;
    }

    /**
     * Calculate the locations where an entity would have a supported floor
     *
     * @param width  <p>The width of the entity</p>
     * @param corner <p>A corner of the entities hit-box where y, x, z coordinates are the lowest</p>
     * @return <p>A list of the locations occupied by the entity</p>
     */
    private static List<Location> getFloorLocations(int width, Location corner) {
        List<Location> floorLocations = new ArrayList<>();
        for (int ix = 0; ix < width; ix++) {
            for (int iz = 0; iz < width; iz++) {
                floorLocations.add(corner.clone().add(new BlockVector(ix, -1, iz)));
            }
        }
        return floorLocations;
    }
}
