package net.TheDgtl.Stargate;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.portal.Gate;
import net.TheDgtl.Stargate.portal.Network;

public class StargateAPI {
	/**
	 * Behaviours:
	 * - Modify portal; change: network, name, owner, PortalType (if networked and so on)
	 * - Create a new portal; force generates a portal from nothing
	 * - Destroy a portal
	 */
	public Network.Portal getPortal(Location portalBlock) {
		return null;
	}
	public Network getNetwork(String networkName) {
		return null;
	}
	public void createPortal(Gate config, Location location, Vector openFacing ) {
		
	}
	public void destroyPortal(Network.Portal portal) {
		
	}
	/**
	 * Force a connection between two portals that are not in the same network, instantly opens both gates
	 * @param portal1
	 * @param portal2
	 */
	public void forceConnect(Network.Portal portal1, Network.Portal portal2) {
		
	}
}
