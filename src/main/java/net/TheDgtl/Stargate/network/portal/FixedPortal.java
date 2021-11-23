package net.TheDgtl.Stargate.network.portal;

import java.util.EnumSet;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;

public class FixedPortal extends Portal{
	/**
	 * 
	 */
	String destination;

	public FixedPortal(Network network, String name, String destiName, Block sign, EnumSet<PortalFlag> flags)
			throws NoFormatFound, GateConflict, NameError {
		super(network, name, sign, flags);
		destination = destiName;

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
		lines[0] = NameSurround.PORTAL.getSurround( super.formatTextFromSign(getColoredName()));
		lines[1] = NameSurround.DESTI.getSurround( super.formatTextFromSign(loadDestination().getColoredName()));
		lines[2] = this.network.concatName();
		lines[3] = (this.network.isPortalNameTaken(destination)) ? ""
					: Stargate.langManager.getString(LangMsg.DISCONNECTED);
		getGate().drawControll(lines,!hasFlag(PortalFlag.ALWAYS_ON));
	}
	
	@Override
	public IPortal loadDestination() {
		return this.network.getPortal(destination);
	}
	
	@Override
	public void close(boolean force) {
		super.close(force);
		this.openFor = null;
	}
}