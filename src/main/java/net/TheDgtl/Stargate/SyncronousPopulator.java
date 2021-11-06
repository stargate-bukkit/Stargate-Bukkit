package net.TheDgtl.Stargate;

import java.util.ArrayList;
import java.util.Iterator;

import net.TheDgtl.Stargate.actions.PopulatorAction;

import java.util.logging.Level;

public class SyncronousPopulator implements Runnable{
	
	private final ArrayList<PopulatorAction> blockPopulatorQueue = new ArrayList<>(); 
	private final ArrayList<PopulatorAction> addList = new ArrayList<>();
	@Override
	public void run() {
		boolean forceEnd;
		blockPopulatorQueue.addAll(addList);
		addList.clear();
		cycleQueue(forceEnd = false);
	}
	
	private void cycleQueue(boolean forceEnd) {
		Iterator<PopulatorAction> it = blockPopulatorQueue.iterator();
		long sTime = System.nanoTime();
		// Why is this done in legacy?
		while (it.hasNext() && (System.nanoTime() - sTime < 25000000)) {
			PopulatorAction action = it.next();
			action.run(forceEnd);
			if(action.isFinished()) {
				it.remove();
			}
		}
	}
	
	public void addAction(PopulatorAction action) {
		if(action != null)
			Stargate.log(Level.FINEST,"Adding action " + action.toString());
		addList.add(action);
	}

	public void forceDoAllTasks() {
		boolean forceEnd = true;
		cycleQueue(forceEnd);
	}
}
