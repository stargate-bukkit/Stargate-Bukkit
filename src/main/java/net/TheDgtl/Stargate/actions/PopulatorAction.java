package net.TheDgtl.Stargate.actions;

/**
 * An action to be triggered in the {@link net.TheDgtl.Stargate.SynchronousPopulator} class
 *
 * @author Thorin
 */
public interface PopulatorAction {

    /**
     * Executes the population, and performs the populator action
     *
     * @param forceEnd <p>Whether to force the action to be performed</p>
     */
    void run(boolean forceEnd);

    /**
     * Whether the populator action has finished
     *
     * @return <p>True if the populator action has finished</p>
     */
    boolean isFinished();
}