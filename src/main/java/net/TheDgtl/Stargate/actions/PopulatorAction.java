package net.TheDgtl.Stargate.actions;

/**
 * A action to be triggered in the {@link SyncronousPopulator} class
 * @author Thorin
 */
public interface PopulatorAction{
	/**
	 * 
	 * @param forceEnd , finish the action instantly
	 */
	public void run(boolean forceEnd);
	public boolean isFinished();
}