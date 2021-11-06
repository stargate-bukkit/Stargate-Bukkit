package net.TheDgtl.Stargate.actions;

import net.TheDgtl.Stargate.SyncronousPopulator;

/**
 * Does a task every time time it gets triggered. If the condition is met, remove from queue
 * @author Thorin
 *
 */
public abstract class ConditionallRepeatedTask implements PopulatorAction {
	/**
	 * 
	 */
	private final SyncronousPopulator syncronousPopulator;
	private PopulatorAction action;
	private boolean isFinished = false;

	public ConditionallRepeatedTask(SyncronousPopulator syncronousPopulator, PopulatorAction action) {
		this.syncronousPopulator = syncronousPopulator;
		this.action = action;
		this.syncronousPopulator.addAction(this);
	}

	@Override
	public void run(boolean forceEnd) {
		if (isCondition() || forceEnd) {
			isFinished = true;
			return;
		}

		action.run(forceEnd);
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}

	@Override
	public String toString() {
		return "[RepeatedCond](" + action.toString() + ")";
	}

	/**
	 * If this returns true, then the repeated task will stop.
	 * 
	 * @return
	 */
	public abstract boolean isCondition();

}