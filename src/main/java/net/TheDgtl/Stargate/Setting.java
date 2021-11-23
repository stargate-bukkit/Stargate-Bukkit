package net.TheDgtl.Stargate;

import java.util.HashMap;

/**
 * Loads values from the configuration into an enum.
 * @author Thorin
 */
public enum Setting {
// TODO This needs to be updated per Discussion Two.
	DEFAULT_NET("defaultGateNetwork"), LANGUAGE("lang"),
	USING_BUNGEE("bungee.usingBungee"), 
	
	GATE_LIMIT("networkLimit"),
	HANDLE_VEHICLES("handleVehicles"), CHECK_PORTAL_VALIDITY("checkPortalValidity"),
	CHECK_TRAVERSIBLES("checkTraversables"), PROTECT_ENTRANCE("protectEntrance"),
	DESTROY_ON_EXPLOSION("destroyOnExplosion"),
	
	USE_ECONOMY("useEconomy"), TAX_DESTINATION("taxAccount"), UPKEEP_COST("economy.upkeepCost"),
	CREATION_COST("creationCost"), DESTROY_COST("destructionCost"), USE_COST("usageCost"),
	GATE_OWNER_REVENUE("gateOwnerRevenue"), CHARGE_FREE_DESTINATION("chargeFreeDestination"),
	
	DEFAULT_SIGN_COLOR("defaultSignColour"), HIGHLIGHT_FREE_GATES("highlightFreeGates"),
	ALPHABETIC_NETWORKS("alphabeticNetworks"), REMEMBER_LAST_DESTINATION("rememberLastDestination"),
	GATE_EXIT_SPEED_MULTIPLIER("gateExitSpeedMultiplier"),

	DEBUG_LEVEL("loggingLevel"), CONFIG_VERSION("configVersion"), DATABASE_NAME("portalFile"),
	USING_REMOTE_DATABASE("bungee.useRemoteDatabase"), SHOW_HIKARI_CONFIG("bungee.remoteDatabaseSettings.advancedDatabaseConfiguration"),
	BUNGEE_DRIVER("remoteSettings.driver"), BUNGEE_DATABASE("remoteSettings.database"),
	BUNGEE_PORT("remoteSettings.port"), BUNGEE_ADDRESS("remoteSettings.address");

	private String key;
	static private final HashMap<String,Setting> map = new HashMap<>();
	static {
		for(Setting value : values())
			map.put(value.getKey(), value);
	}

	Setting(String key) {
		this.key = key;
	}

	static Setting parse(String key) {
		for (Setting testSetting : values())
			if (testSetting.getKey().equals(key))
				return testSetting;
		return null;
	}

	String getKey() {
		return key;
	}
	
	public static int getInteger(Setting setting) {
		return Stargate.getConfigStatic().getInt(setting.getKey());
	}
	
	public static double getDouble(Setting setting) {
		return Stargate.getConfigStatic().getDouble(setting.getKey());
	}
	
	public static String getString(Setting setting) {
		return Stargate.getConfigStatic().getString(setting.getKey());
	}
	
	public static boolean getBoolean(Setting setting) {
		return Stargate.getConfigStatic().getBoolean(setting.getKey());
	}
}
