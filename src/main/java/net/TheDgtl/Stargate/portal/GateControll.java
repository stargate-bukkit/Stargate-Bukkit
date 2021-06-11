package net.TheDgtl.Stargate.portal;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class GateControll extends GateStructure{

	@Override
	public boolean isInPortal(@NotNull Vector relativeLocation) {
		return false;
	}

	@Override
	public boolean isValidState() {
		// TODO Auto-generated method stub
		return false;
	}

}
