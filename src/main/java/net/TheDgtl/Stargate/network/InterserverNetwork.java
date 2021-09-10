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
	public InterserverNetwork(String netName, Database database) throws NameError {
		super(netName, database);
	}
	
	public InterserverNetwork(String netName, Database database, List<IPortal> portals) throws NameError {
		super(netName, database);
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
	protected PreparedStatement compileAddStatement(Connection conn, IPortal portal) throws SQLException{
		PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO portals (network,name,world,x,y,z,flags,server)"
				+ " VALUES(?,?,?,?,?,?,?,?);");
		statement.setString(1, this.name);
		statement.setString(2, portal.getName());
		
		Location loc = portal.getSignPos();
		statement.setString(3, loc.getWorld().getName());
		statement.setInt(4, loc.getBlockX());
		statement.setInt(5, loc.getBlockY());
		statement.setInt(6, loc.getBlockZ());
		statement.setString(7, portal.getAllFlagsString());
		statement.setString(8, Stargate.serverName);
		return statement;
	}
	
	@Override
	public void destroy() {
		destroy();
	}
}
