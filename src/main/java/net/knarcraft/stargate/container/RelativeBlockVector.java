package net.knarcraft.stargate.container;

/**
 * This stores a block location as a vector relative to a position
 *
 * <p>A relative block vector stores a vector relative to some origin. The origin in this plugin is usually the
 * top-left block of a gate (top-left when looking at the side with the sign). The right is therefore the distance
 * from the top-left corner towards the top-right corner. Down is the distance from the top-left corner towards the
 * bottom-left corner. Out is the distance outward from the gate.</p>
 */
public class RelativeBlockVector {

    private final int right;
    private final int down;
    private final int out;

    public enum Property {RIGHT, DOWN, OUT}

    /**
     * Instantiates a new relative block vector
     *
     * <p>Relative block vectors start from a top-left corner. A yaw is used to orient a relative block vector in the
     * "real world".
     * In terms of a gate layout, the origin is 0,0. Right is towards the end of the line. Down is to the
     * next line. Out is towards the observer.</p>
     *
     * @param right <p>The distance rightward relative to the origin</p>
     * @param down  <p>The distance downward relative to the origin</p>
     * @param out   <p>The distance outward relative to the origin</p>
     */
    public RelativeBlockVector(int right, int down, int out) {
        this.right = right;
        this.down = down;
        this.out = out;
    }

    /**
     * Adds a value to one of the properties of this relative block vector
     *
     * @param propertyToAddTo <p>The property to add to</p>
     * @param valueToAdd      <p>The value to add to the property (negative to move in the opposite direction)</p>
     * @return <p>A new relative block vector with the property altered</p>
     */
    public RelativeBlockVector addToVector(Property propertyToAddTo, int valueToAdd) {
        switch (propertyToAddTo) {
            case RIGHT:
                return new RelativeBlockVector(this.right + valueToAdd, this.down, this.out);
            case DOWN:
                return new RelativeBlockVector(this.right, this.down + valueToAdd, this.out);
            case OUT:
                return new RelativeBlockVector(this.right, this.down, this.out + valueToAdd);
            default:
                throw new IllegalArgumentException("Invalid relative block vector property given");
        }
    }

    /**
     * Gets a relative block vector which is this inverted (pointing in the opposite direction)
     *
     * @return <p>This vector, but inverted</p>
     */
    public RelativeBlockVector invert() {
        return new RelativeBlockVector(-this.right, -this.down, -this.out);
    }

    /**
     * Gets the distance to the right relative to the origin
     *
     * @return <p>The distance to the right relative to the origin</p>
     */
    public int getRight() {
        return right;
    }

    /**
     * Gets the distance downward relative to the origin
     *
     * @return <p>The distance downward relative to the origin</p>
     */
    public int getDown() {
        return down;
    }

    /**
     * Gets the distance outward relative to the origin
     *
     * @return <p>The distance outward relative to the origin</p>
     */
    public int getOut() {
        return out;
    }

    @Override
    public String toString() {
        return String.format("(right = %d, down = %d, out = %d)", right, down, out);
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
        return this.right == otherVector.right && this.down == otherVector.down &&
                this.out == otherVector.out;
    }

}
