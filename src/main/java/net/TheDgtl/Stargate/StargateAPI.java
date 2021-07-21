package net.TheDgtl.Stargate;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.portal.Gate;
import net.TheDgtl.Stargate.portal.GateStructure;
import net.TheDgtl.Stargate.portal.Network;

public class StargateAPI {
	/**
	 * Behaviours:
	 * - Modify portal; change: network, name, owner, PortalType (if networked and so on)
	 * - Create a new portal; force generates a portal from nothing
	 * - Destroy a portal
	 */
	public Network.Portal getPortal(Location portalBlock) {
		return Network.getPortal(portalBlock, GateStructure.Type.values());
	}
	public Network getNetwork(String networkName) {
		return Network.getNetwork(networkName);
	}
	public void createPortal(Gate config, Location location, Vector openFacing ) {
		//TODO write a method that does this
	}
	/**
	 * Force a connection between two portals that are not in the same network, instantly opens both gates
	 * @param portal1
	 * @param portal2
	 */
	public void forceConnect(Network.Portal portal1, Network.Portal portal2) {
		portal1.setOverrideDesti(portal2);
		portal1.openDestAndThis(null);
	}
}
