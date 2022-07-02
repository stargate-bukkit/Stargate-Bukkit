package net.TheDgtl.Stargate.util.portal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.RealPortal;

public class TeleportHelper {
    static private int CONE_LENGTH = 7;
    static private int MAXIMUM_CONE_EXTENSION = 5;
    
    public static Location findViableSpawnLocation(Entity entity, RealPortal destinationPortal,
            BlockFace destinationFacing) {

        List<Location> irisLocations = new ArrayList<>();
        destinationPortal.getGate().getLocations(GateStructureType.IRIS)
                .forEach((blockLocation) -> irisLocations.add(blockLocation.getLocation()));
        ;
        int width = (int) Math.ceil(entity.getWidth());
        int height = (int) Math.ceil(entity.getHeight());
        Vector offsett = width % 2 != 0 ? new Vector(0.5,0,0.5) : new Vector();
        
        
        //skip first layer as that was the origin of issue https://github.com/stargate-rewritten/Stargate-Bukkit/issues/231
        List<Location> coneLocations = getDirectionallConeLayer(destinationFacing, irisLocations, 0);
        for (int i = 1; i <= CONE_LENGTH; i++) {
            coneLocations = getDirectionallConeLayer(destinationFacing, coneLocations, 0);
            for(Location possibleSpawnLocation : coneLocations) {
                Location modifiedPossibleSpawnLocation = possibleSpawnLocation.clone().add(offsett);
                if(isViableSpawnLocation(width,height,modifiedPossibleSpawnLocation)) {
                    return modifiedPossibleSpawnLocation;
                }
            }
        }
        return null;
    }
    
    
    private static List<Location> getDirectionallConeLayer(BlockFace destinationFacing, List<Location> locations, int recursionNumber) {
        Vector forward = destinationFacing.getDirection(); //Something like that probably exists
        Vector left = forward.rotateAroundY(Math.PI/2);
        Vector right = forward.rotateAroundY(-Math.PI/2);

        Set<Location> relativeLocations = new HashSet<>();
        for (Location location : locations) {
            //Add the three relevant relative locations
            relativeLocations.add(location.clone().add(forward));
            //Stop after 4 recursion to prevent way too big search areas
            if (recursionNumber < MAXIMUM_CONE_EXTENSION) {
              relativeLocations.add(location.clone().add(forward).add(left));
              relativeLocations.add(location.clone().add(forward).add(right));
              //Run this if 3-dimensional search is necesary
              Set<Location> relativeLocationsCopy = new HashSet<>(relativeLocations);
              for (Location relativeLocation : relativeLocationsCopy) {
                relativeLocations.add(relativeLocation.clone().add(new Vector(0, 1, 0)));
                relativeLocations.add(relativeLocation.clone().add(new Vector(0, -1, 0)));
              }
            }
        }
        List<Location> relativeLocationsList = new ArrayList<>(relativeLocations.size());
        relativeLocationsList.addAll(relativeLocations);
        //TODO: Sort by closeness
        return relativeLocationsList;
    }
    
    /**
     * Check whether an entity will be safe when spawning into specified location
     * @param width <p> The width of the entity </p>
     * @param height <p> The height of the entity </p>
     * @param center <p> The entity's coordinates </p>
     * @return
     */
    private static boolean isViableSpawnLocation(int width, int height, Location center) {
        Location corner = center.clone().subtract(width/2,0,width/2);
        
        for(Location occupiedLocation : getOccupiedLocations(width,height,corner)) {
            if(occupiedLocation.getBlock().getType().isSolid()) {
                return false;
            }
        }
        
        for(Location floorLocation : getFloorLocations(width,corner)) {
            if(floorLocation.getBlock().getType().isBlock()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate the locations an entity will occupy based from a corner location of the entity
     * 
     * @param width <p> The width of the entity </p>
     * @param height <p> The height of the entity </p>
     * @param corner <p> A corner of the entities hitbox where y, x, z coordinates are the lowest </p>
     * @return <p> A list of the locations occupied by the entity </p>
     */
    private static List<Location> getOccupiedLocations(int width, int height, Location corner){
        List<Location> occupiedLocations = new ArrayList<>();
        for(int ix = 0; ix <= width; ix++){
            for(int iy = 0; iy <= height; iy++){
                for (int iz = 0; iz <= width; iz++) {
                    occupiedLocations.add(corner.clone().add(new BlockVector(ix,iy,iz)));
                }
            }
          }
        return occupiedLocations;
    }
    
    /**
     * Calculate the locations where an entity would have a supported floor
     * 
     * @param width <p> The width of the entity </p>
     * @param corner <p> A corner of the entities hitbox where y, x, z coordinates are the lowest </p>
     * @return <p> A list of the locations occupied by the entity </p>
     */
    private static List<Location> getFloorLocations(int width, Location corner){
        List<Location> floorLocations = new ArrayList<>();
        for(int ix = 0; ix < width; ix++) {
            for(int iz = 0; iz < width; iz++) {
                floorLocations.add(corner.clone().add(new BlockVector(ix,-1,iz)));
            }
        }
        return floorLocations;
    }
}
