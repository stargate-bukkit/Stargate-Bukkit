package net.TheDgtl.Stargate.actions;

import net.TheDgtl.Stargate.SyncronousPopulator;

public class DelayedAction implements PopulatorAction{
	/**
	 * 
	 */
	private final SyncronousPopulator syncronousPopulator;
	int delay;
	PopulatorAction action;
	
	/**
	 * @param delay in ticks
	 * @param action
	 * @param syncronousPopulator TODO
	 */
	public DelayedAction(SyncronousPopulator syncronousPopulator, int delay, PopulatorAction action){
		this.syncronousPopulator = syncronousPopulator;
		this.delay = delay;
		this.action = action;
		this.syncronousPopulator.addAction(this);
	}

	@Override
	public void run(boolean forceEnd) {
		delay--;
		if(forceEnd)
			delay = 0;
		
		if(delay <= 0) {
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