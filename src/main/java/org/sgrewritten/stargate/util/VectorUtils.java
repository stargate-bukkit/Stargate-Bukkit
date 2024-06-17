package org.sgrewritten.stargate.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;

public class VectorUtils {

    private VectorUtils(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get all relative blockvectors that represent a position that represent a block adjacent to (0 0 0)
     *
     * @return <p> A list of all adjacent blocks to the (0 0 0) relative location </p>
     */
    public static BlockVector[] getAdjacentRelativePositions() {
        BlockVector x = new BlockVector(1, 0, 0);
        BlockVector y = new BlockVector(0, 1, 0);
        BlockVector z = new BlockVector(0, 0, 1);
        BlockVector minusX = new BlockVector(-1, 0, 0);
        BlockVector minusY = new BlockVector(0, -1, 0);
        BlockVector minusZ = new BlockVector(0, 0, -1);

        return new BlockVector[]{x, y, z, minusX, minusY, minusZ};
    }

    /**
     * Calculates the relative angle difference between two block faces
     *
     * @param originFace      <p>The block face the origin portal is pointing towards</p>
     * @param destinationFace <p>The block face the destination portal is pointing towards</p>
     * @return <p>The angle difference between the two block faces</p>
     */
    public static double calculateAngleDifference(BlockFace originFace, BlockFace destinationFace) {
        if (originFace != null) {
            Vector originGateDirection = originFace.getDirection();
            return directionalAngleOperator(originGateDirection, destinationFace.getDirection());
        } else {
            return -directionalAngleOperator(BlockFace.EAST.getDirection(), destinationFace.getDirection());
        }
    }

    /**
     * Gets the angle between two vectors
     *
     * <p>The {@link Vector#angle(Vector)} function is not directional, meaning if you exchange position with vectors,
     * there will be no difference in angle. The behaviour that is needed in some portal methods is for the angle to
     * change sign if the vectors change places.
     * <p>
     * NOTE: ONLY ACCOUNTS FOR Y AXIS ROTATIONS</p>
     *
     * @param vector1 <p>The first vector</p>
     * @param vector2 <p>The second vector</p>
     * @return <p>The angle between the two vectors</p>
     */
    public static double directionalAngleOperator(Vector vector1, Vector vector2) {
        return Math.atan2(vector1.clone().crossProduct(vector2).getY(), vector1.dot(vector2));
    }

    /**
     * Convert between two different coordinate systems. In this case from format space to minecraft space
     * @param topLeft <p>Origo of system in format space</p>
     * @param vectorOperation <p>Rotations and z axis flips to convert between coordinate systems</p>
     * @param vector <p>The vector to convert</p>
     * @return <p>A location in minecraft space</p>
     */
    public static Location getLocation(Location topLeft, VectorOperation vectorOperation, Vector vector){
        return topLeft.clone().add(vectorOperation.performToRealSpaceOperation(vector));
    }

    /**
     * Convert between two different coordinate systems. In this case from minecraft space to format space
     * @param topLeft <p>The origo of format space</p>
     * @param vectorOperation <p>Rotations and z axis flips to convert between coordinate systems</p>
     * @param location <p>A location to convert from</p>
     * @return <p>A vector in format space</p>
     */
    public static Vector getFormatSpaceCoordinate(Location topLeft, VectorOperation vectorOperation, Location location){
        return vectorOperation.performToAbstractSpaceOperation(topLeft.clone().subtract(location).toVector());
    }
}
