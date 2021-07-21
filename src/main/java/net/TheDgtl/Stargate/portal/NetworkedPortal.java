package net.TheDgtl.Stargate.portal;

import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;

public class NetworkedPortal extends Portal {
	/**
	 * 
	 */
	// used in networked portals
	static final private int NO_DESTI_SELECTED = -1;
	private int selectedDesti = NO_DESTI_SELECTED;
	
	public NetworkedPortal(Network network, Block sign, String[] lines) throws NoFormatFound, GateConflict, NameError {
		super(network, sign, lines);
		
		drawControll();
	}
	
	/**
	 * TODO have this individual for each player?
	 * @param action
	 * @param player 
	 */
	@Override
	public void onSignClick(Action action, Player actor) {
		if (getDestinations().length < 1)
			return;
		openFor = actor;
		if ((selectedDesti == NO_DESTI_SELECTED) || getDestinations().length < 2) {
			selectedDesti = getNextDesti(1, -1);
		} else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
			int step = (action == Action.RIGHT_CLICK_BLOCK) ? -1 : 1;
			selectedDesti = getNextDesti(step, selectedDesti);
		}
		drawControll();
	}
	
	private String getDestination(int index) {
		if (index == NO_DESTI_SELECTED) {
			return "";
		}
		return getDestinations()[index];
	}
	
	private String[] getDestinations() {
		HashSet<String> tempPortalList = new HashSet<>(this.network.portalList.keySet());
		tempPortalList.remove(name);
		return tempPortalList.toArray(new String[0]);
	}
	
	@Override
	public Portal getDestination() {
		if(selectedDesti == NO_DESTI_SELECTED)
			return null;
		return this.network.getPortal(getDestinations()[selectedDesti]);
	}
	/**
	 * A method which allows selecting a index x steps away from a reference index
	 * without having to bother with index out of bounds stuff. If the index is out
	 * of bounds, it will just start counting from 0
	 * @param step
	 * @param initialDesti
	 * @return
	 */
	private int getNextDesti(int step, int initialDesti) {
		int destiLength = getDestinations().length;
		// Avoid infinite recursion if this is the only gate available
		if (destiLength < 1) {
			return -1;
		}
		int temp = initialDesti + destiLength;
		return (temp + step) % destiLength;
	}
	
	@Override
	public void close() {
		this.selectedDesti = NO_DESTI_SELECTED;
		super.close();
	}
	
	@Override
	public void drawControll() {
		String[] lines = new String[4];
		lines[0] = surroundWith(name, Network.PORTALNAMESURROUND);
		if (this.selectedDesti == NO_DESTI_SELECTED) {
			lines[1] = Stargate.langManager.getString("signRightClick");
			lines[2] = Stargate.langManager.getString("signToUse");
			lines[3] = surroundWith(this.network.netName, Network.NETWORKNAMESURROUND);
		} else {
			int destiIndex = selectedDesti % 3;
			int desti1 = selectedDesti - destiIndex;
			int maxLength = getDestinations().length;
			for (int i = 0; i < 3; i++) {
				int desti = i + desti1;
				if(desti == maxLength)
					break;
				String name = getDestination(desti);
				if (destiIndex == i) {
					name = surroundWith(name, Network.DESTINAMESURROUND);
				}
				lines[i + 1] = name;
			}
		}
		getGate().drawControll(lines);
	}
}