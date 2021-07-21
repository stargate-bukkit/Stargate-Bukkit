package net.TheDgtl.Stargate;

public enum LangMsg {
	PREFIX("prefix"), TELEPORT("teleportMsg"), DESTROY("destroyMsg"), INVALID("invalidMsg"), BLOCKED("blockMsg"),
	DEST_EMPTY("destEmpty"), DENY("denyMsg"), SPAWN_BLOCK("spawnBlockMsg"), DEDUCT("ecoDeduct"), REFUND("ecoRefund"),
	ECO_OBTAIN("ecoObtain"), LACKING_FUNDS("ecoInFunds"), CREATE("createMsg"), NET_DENY("createNetDeny"),
	GATE_DENY("createGateDeny"), CREATE_PERSONAL("createPersonal"), ALREADY_EXIST("createExist"), NET_FULL("createFull"),
	WORLD_DENY("createWorldDeny"), GATE_CONFLICT("createConflict"), RIGHT_CLICK("signRightClick"), TO_USE("signToUse"),
	RANDOM("signRandom"), DISCONNECTED("signDisconnected"), BUNGEE_DISABLED("bungeeDisabled"), BUNGEE_DENY("bungeeDeny"),
	BUNGEE_EMPTY("bungeeEmpty"), BUNGEE_SIGN("bungeeSign");

	private final String key;

	LangMsg(String key) {
		this.key = key;
	}
	public String getValue() {
		return key;
	}
}
