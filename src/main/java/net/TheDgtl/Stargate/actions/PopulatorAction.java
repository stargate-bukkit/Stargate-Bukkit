package net.TheDgtl.Stargate.actions;

public interface PopulatorAction{
	/**
	 * 
	 * @param forceEnd , finish the action instantly
	 */
	public void run(boolean forceEnd);
	public boolean isFinished();
}