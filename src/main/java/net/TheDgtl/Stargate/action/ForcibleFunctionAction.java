package net.TheDgtl.Stargate.action;

import java.util.function.Function;

/**
 * A function action which can be forced to stop
 */
public class ForcibleFunctionAction implements ForcibleAction {

    private final Function<Boolean, Boolean> function;
    private boolean finished = false;

    /**
     * Instantiates a new forcible supplier action
     *
     * @param function <p>The function to be run</p>
     */
    public ForcibleFunctionAction(Function<Boolean, Boolean> function) {
        this.function = function;
    }

    @Override
    public void run(boolean forceEnd) {
        this.finished = function.apply(forceEnd);
    }

    @Override
    public void run() {
        this.finished = function.apply(false);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
