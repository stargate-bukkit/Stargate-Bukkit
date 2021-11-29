package net.TheDgtl.Stargate.actions;

/**
 * An action which can be forced
 */
public interface ForcibleAction extends SimpleAction {

    /**
     * Executes the action
     *
     * @param forceEnd <p>Whether to force the action to end</p>
     */
    void run(boolean forceEnd);

}
