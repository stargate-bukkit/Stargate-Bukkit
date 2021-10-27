package net.TheDgtl.Stargate.network.portal;

import java.util.EnumSet;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;

public class BungeePortal extends Portal{

	private VirtualPortal targetPortal;
	private String serverDesti;

	BungeePortal(Network network, String name, String desti,String serverDesti, Block sign, EnumSet<PortalFlag> flags)
			throws NameError, NoFormatFound, GateConflict {
		super(network, name, sign, flags);

		/*
		 * Create a virtual portal that handles everything related
		 * to moving the player to a different server. This is set
		 * as destination portal, of course.
		 * 
		 * Note that this is only used locally inside this portal
		 * and can not be found (should not) in any network anywhere.
		 */
		this.serverDesti = serverDesti;
		targetPortal = new VirtualPortal(serverDesti,desti,null,EnumSet.noneOf(PortalFlag.class));
	}

	@Override
	public void onSignClick(Action action, Player actor) {}

	@Override
	public void drawControll() {
		String[] lines = new String[4];
		lines[0] = NameSurround.PORTAL.getSurround(name);
		lines[1] = NameSurround.DESTI.getSurround(targetPortal.getName());
		lines[2] = serverDesti;
		lines[3] = "";
		getGate().drawControll(lines,!hasFlag(PortalFlag.ALWAYS_ON));
	}

	@Override
	public IPortal loadDestination() {
		return targetPortal;
	}

}
