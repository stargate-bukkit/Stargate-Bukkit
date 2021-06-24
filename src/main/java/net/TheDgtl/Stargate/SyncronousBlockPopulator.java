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
        while (System.nanoTime() - sTime < 25000000) {
        	if(blockPopulatorQueue.isEmpty()) {
        		return;
        	}
        	Action action = blockPopulatorQueue.poll();
        	action.execute();
        }
	}
	
	public interface Action{
		public void execute();
	}
	
	public class BlockSetAction implements Action{
		final private BlockState state;
		public BlockSetAction(BlockState state){
			blockPopulatorQueue.add(this);
			this.state = state;
		}
		@Override
		public void execute() {
			state.update();
		}
	}
}
