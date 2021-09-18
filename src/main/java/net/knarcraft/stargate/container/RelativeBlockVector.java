package net.knarcraft.stargate.container;

/**
 * This stores a block location as a vector relative to a position
 *
 * <p>A relative block vector stores a vector relative to some origin. The origin in this plugin is usually the
 * top-left block of a gate (top-left when looking at the side with the sign). The right is therefore the distance
 * from the top-left corner towards the top-right corner. Depth is the distance from the top-left corner to the
 * bottom-left corner. Distance is the distance outward from the gate.</p>
 */
public class RelativeBlockVector {

    private final int right;
    private final int depth;
    private final int distance;

    /**
     * Instantiates a new relative block vector
     *
     * @param right    <p>The distance to the right relative to the origin</p>
     * @param depth    <p>The distance downward relative to the origin</p>
     * @param distance <p>The distance outward relative to the origin</p>
     */
    public RelativeBlockVector(int right, int depth, int distance) {
        this.right = right;
        this.depth = depth;
        this.distance = distance;
    }

    /**
     * Gets the distance to the right relative to the origin
     *
     * @return The distance to the right relative to the origin
     */
    public int getRight() {
        return right;
    }

    /**
     * Gets the distance downward relative to the origin
     *
     * @return The distance downward relative to the origin
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Gets the distance outward relative to the origin
     *
     * @return The distance outward relative to the origin
     */
    public int getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return String.format("(right = %d, depth = %d, distance = %d)", right, depth, distance);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        RelativeBlockVector otherVector = (RelativeBlockVector) other;
        return this.right == otherVector.right && this.depth == otherVector.depth &&
                this.distance == otherVector.distance;
    }

}
