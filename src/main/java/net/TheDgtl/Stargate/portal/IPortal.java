package net.TheDgtl.Stargate.portal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;

public interface IPortal {

	void destroy();

	boolean isOpen();

	boolean isOpenFor(Player player);

	void teleportHere(Player player);

	void doTeleport(Player player);

	void drawControll();

	void close();

	public void onSignClick(Action action, Player player);

	void onButtonClick(Player player);

	void open(Player player);

	String getName();

	void setNetwork(Network targetNet);

	void setOverrideDesti(IPortal destination);

	Network getNetwork();
	
	boolean hasFlag(PortalFlag flag);
	
	String getAllFlagsString();
	
	/**
	 * Convert a portal into a string, would look like this:
	 * 		Classname{key1=data1,key2=data2 ... }
	 * @param portal
	 * @return
	 */
	public static String getString(IPortal portal) {
		String type = portal.getClass().getName();
		
		HashMap<String, String> data = new HashMap<>();
		data.put("flags", portal.getAllFlagsString());
		data.put("name", portal.getName());
		data.put("net", portal.getNetwork().name);
		if((boolean) Stargate.getSetting(Setting.USING_BUNGEE)) {
			data.put("server", Stargate.serverName);
		}
		String endMsg = type + "{";
		Iterator<String> it = data.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			endMsg = endMsg + key + "=" + data.get(key);
			if(it.hasNext())
				endMsg = endMsg + ",";
		}
		return endMsg + "}"; 
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
	public static IPortal createFromString(String str, boolean isVirtual) throws ClassNotFoundException {
		String type = str.substring(0, str.indexOf("{") - 1);
		String stringData = str.substring(str.indexOf("{") + 1, str.indexOf("}") - 1);
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
			return new VirtualPortal(server, portalName, InterserverNetwork.getOrCreateNetwork(netName, false), flags);
		}
		return null; // TODO Not implemented yet
	}
}
