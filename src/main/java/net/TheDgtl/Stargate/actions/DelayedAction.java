package net.TheDgtl.Stargate.actions;

public class DelayedAction implements PopulatorAction {
    /**
     *
     */
    int delay;
    PopulatorAction action;

    /**
     * Will run a task after {@link DelayedAction#run(boolean)} has been triggered a specific amount of time
     *
     * @param delay               in ticks
     * @param action              the action that will run upon completion
     * @param syncronousPopulator
     */
    public DelayedAction(int delay, PopulatorAction action) {
        this.delay = delay;
        this.action = action;
    }

    @Override
    public void run(boolean forceEnd) {
        delay--;
        if (forceEnd)
            delay = 0;

        if (delay <= 0) {
            action.run(forceEnd);
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