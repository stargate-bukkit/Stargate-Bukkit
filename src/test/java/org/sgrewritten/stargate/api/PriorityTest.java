package org.sgrewritten.stargate.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PriorityTest {

    @Test
    void priorityOrderingTest() {
        int latestPriority = Integer.MAX_VALUE;
        for (Priority priority : Priority.getHighToLowPriority()) {
            Assertions.assertTrue(latestPriority >= priority.getPriorityValue());
            latestPriority = priority.getPriorityValue();
        }
    }
}
