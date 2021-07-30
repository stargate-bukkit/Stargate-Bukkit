package net.TheDgtl.Stargate.portal;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;

public class RandomPortal extends Portal{

	private final Random randomizer = new Random();
	
	RandomPortal(Network network, String name, Block sign, EnumSet<PortalFlag> flags) throws NameError, NoFormatFound, GateConflict {
		super(network, name, sign, flags);
	}

	@Override
	public void onSignClick(Action action, Player actor) {}

	@Override
	public void drawControll() {
		String lines[] = {
			NameSurround.PORTAL.getSurround(name),
			NameSurround.DESTI.getSurround(Stargate.langManager.getString(LangMsg.RANDOM)),
			network.concatName(),
			""
		};
		getGate().drawControll(lines,!hasFlag(PortalFlag.ALWAYS_ON));
	}

	@Override
	public IPortal loadDestination() {
		Set<String> allPortalNames = network.getAvailablePortals(hasFlag(PortalFlag.FORCE_SHOW), this);
		String[] destinations = allPortalNames.toArray(new String[0]);
		if (destinations.length < 1) {
			return null;
		}
		int randomNumber = randomizer.nextInt(destinations.length);
		String dest = destinations[randomNumber];

		return network.getPortal(dest);
	}

	@Override
	public void close() {
		super.close();
		destination = null;
	}
}

