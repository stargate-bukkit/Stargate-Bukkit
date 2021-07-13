package net.TheDgtl.Stargate;

import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;

import net.TheDgtl.Stargate.event.StargateEvent;
import net.TheDgtl.Stargate.portal.Network.PortalFlag;

public class PermissionManager {
	private Player player;
	private String denyMsg;
	public final static String FLAGPERMISSION = "stargate.flag.";
	public final static String DESTROYPERMISSION = "stargate.destroy.";
	public final static String CREATEPERMISSION = "stargate.create.";
	public final static String USEPERMISSION = "stargate.use.";
	public final static String FREEPERMISSION = "stargate.free.";

	public PermissionManager(Player player) {
		this.player = player;
	}

	public HashSet<PortalFlag> returnAllowedFlags(HashSet<PortalFlag> flags){
		HashSet<PortalFlag> permissable = new HashSet<>();
		
		for(PortalFlag flag : flags) {
			if(player.hasPermission( (FLAGPERMISSION + flag.label).toLowerCase() )) {
				permissable.add(flag);
			}
		}
		return permissable;
	}
	
	public boolean hasPerm(String permission) {
		return player.hasPermission(permission);
	}
	
	public boolean hasPerm(StargateEvent event) {
		List<String> perms = event.getRelatedPerms();
		
		for(String perm : perms) {
			if(!player.hasPermission(perm)) {
				return false;
			}
		}
		//TODO throw event to bukkit event thing 
		return true;
	}
	/**
	 * @param targetNetwork
	 * @return allowedNetwork
	 */
	public String getAllowedNetwork(String targetNetwork, String permType) {
		if (player.getName() != targetNetwork && player.hasPermission(permType + "." + targetNetwork))
			return targetNetwork;
		if(player.hasPermission(permType + ".personal"))
			return player.getName();
		return null;
	}

	public String getDenyMsg() {
		return denyMsg;
	}
}
