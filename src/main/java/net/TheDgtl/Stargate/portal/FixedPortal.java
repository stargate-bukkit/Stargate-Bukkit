package net.TheDgtl.Stargate.portal;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;

public class FixedPortal extends Portal{
	/**
	 * 
	 */
	String destination;

	public FixedPortal(Network network, Block sign, String[] lines) throws NoFormatFound, GateConflict, NameError {
		super(network, sign, lines);
		destination = lines[1];

		drawControll();
	}
	
	
	
	/**
	 * What will happen when a player clicks the sign?
	 * @param action
	 * @param player 
	 */
	@Override
	public void onSignClick(Action action, Player actor) {}

	@Override
	public void drawControll() {
		String[] lines = new String[4];
		lines[0] = NameSurround.PORTAL.getSurround(name);
		lines[1] = NameSurround.DESTI.getSurround(destination);
		lines[2] = NameSurround.NETWORK.getSurround(this.network.name);
		lines[3] = (this.network.portalList.containsKey(destination)) ? ""
					: Stargate.langManager.getString(LangMsg.DISCONNECTED);
		getGate().drawControll(lines,!flags.contains(PortalFlag.ALWAYS_ON));
	}
	
	@Override
	public Portal loadDestination() {
		return this.network.getPortal(destination);
	}
	
	@Override
	public void close() {
		super.close();
		this.openFor = null;
	}
}