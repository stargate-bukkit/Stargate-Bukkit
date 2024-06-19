package org.sgrewritten.stargate.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum Priority {
    /**
     * Lowest priority
     */
    LOWEST(0),
    /**
     * Low priority
     */
    LOW(1),
    /**
     * Normal priority
     */
    NORMAL(2),
    /**
     * High priority
     */
    HIGH(3),
    /**
     * Highest priority
     */
    HIGHEST(4);

    private final int priorityLevel;
    private static final ArrayList<Priority> highToLowPriority = new ArrayList<>();

    static {
        highToLowPriority.addAll(Arrays.asList(Priority.values()));
        highToLowPriority.sort(Comparator.comparingInt(aPriority -> -aPriority.priorityLevel));
    }

    Priority(int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    /**
     * @return <p>the priority value of the priority</p>
     */
    public int getPriorityValue() {
        return this.priorityLevel;
    }

    /**
     * @return <p>A list going from the highest to lowest priority level</p>
     */
    static List<Priority> getHighToLowPriority() {
        highToLowPriority.clone();
        return highToLowPriority;
    }

}
