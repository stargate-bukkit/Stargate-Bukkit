package net.knarcraft.stargate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RelativeBlockVectorTest {

    @Test
    public void getTest() {
        RelativeBlockVector relativeBlockVector = new RelativeBlockVector(56, 44, 23);
        assertEquals(56, relativeBlockVector.getRight());
        assertEquals(44, relativeBlockVector.getDepth());
        assertEquals(23, relativeBlockVector.getDistance());
    }

    @Test
    public void equalsTest() {
        RelativeBlockVector vector1 = new RelativeBlockVector(56, 34, 76);
        RelativeBlockVector vector2 = new RelativeBlockVector(56, 34, 76);
        assertEquals(vector1, vector2);
    }

    @Test
    public void notEqualsTest() {
        RelativeBlockVector vector1 = new RelativeBlockVector(456, 78, 234);
        RelativeBlockVector vector2 = new RelativeBlockVector(56, 34, 76);
        assertNotEquals(vector1, vector2);
    }

}
