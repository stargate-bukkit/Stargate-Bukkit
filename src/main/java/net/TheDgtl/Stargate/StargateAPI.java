package net.TheDgtl.Stargate;


import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.portal.IPortal;

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
	public IPortal getPortal(Location portalBlock) {
		return Network.getPortal(portalBlock, GateStructureType.values());
	}
	
	/**
	 * Check if specific part of a portal is in the specified position
	 * @param portalBlock
	 * @param structure , the type of part of the portal
	 * @return the portal if found / otherwise null
	 */
	public IPortal getPortal(Location portalBlock, GateStructureType structure){
		return Network.getPortal(portalBlock, structure);
	}
	
	/**
	  * Check if specific parts of a portal is in the specified position
	 * @param portalBlock
	 * @param structures , the types of parts of the portal
	 * @return the portal if found / otherwise null
	 */
	public IPortal getPortal(Location portalBlock, GateStructureType[] structures){
		return Network.getPortal(portalBlock, structures);
	}
	
	/**
	 * @param net Network
	 * @param portalName Portal
	 * @return The portal found / otherwise null
	 */
	public IPortal getPortal(Network net, String portalName){
		return net.getPortal(portalName);
	}
	
	/**
	 * @param netName name of the network
	 * @param portalName name of the portal
	 * @return The portal found / otherwise null
	 */
	public IPortal getPortal(String netName, String portalName, boolean isBungee, boolean isPersonal){
		Network net = Stargate.factory.getNetwork(netName, isBungee, isPersonal);
		if(net == null)
			return null;
		
		return net.getPortal(portalName);
	}
	
	/**
	 * 
	 * @param portal
	 * @return The network which the portal is assigned to
	 */
	public Network getNetwork(IPortal portal) {
		return portal.getNetwork();
	}
	
	/**
	 * 
	 * @param networkName
	 * @return The network found / otherwise null
	 */
	public Network getNetwork(String networkName, boolean isBungee, boolean isPersonal) {
		return Stargate.factory.getNetwork(networkName, isBungee, isPersonal);
	}
	
	/**
	 * Change the network which a portal is operating at, note that you will need
	 * to change it has a fixed destination
	 * @param portal
	 * @param targetNet
	 */
	public void changeNetwork(IPortal portal, Network targetNet) {
		portal.setNetwork(targetNet);
	}
	/**
	 * TODO currently not implemented
	 * @param config
	 * @param location
	 * @param openFacing
	 */
	public void createPortal(Gate config, Location location, Vector openFacing ) {
		
	}

	/**
	 * Force a connection between two portals. They do not have to be in the same
	 * network. When portal has been entered, the destination will get removed
	 * 
	 * @param target the portal which will have it's destination changed
	 * @param destination
	 */
	public void forceConnect(IPortal target, IPortal destination) {
		target.setOverrideDesti(destination);
	}


	public class InterFacePortal{
		private IPortal portal;
	}
	
	
}
