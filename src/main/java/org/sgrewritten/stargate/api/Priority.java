package org.sgrewritten.stargate.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum Priority {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4);

    private final int priority;
    private static final ArrayList<Priority> highToLowPriority = new ArrayList<>();

    static {
        highToLowPriority.addAll(Arrays.asList(Priority.values()));
        highToLowPriority.sort(Comparator.comparingInt((aPriority) -> -aPriority.priority));
    }

    Priority(int priority) {
        this.priority = priority;
    }

    public int getPriorityValue() {
        return this.priority;
    }

    static List<Priority> getHighToLowPriority() {
        highToLowPriority.clone();
        return highToLowPriority;
    }

}
