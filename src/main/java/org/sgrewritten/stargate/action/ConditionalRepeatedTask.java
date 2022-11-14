package org.sgrewritten.stargate.action;

import java.util.function.Supplier;

/**
 * Does a task every time it gets triggered. If the condition is met, remove from queue
 *
 * @author Thorin
 */
public class ConditionalRepeatedTask implements ForcibleAction {

    private final Supplier<Boolean> task;
    private final Supplier<Boolean> condition;
    private boolean isFinished = false;

    /**
     * Instantiates a new conditional repeated task
     *
     * @param task      <p>The task to repeat</p>
     * @param condition <p>The condition to keep running</p>
     */
    public ConditionalRepeatedTask(Supplier<Boolean> task, Supplier<Boolean> condition) {
        this.task = task;
        this.condition = condition;
    }

    @Override
    public void run(boolean forceEnd) {
        if (forceEnd) {
            isFinished = true;
        } else {
            run();
        }
    }

    @Override
    public void run() {
        if (isConditionFalse()) {
            isFinished = true;
        } else {
            task.get();
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public String toString() {
        return "[RepeatedCond](" + task.toString() + ")";
    }

    /**
     * Checks if the running condition is no longer true
     *
     * @return <p>Whether the running condition is no longer true</p>
     */
    private boolean isConditionFalse() {
        return !condition.get();
    }

}