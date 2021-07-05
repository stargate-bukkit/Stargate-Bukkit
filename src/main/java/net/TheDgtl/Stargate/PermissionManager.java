package net.TheDgtl.Stargate;

import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;

import net.TheDgtl.Stargate.event.StargateEvent;
import net.TheDgtl.Stargate.portal.Network.PortalFlag;

public class PermissionManager {
	private Player player;
	
	private final static String FLAGPERMISSION = "stargate.flag.";
	private final static String DESTROYPERMISSION = "stargate.destroy.";
	private final static String CREATEPERMISSION = "stargate.create.";
	private final static String USEPERMISSION = "stargate.use.";
	private final static String FREEPERMISSION = "stargate.free.";

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
}
