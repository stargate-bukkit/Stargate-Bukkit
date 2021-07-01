package net.TheDgtl.Stargate;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.block.BlockState;
import java.util.logging.Level;

public class SyncronousPopulator implements Runnable{
	
	private final ArrayList<Action> blockPopulatorQueue = new ArrayList<>(); 
	private final ArrayList<Action> addList = new ArrayList<>();
	@Override
	public void run() {
		long sTime = System.nanoTime();

		blockPopulatorQueue.addAll(addList);
		addList.clear();
		Iterator<Action> it = blockPopulatorQueue.iterator();
		// Why is this done in legacy?
		while (it.hasNext() && (System.nanoTime() - sTime < 25000000)) {
			Action action = it.next();
			action.run();
			if(action.isFinished()) {
				it.remove();
			}
		}
	}
	
	public void addAction(Action action) {
		if(action != null)
			Stargate.log(Level.FINEST,"Adding action " + action.toString());
		addList.add(action);
	}
	
	public interface Action{
		public void run();
		public boolean isFinished();
	}
	
	public class BlockSetAction implements Action{
		final private BlockState state;
		final private boolean force;
		public BlockSetAction(BlockState state, boolean force){
			this.state = state;
			this.force = force;
			addAction(this);
		}
		@Override
		public void run() {
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
	
	public class DelayedAction implements Action{
		int delay;
		Action action;
		
		/**
		 * 
		 * @param delay in ticks
		 * @param action
		 */
		public DelayedAction(int delay, Action action){
			this.delay = delay;
			this.action = action;
			addAction(this);
		}

		@Override
		public void run() {
			delay--;
			if(delay <= 0) {
				action.run();
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
}
