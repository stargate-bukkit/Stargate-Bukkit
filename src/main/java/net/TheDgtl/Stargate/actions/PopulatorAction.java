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
     * @param forceEnd <p>Unused???</p>
     */
    void run(boolean forceEnd);

    /**
     * Whether the populator action has finished
     *
     * @return <p>True if the populator action has finished</p>
     */
    boolean isFinished();
}