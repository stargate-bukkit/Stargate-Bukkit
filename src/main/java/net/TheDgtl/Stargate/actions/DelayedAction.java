package net.TheDgtl.Stargate.actions;

import java.util.function.Supplier;

/**
 * An action which should be delayed by a given amount
 */
public class DelayedAction implements ForcibleAction {

    private int delay;
    private final Supplier<Boolean> action;

    /**
     * Will run a task after {@link DelayedAction#run(boolean)} has been triggered a specific amount of time
     *
     * @param delay  in ticks
     * @param action the action that will run upon completion
     */
    public DelayedAction(int delay, Supplier<Boolean> action) {
        this.delay = delay;
        this.action = action;
    }

    @Override
    public void run(boolean forceEnd) {
        delay--;
        if (forceEnd) {
            delay = 0;
        }

        if (delay <= 0) {
            action.get();
        }
    }

    @Override
    public void run() {
        delay--;
        if (delay <= 0) {
            action.get();
        }
    }

    @Override
    public boolean isFinished() {
        return (delay <= 0);
    }

    @Override
    public String toString() {
        return "[" + delay + "](" + action.toString() + ")";
    }
}