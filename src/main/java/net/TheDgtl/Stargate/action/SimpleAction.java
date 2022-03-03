package net.TheDgtl.Stargate.action;

/**
 * A simple action which can be run, and reports if it's finished
 */
public interface SimpleAction {

    /**
     * Executes the action
     */
    void run();

    /**
     * Whether the action has finished
     *
     * @return <p>True if the action has finished</p>
     */
    boolean isFinished();

}
