package net.TheDgtl.Stargate;

import java.util.logging.Level;

public enum LangMsg {
	PREFIX("prefix"), TELEPORT("teleportMsg"), DESTROY("destroyMsg"), INVALID("invalidMsg"), BLOCKED("blockMsg"),
	DEST_EMPTY("destEmpty"), DENY("denyMsg"), SPAWN_BLOCK("spawnBlockMsg"), DEDUCT("ecoDeduct"), REFUND("ecoRefund"),
	ECO_OBTAIN("ecoObtain"), LACKING_FUNDS("ecoInFunds"), CREATE("createMsg"), NET_DENY("createNetDeny"),
	GATE_DENY("createGateDeny"), CREATE_PERSONAL("createPersonal"), NAME_LENGTH_FAULT("createNameLength"),
	ALREADY_EXIST("createExists"), NET_FULL("createFull"), WORLD_DENY("createWorldDeny"),
	GATE_CONFLICT("createConflict"), RIGHT_CLICK("signRightClick"), TO_USE("signToUse"), RANDOM("signRandom"),
	DISCONNECTED("signDisconnected"), BUNGEE_DISABLED("bungeeDisabled"), BUNGEE_DENY("bungeeDeny"),
	BUNGEE_EMPTY("bungeeEmpty"), BUNGEE_SIGN("bungeeSign");

	private final String key;

	LangMsg(String key) {
		this.key = key;
	}
	public String getValue() {
		return key;
	}
	public static LangMsg getEnum(String value) {
		Stargate.log(Level.FINER, "getting enum from value: ''" + value + "''");
		for(LangMsg enumeration : values()) {
			if(enumeration.getValue().equals(value))
				return enumeration;
		}
		return null;
	}
}
