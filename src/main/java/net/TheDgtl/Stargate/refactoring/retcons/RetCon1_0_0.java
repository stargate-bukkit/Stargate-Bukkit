package net.TheDgtl.Stargate.refactoring.retcons;

import java.util.HashMap;

public class RetCon1_0_0 extends Modificator {
    /**
     * A list of every old settingname and what it changed to in this retcon
     */
    static private final HashMap<String, String> CONFIG_CONVERSIONS = new HashMap<>();

    static {
        CONFIG_CONVERSIONS.put("enableBungee", "bungee.usingBungee");
        CONFIG_CONVERSIONS.put("default-gate-network", "defaultGateNetwork");
        CONFIG_CONVERSIONS.put("maxgates", "networkLimit");
        CONFIG_CONVERSIONS.put("verifyPortals", "checkPortalValidity");
        CONFIG_CONVERSIONS.put("ignoreEntrance", "checkTraversables");
        CONFIG_CONVERSIONS.put("destroyexplosion", "destroyOnExplosion");
        CONFIG_CONVERSIONS.put("useeconomy", "useEconomy");
        CONFIG_CONVERSIONS.put("createcost", "creationCost");
        CONFIG_CONVERSIONS.put("destroycost", "destructionCost");
        CONFIG_CONVERSIONS.put("usecost", "usageCost");
        CONFIG_CONVERSIONS.put("toowner", "gateOwnerRevenue");
        CONFIG_CONVERSIONS.put("taxaccount", "taxAccount");
        CONFIG_CONVERSIONS.put("chargefreedestination", "chargeFreeDestination");
        CONFIG_CONVERSIONS.put("signColor", null);
        CONFIG_CONVERSIONS.put("freegatesgreen", null);
        CONFIG_CONVERSIONS.put("sortLists", "alphabeticNetworks");
        CONFIG_CONVERSIONS.put("portal-folder", null);
        CONFIG_CONVERSIONS.put("gate-folder", null);
        CONFIG_CONVERSIONS.put("debug", null);
        CONFIG_CONVERSIONS.put("permdebug", null);


    }

    @Override
    protected Object[] getNewSetting(Object[] oldSetting) {
        String newKey = CONFIG_CONVERSIONS.get(oldSetting[0]);
        if (newKey != null)
            return new Object[]{newKey, oldSetting[1]};
        return null;
    }

    @Override
    public int getConfigNumber() {
        return 6;
    }

}
