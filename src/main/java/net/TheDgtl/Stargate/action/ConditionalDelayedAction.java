package net.TheDgtl.Stargate.action;

import java.util.function.Supplier;

/**
 * Performs a task only if the given condition is true
 *
 * @author Kristian
 */
public class ConditionalDelayedAction implements ForcibleAction {

    private final Supplier<Boolean> task;
    private final Supplier<Boolean> condition;
    private boolean finished = false;

    /**
     * Instantiates a new conditional delayed action
     *
     * @param task      <p>The task to perform</p>
     * @param condition <p>The condition required for execution</p>
     */
    public ConditionalDelayedAction(Supplier<Boolean> task, Supplier<Boolean> condition) {
        this.task = task;
        this.condition = condition;
    }

    @Override
    public void run(boolean forceEnd) {
        if (!forceEnd && !condition.get()) {
            return;
        }
        task.get();
        this.finished = true;
    }

    @Override
    public void run() {
        run(false);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

}
