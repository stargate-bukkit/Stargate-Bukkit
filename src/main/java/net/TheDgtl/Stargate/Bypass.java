package net.TheDgtl.Stargate;

import java.util.EnumSet;

import org.bukkit.entity.Player;

public enum Bypass {
	COST_USE("sg.admin.bypass.cost.use"), COST_CREATE("sg.admin.bypass.cost.create"),
	COST_DESTROY("sg.admin.bypass.cost.destroy"), PRIVATE("sg.admin.bypass.private"),
	HIDDEN("sg.admin.bypass.hidden");
	
	
	private String relatedPerm;

	private Bypass(String relatedPerm) {
		this.relatedPerm = relatedPerm;
	}
	
	public EnumSet<Bypass> parseBypass(Player player){
		EnumSet<Bypass> allowedBypasses = EnumSet.noneOf(Bypass.class);
		for(Bypass bp : values()) {
			if(player.hasPermission(bp.relatedPerm))
				allowedBypasses.add(bp);
		}
		return allowedBypasses;
	}
	
	public String getPerm() {
		return relatedPerm;
	}
}
