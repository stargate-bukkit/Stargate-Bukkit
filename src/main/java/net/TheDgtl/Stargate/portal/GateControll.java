package net.TheDgtl.Stargate.portal;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class GateControll extends GateStructure{

	@Override
	public boolean isInPortal(@NotNull Location loc) {
		return false;
	}

	@Override
	public boolean isValidState() {
		// TODO Auto-generated method stub
		return false;
	}

}
