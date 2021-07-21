package net.TheDgtl.Stargate;

import java.lang.reflect.Constructor;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateStructure;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.Portal;

public class StargateAPI {
	/*
	 * Behaviours:
	 * - Modify portal; change: network, name, owner, PortalType (if networked and so on)
	 * - Create a new portal; force generates a portal from nothing
	 * - Destroy a portal
	 */
	
	/**
	 * Check if a portal is in specified position
	 * @param portalBlock
	 * @return the portal if found / otherwise null
	 */
	public Portal getPortal(Location portalBlock) {
		return Network.getPortal(portalBlock, GateStructure.Type.values());
	}
	
	/**
	 * Check if specific part of a portal is in the specified position
	 * @param portalBlock
	 * @param structure , the type of part of the portal
	 * @return the portal if found / otherwise null
	 */
	public Portal getPortal(Location portalBlock, GateStructure.Type structure){
		return Network.getPortal(portalBlock, structure);
	}
	
	/**
	  * Check if specific parts of a portal is in the specified position
	 * @param portalBlock
	 * @param structures , the types of parts of the portal
	 * @return the portal if found / otherwise null
	 */
	public Portal getPortal(Location portalBlock, GateStructure.Type[] structures){
		return Network.getPortal(portalBlock, structures);
	}
	
	/**
	 * @param net Network
	 * @param portalName Portal
	 * @return The portal found / otherwise null
	 */
	public Portal getPortal(Network net, String portalName){
		return net.getPortal(portalName);
	}
	
	/**
	 * @param netName name of the network
	 * @param portalName name of the portal
	 * @return The portal found / otherwise null
	 */
	public Portal getPortal(String netName, String portalName){
		return Network.getNetwork(netName).getPortal(portalName);
	}
	
	/**
	 * 
	 * @param portal
	 * @return The network which the portal is assigned to
	 */
	public Network getNetwork(Portal portal) {
		return portal.getNetwork();
	}
	
	/**
	 * 
	 * @param networkName
	 * @return The network found / otherwise null
	 */
	public Network getNetwork(String networkName) {
		return Network.getNetwork(networkName);
	}
	
	/**
	 * Change the network which a portal is operating at
	 * @param portal
	 * @param targetNet
	 */
	public void changeNetwork(Portal portal, Network targetNet) {
		portal.setNetwork(targetNet);
	}
	/**
	 * //TODO currently not implemented
	 * @param config
	 * @param location
	 * @param openFacing
	 */
	public void createPortal(Gate config, Location location, Vector openFacing ) {
		
	}

	/**
	 * Force a connection between two portals. They do not have to be in the same
	 * network, instantly opens both gates
	 * 
	 * @param portal1
	 * @param portal2
	 */
	public void forceConnect(Portal portal1, Portal portal2) {
		portal1.setOverrideDesti(portal2);
		portal1.openDestAndThis(null);
	}
}
