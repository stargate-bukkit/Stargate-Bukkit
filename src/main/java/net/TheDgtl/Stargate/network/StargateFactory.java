package net.TheDgtl.Stargate.network;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.DriverEnum;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;

public class StargateFactory {
	

	private final HashMap<String, Network> networkList = new HashMap<>();
	private final HashMap<String, Network> privateNetworkList = new HashMap<>();
	private final HashMap<String,InterserverNetwork> bungeeNetList = new HashMap<>(); 
	private final HashMap<String,InterserverNetwork> privateBungeeNetList = new HashMap<>(); 
	
	private final HashMap<byte[], HashMap<String,? extends Network>> test = new HashMap<>();
	
	private final Database database;
	private final Database bungeeDatabase;
	
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
		createTables();
	}
	
	private void createTables() throws SQLException {
		Connection conn = database.getConnection();
		PreparedStatement statement = conn.prepareStatement(
				"CREATE TABLE IF NOT EXISTS portals("
				+ " network VARCHAR, name VARCHAR, world VARCHAR,"
				+ " x INTEGER, y INTEGER, z INTEGER, flags VARCHAR,"
				+ " UNIQUE(network,name) );");
		statement.execute();
		statement.close();
		conn.close();
		if(!(boolean)Stargate.getSetting(Setting.USING_BUNGEE)) {
			return;
		}
		Connection bungeeConn = bungeeDatabase.getConnection();
		PreparedStatement bungeeStatement = bungeeConn.prepareStatement(
				"CREATE TABLE IF NOT EXISTS portals("
				+ " network VARCHAR, name VARCHAR, world VARCHAR,"
				+ " x INTEGER, y INTEGER, z INTEGER, flags VARCHAR,"
				+ " server VARCHAR, UNIQUE(network,name) );");
		bungeeStatement.execute();
		bungeeStatement.close();
		bungeeConn.close();
	}
	
	public void loadAllPortals() {
		
	}
	
	/**
	 * TODO there's probably a hashmap or something to simplify this situation
	 * 
	 * @param name
	 * @param isBungee
	 * @param isPersonal
	 * @throws NameError
	 */
	public void createNetwork(String name, boolean isBungee, boolean isPersonal) throws NameError {
		if (getNetwork(name, isBungee, isPersonal) != null)
			throw new NameError(null);
		if (isBungee) {
			InterserverNetwork net = new InterserverNetwork(name, bungeeDatabase);
			HashMap<String, InterserverNetwork> map;
			if (isPersonal)
				map = privateBungeeNetList;
			else
				map = bungeeNetList;
			map.put(name, net);
			return;
		}
		Network net = new Network(name,database);
		HashMap<String, Network> map;
		if (isPersonal)
			map = privateNetworkList;
		map = networkList;
		map.put(name, net);
	}

	public Network getNetwork(String name, boolean isBungee, boolean isPersonal) {
		Network net;
		if (isBungee) {
			if (isPersonal)
				net = privateBungeeNetList.get(name);
			else
				net = bungeeNetList.get(name);
		} else {
			if (isPersonal)
				net = privateNetworkList.get(name);
			else
				net = networkList.get(name);
		}
		return net;
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
				createNetwork(netName,true,flags.contains(PortalFlag.PERSONAL_NETWORK));
			} catch(NameError e) {
				
			}
			InterserverNetwork network=  (InterserverNetwork) getNetwork(netName, true, flags.contains(PortalFlag.PERSONAL_NETWORK));
			return new VirtualPortal(server, portalName,network, flags);
		}
		return null; // TODO Not implemented yet
	}
}
