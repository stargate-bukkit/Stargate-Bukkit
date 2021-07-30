package net.TheDgtl.Stargate;

import java.util.HashMap;

public enum Setting {
	DEFAULT_NET("preferences.defaultGateNetwork"), LANGUAGE("preferences.language"),
	USING_BUNGEE("preferences.usingBungee"), 
	
	GATE_LIMIT("behaviour.gateLimit"),
	HANDLE_VEHICLES("behaviour.handleVehicles"), CHECK_PORTAL_VALIDITY("behaviour.checkPortalValidity"),
	CHECK_TRAVERSIBLES("behaviour.checkTraversables"), PROTECT_ENTRANCE("behaviour.protectEntrance"),
	DESTROY_ON_EXPLOSION("behaviour.destroyOnExplosion"),
	
	USE_ECONOMY("economy.useEconomy"), TAX_DESTINATION("economy.taxDestination"), UPKEEP_COST("economy.upkeepCost"),
	CREATION_COST("economy.creationCost"), DESTROY_COST("economy.destroyCost"), USE_COST("economy.useCost"),
	GATE_OWNER_REVENUE("economy.gateOwnerRevenue"), CHARGE_FREE_DESTINATION("economy.chargeFreeDestination"),
	
	DEFAULT_SIGN_COLOR("tweaks.defaultSignColour"), HIGHLIGHT_FREE_GATES("tweaks.highlightFreeGates"),
	ALPHABETIC_NETWORKS("tweaks.alphabeticNetworks"), REMEMBER_LAST_DESTINATION("tweaks.rememberLastDestination"),
	
	DEBUG_LEVEL("technical.debug.debug-level"), CONFIG_VERSION("configVersion");

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
}
