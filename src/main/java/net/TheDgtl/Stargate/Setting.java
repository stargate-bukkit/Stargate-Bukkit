package net.TheDgtl.Stargate;

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

	String key;

	Setting(String key) {
		this.key = key;
	}

	static Setting getSetting(String key) {
		for (Setting testSetting : values())
			if (testSetting.getKey().equals(key))
				return testSetting;
		return null;
	}

	String getKey() {
		return key;
	}
}
