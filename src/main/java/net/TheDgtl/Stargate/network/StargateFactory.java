package net.TheDgtl.Stargate.network;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.DriverEnum;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;

public class StargateFactory {
	

	private final HashMap<String, Network> networkList = new HashMap<>();
	private final HashMap<String,InterserverNetwork> bungeeNetList = new HashMap<>(); 
	
	String bungeeDataBaseName = "bungeePortals";
	String tableName = "portals";
	
	private final Database database;
	private final Database bungeeDatabase;
	
	private SQLQuerryMaker bungeeSqlMaker;
	private SQLQuerryMaker localSqlMaker;
	
	public StargateFactory(Stargate stargate) throws SQLException {
		String databaseName = (String)Stargate.getSetting(Setting.DATABASE_NAME);
		File file = new File(stargate.getDataFolder().getAbsoluteFile(),databaseName + ".db");
		database = new SQLiteDatabase(file,stargate);
		if((boolean)Stargate.getSetting(Setting.USING_BUNGEE)) {
			DriverEnum driver = DriverEnum.parse((String)Stargate.getSetting(Setting.BUNGEE_DRIVER));
			String bungeeDatabaseName = (String)Stargate.getSetting(Setting.BUNGEE_DATABASE);
			String address = (String) Stargate.getSetting(Setting.BUNGEE_ADDRESS);
			switch(driver) {
			case SQLITE:
				File bFile = new File(address, bungeeDatabaseName + ".db");
				bungeeDatabase = new SQLiteDatabase(bFile, stargate);
				break;
			default:
				int port = (int) Stargate.getSetting(Setting.BUNGEE_PORT);
				bungeeDatabase = new MySqlDatabase(driver,address,port,bungeeDatabaseName,stargate);
			}
		} else {
			bungeeDatabase = null;
		}
		this.bungeeSqlMaker = new SQLQuerryMaker(bungeeDataBaseName);
		this.localSqlMaker = new SQLQuerryMaker(tableName);
		
		createTables();
		
		loadAllPortals(database,tableName);
		loadAllPortals(database,bungeeDataBaseName);
	}
	
	private void runStatement(Database database, PreparedStatement statement) throws SQLException{
		Connection conn = database.getConnection();
		statement.execute();
		statement.close();
	}
	
	
	private void createTables() throws SQLException {
		boolean isInterServer;
		Connection conn1 = database.getConnection();
		PreparedStatement localPortalsStatement = localSqlMaker.compileCreateStatement(conn1,(isInterServer = false));
		runStatement(database,localPortalsStatement);
		conn1.close();
		
		if(!(boolean)Stargate.getSetting(Setting.USING_BUNGEE)) {
			return;
		}
		Connection conn2 = database.getConnection();
		PreparedStatement localInterserverPortalsStatement = bungeeSqlMaker.compileCreateStatement(conn2,(isInterServer = false));
		runStatement(database,localInterserverPortalsStatement);
		conn2.close();
		
		Connection conn3 = bungeeDatabase.getConnection();
		PreparedStatement interserverPortalsStatement = bungeeSqlMaker.compileCreateStatement(conn3,(isInterServer = true));
		runStatement(bungeeDatabase,interserverPortalsStatement);
		conn3.close();
	}
	
	private void loadAllPortals(Database database, String databaseName) throws SQLException {
		loadAllPortals(database,databaseName,false);
	}
	
	private void loadAllPortals(Database database, String databaseName, boolean areVirtual) throws SQLException {
		Connection connection = database.getConnection();
		PreparedStatement statement = connection.prepareStatement(
				"SELECT * FROM " + databaseName);
		
		ResultSet set = statement.executeQuery();
		while(set.next()) {
			String netName = set.getString(1);
			String name = set.getString(2);
			String desti = set.getString(3);
			String worldName = set.getString(4);
			int x = set.getInt(5);
			int y = set.getInt(6);
			int z = set.getInt(7);
			String flagsMsg = set.getString(8);
			
			EnumSet<PortalFlag> flags = PortalFlag.parseFlags(flagsMsg);
			
			boolean isBungee = flags.contains(PortalFlag.FANCY_INTERSERVER);
			
			try {
				createNetwork(netName, isBungee);
			} catch (NameError e) {}
			Network net = getNetwork(netName, isBungee);
			
		
			if(areVirtual) {
				String server = set.getString(9);
				IPortal virtualPortal = new VirtualPortal(server,name,(InterserverNetwork) net,flags);
				net.addPortal(virtualPortal, false);
				continue;
			} else if(isBungee) {
				
			}
			
			World world = Bukkit.getWorld(worldName);
			Block block = world.getBlockAt(x, y, z);
			String[] virtualSign = {name, desti, netName};
			
			try {
				IPortal portal = Portal.createPortalFromSign(net, virtualSign, block, flags);
				net.addPortal(portal, false);
			}  catch (GateConflict e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoFormatFound e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NameError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	
	public void startInterServerConnection() throws SQLException {
		loadAllPortals(bungeeDatabase,bungeeDataBaseName,true);
	}
	
	public void endInterserverConnection() throws SQLException {
		for(InterserverNetwork net : bungeeNetList.values()) {
			for(IPortal portal : net.getAllPortals()) {
				/*
				 * To not unregister portals not belonging to this server
				 */
				if(portal instanceof VirtualPortal)
					continue;
				/*
				 * Removes the portal from the interserver database
				 */
				net.unregisterFromInterserver(portal);
			}
		}
	}
	
	public void createNetwork(String netName, boolean isBungee) throws NameError {
		if (netExists(netName, isBungee))
			throw new NameError(null);
		if (isBungee) {
			InterserverNetwork net = new InterserverNetwork(netName, database, bungeeDatabase, bungeeSqlMaker);
			HashMap<String, InterserverNetwork> map;
			map = bungeeNetList;
			map.put(netName, net);
			return;
		}
		Network net = new Network(netName,database,localSqlMaker);
		HashMap<String, Network> map;
		map = networkList;
		map.put(netName, net);
	}
	
	public boolean netExists(String netName, boolean isBungee) {
		return (getNetwork(netName,isBungee) != null);
	}
	
 	public Network getNetwork(String name, boolean isBungee) {
		return getNetMap(isBungee).get(name);
	}
	
	private HashMap<String, ? extends Network> getNetMap(boolean isBungee){
		if (isBungee) {
			return bungeeNetList;
		} else {
			return networkList;
		}
	}
	
	
	/**
	 * Convert a string processed from IPortal.getString(IPortal portal) back into a portal
	 * TODO only works for virtual portals
	 * TODO come up with a good script to read this kind of data
	 * @param str
	 * @param isVirtual should the string be converted as a virtual portal?
	 * @return
	 * @throws ClassNotFoundException Issue with type in the string
	 */
	public IPortal createFromString(String str, boolean isVirtual) throws ClassNotFoundException {
		String type = str.substring(0, str.indexOf("{"));
		String stringData = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
		String[] elements = stringData.split(",");
		String portalName = "";
		String destiName = "";
		EnumSet<PortalFlag> flags = EnumSet.noneOf(PortalFlag.class);
		String netName = "";
		String server = "";

		for (String element : elements) {
			String[] temp = element.split("=");
			String key = temp[0];
			String data = temp[1];
			switch (key) {
			case "flags":
				flags = PortalFlag.parseFlags(data);
				break;
			case "name":
				portalName = data;
				break;
			case "net":
				netName = data;
				break;
			case "desti":
				destiName = data;
				break;
			case "server":
				server = data;
				break;
			}
		}

		if (isVirtual) {
			try {
				createNetwork(netName,true);
			} catch(NameError e) {}
			
			InterserverNetwork network=  (InterserverNetwork) getNetwork(netName, true);
			return new VirtualPortal(server, portalName,network, flags);
		}
		return null; // TODO Not implemented yet
	}
}
