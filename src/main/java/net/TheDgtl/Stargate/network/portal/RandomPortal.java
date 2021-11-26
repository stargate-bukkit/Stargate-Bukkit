package net.TheDgtl.Stargate.network.portal;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.Network;

public class RandomPortal extends Portal{

	private final Random randomizer = new Random();
	
	RandomPortal(Network network, String name, Block sign, EnumSet<PortalFlag> flags, UUID ownerUUID) throws NameError, NoFormatFound, GateConflict {
		super(network, name, sign, flags, ownerUUID);
	}

	@Override
	public void onSignClick(Action action, Player actor) {}

	@Override
	public void drawControll() {
		String lines[] = {
				super.colorDrawer.parseName(NameSurround.PORTAL, this),
				super.colorDrawer.parseLine(NameSurround.DESTI.getSurround(Stargate.langManager.getString(LangMsg.RANDOM))),
				super.colorDrawer.parseLine(network.concatName()),
			""
		};
		getGate().drawControll(lines,!hasFlag(PortalFlag.ALWAYS_ON));
	}

	@Override
	public IPortal loadDestination() {
		Set<String> allPortalNames = network.getAvailablePortals(null, this);
		String[] destinations = allPortalNames.toArray(new String[0]);
		if (destinations.length < 1) {
			return null;
		}
		int randomNumber = randomizer.nextInt(destinations.length);
		String dest = destinations[randomNumber];

		return network.getPortal(dest);
	}

	@Override
	public void close(boolean force) {
		super.close(force);
		destination = null;
	}
}

