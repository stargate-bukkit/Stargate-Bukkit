package org.sgrewritten.stargate.api;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public enum Priority {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4);

    private final int priority;
    private static final ArrayList<Priority> highToLowPriority = new ArrayList<>();
    static{
        for(Priority aPriority : Priority.values()) {
            highToLowPriority.add(aPriority);
        }
        highToLowPriority.sort(Comparator.comparingInt((aPriority) -> -aPriority.priority));
    }

    Priority(int priority){
        this.priority = priority;
    }

    public int getPriorityValue(){
        return this.priority;
    }

    static List<Priority> getHighToLowPriority(){
        highToLowPriority.clone();
        return highToLowPriority;
    }

}
