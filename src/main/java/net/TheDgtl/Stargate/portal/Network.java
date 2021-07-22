package net.TheDgtl.Stargate.portal;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.GateStructureType;

public class Network {
	/*
	 * Conceived as the class that can store a network of portals inside itself
	 * Portals inside this class can only communicate with each other
	 * 
	 * pros: - Makes searching algorithms somewhat easier/ less CPU intensive -
	 * Segments code?
	 * 
	 * Behaviours: - Load network (each network might as well have an individual db)
	 * - Get portal by name
	 */
	HashMap<String, Portal> portalList;
	public static final HashMap<String, Network> networkList = new HashMap<>();
	String name;

	public static final String DEFAULT_NET = "central";
	static final String[] PORTALNAMESURROUND;
	static final String[] DESTINAMESURROUND;
	static final String[] NETWORKNAMESURROUND;
	static {
		PORTALNAMESURROUND = new String[] { "-", "-" };
		DESTINAMESURROUND = new String[] { ">", "<" };
		NETWORKNAMESURROUND = new String[] { "(", ")" };
	}

	final static HashMap<GateStructureType, HashMap<SGLocation, Portal>> portalFromPartsMap = new HashMap<>();

	public Network(String netName) {
		this.name = netName;
		portalList = new HashMap<>();
	}

	public static Network getOrCreateNetwork(String netName, boolean isPersonal) {
		if (!(networkList.containsKey(netName))) {
			Network.networkList.put(netName, new Network(netName));
		}
		return Network.networkList.get(netName);
	}
	
	public Portal getPortal(String name) {
		return portalList.get(name);
	}
	
	public void addPortal(Portal portal) {
		this.portalList.put(portal.name, portal);
	}
	
	static public Portal getPortal(Location loc, GateStructureType key) {
		return getPortal(new SGLocation(loc),key);
	}
	
	public static Portal getPortal(Location loc, GateStructureType[] keys) {
		
		return getPortal(new SGLocation(loc), keys);
	}
	
	/**
	 * Get a portal from location and the type of gateStructure targeted
	 * @param loc
	 * @param key
	 * @return
	 */
	static public Portal getPortal(SGLocation loc, GateStructureType key) {
		if (!(portalFromPartsMap.containsKey(key))) {
			Stargate.log(Level.FINER, "portalFromPartsMap does not contain key " + key);
			return null;
		}
		Portal portal = portalFromPartsMap.get(key).get(loc);
		return portal;
	}
	
	/**
	 * Get a portal from location and the types of gateStructures targeted
	 * @param loc
	 * @param keys
	 * @return
	 */
	static public Portal getPortal(SGLocation loc, GateStructureType[] keys) {
		for(GateStructureType key : keys) {
			Portal portal = getPortal(loc, key);
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
	
	//Adjacent
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

	public static Network getNetwork(String networkName) {
		return networkList.get(networkName);
	}
	
	public Network getNetworkInstance() {
		return this;
	}




	
}
