package net.TheDgtl.Stargate.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.NameSurround;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.SGLocation;

public class Network {
	protected HashMap<String, IPortal> portalList;
	private Database database;
	protected String name;
	

	final static EnumMap<GateStructureType, HashMap<SGLocation, IPortal>> portalFromPartsMap = new EnumMap<>(GateStructureType.class);
	
	public Network(String name, Database database) throws NameError {
		if (name.isBlank() || (name.length() == Stargate.MAX_TEXT_LENGTH))
			throw new NameError(LangMsg.NAME_LENGTH_FAULT);
		this.name = name;
		this.database = database;
		portalList = new HashMap<>();
	}
	
	public IPortal getPortal(String name) {
		return portalList.get(name);
	}
	
	public void registerLocations(GateStructureType type, HashMap<SGLocation, IPortal> locationsMap) {
		if(!portalFromPartsMap.containsKey(type)) {
			portalFromPartsMap.put(type, new HashMap<SGLocation, IPortal>());
		}
		portalFromPartsMap.get(type).putAll(locationsMap);

		for (SGLocation loc : locationsMap.keySet()) {
			Stargate.log(Level.FINEST, "Registering portal " + locationsMap.get(loc).getName() + " with structType "
					+ type + " at location " + loc.toString());
		}
	}

	public void unRegisterLocation(GateStructureType type, SGLocation loc) {
		HashMap<SGLocation, IPortal> map = portalFromPartsMap.get(type);
		if (map != null) {
			Stargate.log(Level.FINEST, "Unregistering portal " + map.get(loc).getName() + " with structType " + type
					+ " at location " + loc.toString());
			map.remove(loc);
		}
	}
	
	protected PreparedStatement compileRemoveStatement(Connection conn, IPortal portal) throws SQLException {
		PreparedStatement output = conn.prepareStatement(
				"DELETE FROM portals"
				+ " WHERE name = ? AND network = ?");
		output.setString(1, portal.getName());
		output.setString(2, this.name);
		return output;
	}
	
	protected PreparedStatement compileAddStatement(Connection conn, IPortal portal) throws SQLException {
		PreparedStatement output = conn.prepareStatement(
				"INSERT INTO portals (network,name,world,x,y,z,flags)"
				+ " VALUES(?,?,?,?,?,?,?);");
		output.setString(1, this.name);
		output.setString(2, portal.getName());
		
		Location loc = portal.getSignPos();
		output.setString(3, loc.getWorld().getName());
		output.setInt(4, loc.getBlockX());
		output.setInt(5, loc.getBlockY());
		output.setInt(6, loc.getBlockZ());
		output.setString(7, portal.getAllFlagsString());
		return output;
	}
	
	public void removePortal(IPortal portal) {
		try {
			Connection connection = database.getConnection();
			PreparedStatement statement = compileRemoveStatement(connection, portal);
			statement.execute();
			statement.close();
			portalList.remove(portal.getName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addPortal(IPortal portal) {
		try {
			Connection connection = database.getConnection();
			PreparedStatement statement = compileAddStatement(connection, portal);
			statement.execute();
			statement.close();
			portalList.put(portal.getName(), portal);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isPortalNameTaken(String name) {
		return portalList.containsKey(name);
	}

	public void updatePortals() {
		for (String portal : portalList.keySet()) {
			getPortal(portal).drawControll();
		}
	}
 	
	public HashSet<String> getAvailablePortals(boolean isOverrideHidden, IPortal requester){
		HashSet<String> tempPortalList = new HashSet<>(portalList.keySet());
		tempPortalList.remove(requester.getName());
		if (!isOverrideHidden) {
			HashSet<String> removeList = new HashSet<>();
			for (String portalName : tempPortalList) {
				if (getPortal(portalName).hasFlag(PortalFlag.HIDDEN))
					removeList.add(portalName);
			}
			tempPortalList.removeAll(removeList);
		}
		return tempPortalList;
	}
	
	static public IPortal getPortal(Location loc, GateStructureType key) {
		return getPortal(new SGLocation(loc),key);
	}
	
	public static IPortal getPortal(Location loc, GateStructureType[] keys) {
		
		return getPortal(new SGLocation(loc), keys);
	}
	
	/**
	 * Get a portal from location and the type of gateStructure targeted
	 * @param loc
	 * @param key
	 * @return
	 */
	static public IPortal getPortal(SGLocation loc, GateStructureType key) {
		if (!(portalFromPartsMap.containsKey(key))) {
			return null;
		}
		IPortal portal = portalFromPartsMap.get(key).get(loc);
		return portal;
	}
	
	/**
	 * Get a portal from location and the types of gateStructures targeted
	 * @param loc
	 * @param keys
	 * @return
	 */
	static public IPortal getPortal(SGLocation loc, GateStructureType[] keys) {
		for(GateStructureType key : keys) {
			IPortal portal = getPortal(loc, key);
			if(portal != null)
				return portal;
		}
		return null;
	}
	

	public static boolean isInPortal(List<Block> blocks, GateStructureType[] keys) {
		for (Block block : blocks) {
			if (getPortal(block.getLocation(), GateStructureType.values()) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks North, west, south, east direction. Not up / down, as it is currently
	 * not necessary and a waste of resources
	 * 
	 * @param loc The location to check for adjacency
	 * @param key
	 * @return Is adjacent to specified type of gateStructure.
	 */
	public static boolean isNextToPortal(Location loc, GateStructureType key) {
		BlockVector adjacentVec = new BlockVector(1,0,0);
		for(int i = 0; i < 4; i++) {
			Location adjacentLoc = loc.clone().add(adjacentVec);
			if(getPortal(adjacentLoc, key) != null) {
				return true;
			}
			adjacentVec.rotateAroundY(Math.PI/2);
		}
		return false;
	}

	public String concatName() {
		return NameSurround.NETWORK.getSurround(getName());
	}
	
	/**
	 * Destroy network and every portal contained in it
	 */
	public void destroy() {
		for(String portalName : portalList.keySet()) {
			IPortal portal = portalList.get(portalName);
			portal.destroy();
		}
		portalList.clear();
	}

	public String getName() {
		return name;
	}
	
}
