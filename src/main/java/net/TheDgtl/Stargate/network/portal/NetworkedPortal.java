package net.TheDgtl.Stargate.network.portal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.event.StargateActivateEvent;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;

public class NetworkedPortal extends Portal {
	/**
	 * 
	 */
	// used in networked portals
	static final private int NO_DESTI_SELECTED = -1;
	private int selectedDesti = NO_DESTI_SELECTED;
	private boolean isActive;
	private long activateTiming;
	
	private List<IPortal> destinations = new ArrayList<>();
	private static int ACTIVE_DELAY = 15; // seconds

	public NetworkedPortal(Network network, String name, Block sign, EnumSet<PortalFlag> flags)
			throws NoFormatFound, GateConflict, NameError {
		super(network, name, sign, flags);

		
		drawControll();
	}
	
	/**
	 * TODO have this individual for each player?
	 * @param action
	 * @param player 
	 */
	@Override
	public void onSignClick(Action action, Player actor) {
		activate(actor);
		if (destinations.size() < 1)
			return;
		if (selectedDesti == NO_DESTI_SELECTED || destinations.size() < 2) {
			selectedDesti = getNextDesti(1, -1);
		} else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
			int step = (action == Action.RIGHT_CLICK_BLOCK) ? -1 : 1;
			selectedDesti = getNextDesti(step, selectedDesti);
		}
		drawControll();
	}
	
	private String getDestinationName(int index) {
		return destinations.get(index).getName();
	}

	private String[] getDestinations(boolean isForce) {
		HashSet<String> tempPortalList = network.getAvailablePortals( (hasFlag(PortalFlag.FORCE_SHOW) || isForce), this);
		return tempPortalList.toArray(new String[0]);
	}

	public IPortal loadDestination() {
		if(selectedDesti == NO_DESTI_SELECTED)
			return null;
		return destinations.get(selectedDesti);
	}

	/**
	 * A method which allows selecting a index x steps away from a reference index
	 * without having to bother with index out of bounds stuff. If the index is out
	 * of bounds, it will just start counting from 0
	 * 
	 * @param step
	 * @param initialDesti
	 * @return
	 */
	private int getNextDesti(int step, int initialDesti) {
		int destiLength = destinations.size();
		// Avoid infinite recursion if this is the only gate available
		if (destiLength < 1) {
			return -1;
		}
		int temp = initialDesti + destiLength;
		return (temp + step) % destiLength;
	}
	
	@Override
	public void close(boolean force) {
		this.selectedDesti = NO_DESTI_SELECTED;
		deactivate();
		super.close(force);
	}
	
	@Override
	public void drawControll() {
		String[] lines = new String[4];
		lines[0] = NameSurround.PORTAL.getSurround(name);
		if (!isActive) {
			lines[1] = Stargate.langManager.getString(LangMsg.RIGHT_CLICK);
			lines[2] = Stargate.langManager.getString(LangMsg.TO_USE);
			lines[3] = network.concatName();
		} else {
			int destiIndex = selectedDesti % 3;
			int desti1 = selectedDesti - destiIndex;
			int maxLength = destinations.size();
			for (int i = 0; i < 3; i++) {
				int desti = i + desti1;
				if(desti == maxLength)
					break;
				String aDestinationName = getDestinationName(desti);
				if (destiIndex == i) {
					aDestinationName = NameSurround.DESTI.getSurround(aDestinationName);
				}
				lines[i + 1] = aDestinationName;
			}
		}
		getGate().drawControll(lines,!hasFlag(PortalFlag.ALWAYS_ON));
	}
	
	private void activate(Player actor) {
		/*
		 * Schedule for deactivation
		 */
		long activateTiming = System.currentTimeMillis();
		this.activateTiming = activateTiming;
		PopulatorAction action = new PopulatorAction() {

			@Override
			public void run(boolean forceEnd) {
				deactivate(activateTiming);
			}

			@Override
			public boolean isFinished() {
				return true;
			}
		};
		Stargate.syncSecPopulator.addAction(new DelayedAction(ACTIVE_DELAY,action));

		if(isActive)
			return;
		
		String[] destiNames = getDestinations(false);
		for(String name : destiNames) {
			destinations.add(network.getPortal(name));
		}
		StargateActivateEvent event = new StargateActivateEvent(this,actor,destinations);
		Bukkit.getPluginManager().callEvent(event);
		this.isActive = (destinations.size() > 0);
	}
	
	/**
	 * 
	 * @param activateTiming , the time the portal was activated. Kept track of so that to be sure the deactivation
	 * and activation matches
	 */
	private void deactivate(long activateTiming) {
		if(!isActive || isOpen() || activateTiming != this.activateTiming)
			return;
		deactivate();
	}
	
	private void deactivate() {
		this.destinations.clear();
		this.isActive = false;
		selectedDesti = NO_DESTI_SELECTED;
		drawControll();
	}
}