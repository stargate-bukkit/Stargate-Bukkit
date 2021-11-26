package net.TheDgtl.Stargate.actions;

/**
 * An action to be triggered in the {@link net.TheDgtl.Stargate.SynchronousPopulator} class
 *
 * @author Thorin
 */
public interface PopulatorAction {
    /**
     * @param forceEnd , finish the action instantly
     */
    void run(boolean forceEnd);

    boolean isFinished();
}