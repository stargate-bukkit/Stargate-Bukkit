package net.TheDgtl.Stargate.actions;

import net.TheDgtl.Stargate.Stargate;

/**
 * An action that only will be performed after this server knows its server id on the bungee network 
 * @author Thorin
 *
 */
public class BungeeDelayedAction implements PopulatorAction{
	
	private PopulatorAction action;
	boolean isFinished = false;
	
	public BungeeDelayedAction(PopulatorAction action) {
		this.action = action;
	}
	
	@Override
	public void run(boolean forceEnd) {
		if(!Stargate.knowsServerName && !forceEnd) {
			return;
		}
		isFinished = true;
		action.run(forceEnd);
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}
	
	@Override
	public String toString() {
		return "BungeeDelayedAction(" + action.toString() + ")";
	}
	
}