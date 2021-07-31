package net.TheDgtl.Stargate.network;

import java.io.InvalidClassException;
import java.util.HashMap;
import java.util.List;

import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.StargateData.PortalDataSender;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.NameSurround;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;


public class InterserverNetwork extends Network{
	private static final HashMap<String,InterserverNetwork> networkList = new HashMap<>(); 
	private static final String TARGET = "All";
	public InterserverNetwork(String netName) {
		super(netName);
	}
	
	public InterserverNetwork(String netName, List<IPortal> portals) {
		super(netName);
		PortalDataSender sender = new PortalDataSender(TARGET);
		for(IPortal portal : portals) {
			sender.addPortal(portal);
		}
		sender.send();
	}
	
	
	/*
	 * This is a duplicate, but i really don't know any better solution here / thor
	 */
	public static InterserverNetwork getOrCreateNetwork(String netName, boolean isPersonal) {
		if (!(networkList.containsKey(netName))) {
			InterserverNetwork.networkList.put(netName, new InterserverNetwork(netName));
		}
		return getNetwork(netName, isPersonal);
	}
	
	public static InterserverNetwork getNetwork(String netName, boolean isPersonal) {
		return networkList.get(netName);
	}
	
	public void sendPortalData(String targetServer) {
		PortalDataSender sender = new PortalDataSender(targetServer);
		for(String portalName : portalList.keySet()) {
			IPortal portal = portalList.get(portalName);
			if(portal instanceof VirtualPortal) {
				continue;
			}
			sender.addPortal(portal);
		}
		sender.send();
	}
	
	@Override
	public void addPortal(IPortal portal) {
		super.addPortal(portal);
		PortalDataSender sender = new PortalDataSender(TARGET);
		sender.addPortal(portal);
		sender.send();
	}

	@Override
	public void removePortal(IPortal portal) {
		super.removePortal(portal);
		PortalDataSender sender = new PortalDataSender(TARGET);
		
		sender.destroyPortal(getPortal(portal.getName()));
		sender.send();
	}
	
	public void addVirtualPortal(VirtualPortal virtual) {
		super.addPortal(virtual);
	}
	
	public void removeVirtualPortal(String virtualName) throws InvalidClassException {
		if(!(getPortal(virtualName) instanceof VirtualPortal))
			throw new InvalidClassException("portal has to be InterserverPortal class");
		super.removePortal(getPortal(virtualName));
	}
	
	public void removeVirtualPortal(VirtualPortal portal) {
		super.removePortal(portal);
	}
	
	@Override
	public String concatName() {
		return NameSurround.BUNGEE.getSurround(getName());
	}
	
	
	@Override
	public void destroy() {
		PortalDataSender sender = new PortalDataSender(TARGET);
		for(String portalName : portalList.keySet()) {
			IPortal portal = portalList.get(portalName);
			if(portal instanceof VirtualPortal) {
				continue;
			}
			sender.destroyPortal(portal);
		}
		sender.send();
		super.destroy();
	}
}
