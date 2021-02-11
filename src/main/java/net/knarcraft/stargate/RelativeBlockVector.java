package net.knarcraft.stargate;

/**
 * This stores a block location as a vector in an alternate coordinate system
 *
 * <p></p>
 */
public class RelativeBlockVector {

    private int right;
    private int depth;
    private int distance;

    /**
     * Instantiates a new relative block vector
     * @param right <p>The x coordinate in the gate description</p>
     * @param depth <p>The y coordinate in the gate description</p>
     * @param distance <p></p>
     */
    public RelativeBlockVector(int right, int depth, int distance) {
        this.right = right;
        this.depth = depth;
        this.distance = distance;
    }

    public int getRight() {
        return right;
    }

    public int getDepth() {
        return depth;
    }

    public int getDistance() {
        return distance;
    }

}
