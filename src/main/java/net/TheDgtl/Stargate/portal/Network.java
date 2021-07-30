package net.TheDgtl.Stargate.portal;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.gate.GateStructureType;

public class Network {
	protected HashMap<String, IPortal> portalList;
	private static final HashMap<String, Network> networkList = new HashMap<>();
	protected String name;

	final static EnumMap<GateStructureType, HashMap<SGLocation, IPortal>> portalFromPartsMap = new EnumMap<>(GateStructureType.class);
	
	public Network(String netName) {
		this.name = netName;
		portalList = new HashMap<>();
	}
	
	public static Network getOrCreateNetwork(String netName, boolean isPersonal) {
		if (!(networkList.containsKey(netName))) {
			Network.networkList.put(netName, new Network(netName));
		}
		return getNetwork(netName, isPersonal);
	}
	
	public static Network getNetwork(String networkName, boolean isPersonal) {
		return networkList.get(networkName);
	}
	
	public IPortal getPortal(String name) {
		return portalList.get(name);
	}
	
	public void removePortal(String name) {
		portalList.remove(name);
	}
	
	public void addPortal(IPortal portal) {
		this.portalList.put(portal.getName(), portal);
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
		return NameSurround.NETWORK.getSurround(name);
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


	
}
