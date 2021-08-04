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
		boolean forceEnd = false;
		blockPopulatorQueue.addAll(addList);
		addList.clear();
		cycleQueue(forceEnd);
	}
	
	private void cycleQueue(boolean forceEnd) {
		Iterator<Action> it = blockPopulatorQueue.iterator();
		long sTime = System.nanoTime();
		// Why is this done in legacy?
		while (it.hasNext() && (System.nanoTime() - sTime < 25000000)) {
			Action action = it.next();
			action.run(forceEnd);
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

	public void forceDoAllTasks() {
		boolean forceEnd = true;
		cycleQueue(forceEnd);
	}
	
	public interface Action{
		/**
		 * 
		 * @param forceEnd , finish the action instantly
		 */
		public void run(boolean forceEnd);
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
	
	public class DelayedAction implements Action{
		int delay;
		Action action;
		
		/**
		 * @param delay in ticks
		 * @param action
		 */
		public DelayedAction(int delay, Action action){
			this.delay = delay;
			this.action = action;
			addAction(this);
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

	/**
	 * Does a task every time time it gets triggered. If the condition is met, remove from queue
	 * @author Thorin
	 *
	 */
	public abstract class ConditionallRepeatedTask implements Action {
		private Action action;
		private boolean isFinished = false;

		public ConditionallRepeatedTask(Action action) {
			this.action = action;
			addAction(this);
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
}
