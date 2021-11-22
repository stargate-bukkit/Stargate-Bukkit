package net.TheDgtl.Stargate;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import net.TheDgtl.Stargate.event.StargateEvent;
import net.TheDgtl.Stargate.network.portal.PortalFlag;

/**
 * Manages the plugin's permissions.
 * @author Thorin
 * @author Pheotis
 */

// TODO See Discussion Three
public class PermissionManager {
	private Entity player;
	private LangMsg denyMsg;
	
	private final static String FLAGPERMISSION = "stargate.flag";
	private final static String CREATEPERMISSION = "sg.create.network";

	public PermissionManager(Entity target) {
		this.player = target;
	}

	public enum PortalAction{
		CREATE, DESTROY, USE;
	}
	
	public enum Select {
		TYPE, NETWORK, WORLD, FOLLOW;
	}
	
	public EnumSet<PortalFlag> returnAllowedFlags(EnumSet<PortalFlag> flags){
		for(PortalFlag flag : flags) {
			if(!player.hasPermission( (FLAGPERMISSION + flag.label).toLowerCase() )) {
				flags.remove(flag);
			}
		}
		return flags;
	}
	
	public boolean hasPerm(String permission) {
		return player.hasPermission(permission);
	}
	
	public boolean hasPerm(StargateEvent event) {
		List<Permission> perms = event.getRelatedPerms();
		
		for(Permission perm : perms) {
			if(!player.hasPermission(perm)) {
				return false;
			}
		}
		//TODO throw event to bukkit event thing 
		return true;
	}
	
	public boolean canCreateInNetwork(String network) {
		boolean hasPerm = true;
		
		if(player.getName() == network)
			hasPerm = player.hasPermission(CREATEPERMISSION + ".personal");
		else
			hasPerm =  player.hasPermission(CREATEPERMISSION + ".custom." + network);
		if(!hasPerm)
			denyMsg = LangMsg.NET_DENY;
		return hasPerm;
	}

	public LangMsg getDenyMsg() {
		return denyMsg;
	}
	
	
}
