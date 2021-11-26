package net.TheDgtl.Stargate.actions;

import org.bukkit.block.BlockState;

import net.TheDgtl.Stargate.SyncronousPopulator;

public class BlockSetAction implements PopulatorAction{
	/**
	 * 
	 */
	private final SyncronousPopulator syncronousPopulator;
	final private BlockState state;
	final private boolean force;
	public BlockSetAction(SyncronousPopulator syncronousPopulator, BlockState state, boolean force){
		this.syncronousPopulator = syncronousPopulator;
		this.state = state;
		this.force = force;
		this.syncronousPopulator.addAction(this);
	}
	@Override
	public void run(boolean forceEnd) {
		state.update(force);
	}
	@Override
	public boolean isFinished() {
		return true;
	}
	@Override
	public String toString() {
		return state.toString();
	}
}