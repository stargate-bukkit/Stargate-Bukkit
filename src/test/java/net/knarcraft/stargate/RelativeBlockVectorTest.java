package net.knarcraft.stargate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RelativeBlockVectorTest {

    @Test
    public void getTest() {
        RelativeBlockVector relativeBlockVector = new RelativeBlockVector(56, 44, 23);
        assertEquals(56, relativeBlockVector.getRight());
        assertEquals(44, relativeBlockVector.getDepth());
        assertEquals(23, relativeBlockVector.getDistance());
    }

}
