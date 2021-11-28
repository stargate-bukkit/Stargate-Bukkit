package net.TheDgtl.Stargate.actions;

/**
 * Does a task every time it gets triggered. If the condition is met, remove from queue
 *
 * @author Thorin
 */
public abstract class ConditionalRepeatedTask implements PopulatorAction {
    /**
     *
     */
    private PopulatorAction action;
    private boolean isFinished = false;

    public ConditionalRepeatedTask(PopulatorAction action) {
        this.action = action;
    }

    @Override
    public void run(boolean forceEnd) {
        if (isCondition() || forceEnd) {
            isFinished = true;
            return;
        }

        action.run(forceEnd);
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public String toString() {
        return "[RepeatedCond](" + action.toString() + ")";
    }

    /**
     * If this returns true, then the repeated task will stop.
     *
     * @return
     */
    public abstract boolean isCondition();

}