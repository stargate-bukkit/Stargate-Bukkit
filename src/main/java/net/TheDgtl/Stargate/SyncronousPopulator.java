package net.TheDgtl.Stargate;

import java.util.ArrayList;
import java.util.Iterator;

import net.TheDgtl.Stargate.actions.PopulatorAction;

import java.util.logging.Level;

public class SyncronousPopulator implements Runnable{
	
	private final ArrayList<PopulatorAction> populatorQueue = new ArrayList<>();
	private final ArrayList<PopulatorAction> bungeePopulatorQueue = new ArrayList<>();
	private final ArrayList<PopulatorAction> addList = new ArrayList<>();
	private final ArrayList<PopulatorAction> bungeeAddList = new ArrayList<>();
	
	@Override
	public void run() {
		boolean forceEnd = false;
		
		populatorQueue.addAll(addList);
		addList.clear();
		cycleQueue(forceEnd,populatorQueue);
		
		if(!Stargate.knowsServerName)
			return;
		
		bungeePopulatorQueue.addAll(bungeeAddList);
		bungeeAddList.clear();
		cycleQueue(forceEnd,bungeePopulatorQueue);
	}
	
	private void cycleQueue(boolean forceEnd, ArrayList<PopulatorAction> queue) {
		Iterator<PopulatorAction> it = queue.iterator();
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
		addAction(action,false);
	}
	
	public void addAction(PopulatorAction action, boolean isBungee) {
		if(action != null)
			Stargate.log(Level.FINEST,"Adding action " + action.toString());
		
		
		ArrayList<PopulatorAction> addList = isBungee ? this.bungeeAddList : this.addList;
		addList.add(action);
	}

	public void forceDoAllTasks() {
		boolean forceEnd = true; 
		cycleQueue(forceEnd,populatorQueue);
		if(!Stargate.knowsServerName)
			return;
		cycleQueue(forceEnd,bungeePopulatorQueue);
	}
}
