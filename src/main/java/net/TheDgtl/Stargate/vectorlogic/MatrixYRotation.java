package net.TheDgtl.Stargate.vectorlogic;

import org.bukkit.util.Vector;

/**
 * A vector rotation limited to n*pi/2 radians rotations
 *
 * <p>Rotates around y-axis. Rounded to avoid Math.sin and Math.cos approximation errors.
 * DOES NOT PERMUTE INPUT VECTOR</p>
 *
 * @author Thorin
 */
public class MatrixYRotation {
    private final int sinTheta;
    private final int cosTheta;
    private final double rot;

    /**
     * Instantiates a new matrix y rotation
     *
     * @param rotation <p>A rotation given in radians</p>
     */
    public MatrixYRotation(double rotation) {
        sinTheta = (int) Math.round(Math.sin(rotation));
        cosTheta = (int) Math.round(Math.cos(rotation));
        this.rot = rotation;
    }

    /**
     * Performs the matrix y rotation on a vector
     *
     * @param vector <p>The vector to perform the rotation on</p>
     * @return <p>A new vector with the changes applied</p>
     */
    public Vector performOperation(Vector vector) {
        Vector newVector = new Vector();

        newVector.setX(sinTheta * vector.getZ() + cosTheta * vector.getX());
        newVector.setY(vector.getY());
        newVector.setZ(cosTheta * vector.getZ() - sinTheta * vector.getX());
        return newVector;
    }

    /**
     * Gets the inverse rotation of this rotation
     *
     * @return <p>The reverse rotation of this rotation</p>
     */
    public MatrixYRotation getInverse() {
        return new MatrixYRotation(-rot);
    }
    
}
