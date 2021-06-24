package net.TheDgtl.Stargate;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.block.BlockState;

public class SyncronousBlockPopulator implements Runnable{
	
	private final Queue<Action> blockPopulatorQueue = new LinkedList<>(); 

	@Override
	public void run() {
		long sTime = System.nanoTime();
		
		//Why is this done in legacy?
		while (!(blockPopulatorQueue.isEmpty()) && (System.nanoTime() - sTime < 25000000)) {
			Action action = blockPopulatorQueue.poll();
			action.run();
		}
	}
	
	public interface Action{
		public void run();
	}
	
	public class BlockSetAction implements Action{
		final private BlockState state;
		public BlockSetAction(BlockState state){
			blockPopulatorQueue.add(this);
			this.state = state;
		}
		@Override
		public void run() {
			state.update();
		}
	}
}
