package net.knarcraft.stargate.container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This stores a block location as a vector relative to a position
 *
 * <p>A relative block vector stores a vector relative to some origin. The origin in this plugin is usually the
 * top-left block of a gate (top-left when looking at the side with the sign). The right is therefore the distance
 * from the top-left corner towards the top-right corner. Down is the distance from the top-left corner towards the
 * bottom-left corner. Out is the distance outward from the gate.</p>
 *
 * <p>Relative block vectors start from a top-left corner. A yaw is used to orient a relative block vector in the
 * "real world". In terms of a gate layout, the origin is 0,0. Right is towards the end of the line. Down is to the
 * next line. Out is towards the observer.</p>
 *
 * @param right <p>The distance rightward relative to the origin</p>
 * @param down  <p>The distance downward relative to the origin</p>
 * @param out   <p>The distance outward relative to the origin</p>
 */
public record RelativeBlockVector(int right, int down, int out) {

    /**
     * Adds the given value to this relative block vector's "right" property
     *
     * @param valueToAdd <p>The value to add</p>
     * @return <p>The new resulting vector</p>
     */
    @NotNull
    public RelativeBlockVector addRight(int valueToAdd) {
        return new RelativeBlockVector(this.right + valueToAdd, this.down, this.out);
    }

    /**
     * Adds the given value to this relative block vector's "down" property
     *
     * @param valueToAdd <p>The value to add</p>
     * @return <p>The new resulting vector</p>
     */
    @NotNull
    public RelativeBlockVector addDown(int valueToAdd) {
        return new RelativeBlockVector(this.right, this.down + valueToAdd, this.out);
    }

    /**
     * Adds the given value to this relative block vector's "out" property
     *
     * @param valueToAdd <p>The value to add</p>
     * @return <p>The new resulting vector</p>
     */
    @NotNull
    public RelativeBlockVector addOut(int valueToAdd) {
        return new RelativeBlockVector(this.right, this.down, this.out + valueToAdd);
    }

    /**
     * Gets a relative block vector which is this inverted (pointing in the opposite direction)
     *
     * @return <p>This vector, but inverted</p>
     */
    @NotNull
    public RelativeBlockVector invert() {
        return new RelativeBlockVector(-this.right, -this.down, -this.out);
    }

    @Override
    @NotNull
    public String toString() {
        return String.format("(right = %d, down = %d, out = %d)", right, down, out);
    }

    @Override
    public boolean equals(@Nullable Object other) {
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
