package net.TheDgtl.Stargate.network;

import java.io.InvalidClassException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;

import net.TheDgtl.Stargate.Channel;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.NameSurround;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;


public class InterserverNetwork extends Network{
	private Database interserverDatabase;

	public InterserverNetwork(String netName, Database database, Database interserverDatabase,SQLQuerryMaker sqlMaker) throws NameError {
		super(netName, database, sqlMaker);
		this.interserverDatabase = interserverDatabase;
	}
	
	public InterserverNetwork(String netName, Database database,SQLQuerryMaker sqlMaker, List<IPortal> portals) throws NameError {
		super(netName, database, sqlMaker);
		for(IPortal portal : portals)
			addPortal(portal,false);
	}
	
	public void addVirtualPortal(VirtualPortal virtual) {
		super.addPortal(virtual,false);
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
	protected void savePortal(IPortal portal) {
		boolean isInterServer;
		/*
		 * Save one local partition of every bungee gate on this server
		 * Also save it to the interserver database, so that it can be
		 * seen on other servers TODO rethink this
		 */
		super.savePortal(database, portal, (isInterServer=false));
		super.savePortal(interserverDatabase, portal, (isInterServer=true));
	}
	
	@Override
	public void destroy() {
		destroy();
	}
}
