package net.TheDgtl.Stargate.refactoring.retcons;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RetCon1_0_0 extends Modificator {
    /**
     * A list of every old settingname and what it changed to in this retcon
     */
    static private final HashMap<String, String> CONFIG_CONVERSIONS = new HashMap<>();

    static {
        /*
         *  Dinnerbone
         *  TODO: convert old ass database
         */
        CONFIG_CONVERSIONS.put("portal-save-location", null);
        CONFIG_CONVERSIONS.put("teleportMessage", null);
        CONFIG_CONVERSIONS.put("registerMessage", null);
        CONFIG_CONVERSIONS.put("destroyzMessage", null);
        CONFIG_CONVERSIONS.put("noownersMessage", null);
        CONFIG_CONVERSIONS.put("unselectMessage", null);
        CONFIG_CONVERSIONS.put("collisinMessage", null);
        CONFIG_CONVERSIONS.put("cantAffordToUse", null);
        CONFIG_CONVERSIONS.put("cantAffordToNew", null);
        CONFIG_CONVERSIONS.put("defaultNetwork", "defaultGateNetwork");
        CONFIG_CONVERSIONS.put("use-mysql", "useRemoteDatabase");
        CONFIG_CONVERSIONS.put("portal-open", null);
        CONFIG_CONVERSIONS.put("portal-closed", null);
        CONFIG_CONVERSIONS.put("cost-type", null);
        CONFIG_CONVERSIONS.put("cost-to-use", "usageCost");
        CONFIG_CONVERSIONS.put("cost-to-create", "creationCost");
        CONFIG_CONVERSIONS.put("cost-to-activate", null);
        CONFIG_CONVERSIONS.put("cost-destination", "chargeFreeDestination");


        // Drakia
        CONFIG_CONVERSIONS.put("lang", "language");
        CONFIG_CONVERSIONS.put("enableBungee", "bungee.usingBungee");
        CONFIG_CONVERSIONS.put("default-gate-network", "defaultGateNetwork");
        CONFIG_CONVERSIONS.put("maxgates", "networkLimit");
        CONFIG_CONVERSIONS.put("ignoreEntrance", null);
        CONFIG_CONVERSIONS.put("destroyexplosion", "destroyOnExplosion");
        CONFIG_CONVERSIONS.put("useiconomy", "useEconomy");
        CONFIG_CONVERSIONS.put("createcost", "creationCost");
        CONFIG_CONVERSIONS.put("destroycost", "destructionCost");
        CONFIG_CONVERSIONS.put("usecost", "usageCost");
        CONFIG_CONVERSIONS.put("toowner", "gateOwnerRevenue");
        CONFIG_CONVERSIONS.put("chargefreedestination", "chargeFreeDestination");
        CONFIG_CONVERSIONS.put("signColor", null);
        CONFIG_CONVERSIONS.put("freegatesgreen", "signStyle.listing");
        CONFIG_CONVERSIONS.put("sortLists", "alphabeticNetworks");
        CONFIG_CONVERSIONS.put("portal-folder", null);
        CONFIG_CONVERSIONS.put("gate-folder", null);
        CONFIG_CONVERSIONS.put("debug", null);
        CONFIG_CONVERSIONS.put("permdebug", null);
        CONFIG_CONVERSIONS.put("destMemory", "rememberLastDestination");

        // PseudoKnight
        CONFIG_CONVERSIONS.put("verifyPortals", "checkPortalValidity");
        CONFIG_CONVERSIONS.put("useeconomy", "useEconomy");

        // LCLO
        CONFIG_CONVERSIONS.put("taxaccount", "taxAccount");

        // EpicKnarvik97
        CONFIG_CONVERSIONS.put("adminUpdateAlert", null);
        CONFIG_CONVERSIONS.put("folders.portalFolder", null);
        CONFIG_CONVERSIONS.put("gates.maxGatesEachNetwork", "networkLimit");
        CONFIG_CONVERSIONS.put("gates.defaultGateNetwork", "defaultGateNetwork");
        CONFIG_CONVERSIONS.put("gates.cosmetic.rememberDestination", "rememberLastDestination");
        CONFIG_CONVERSIONS.put("gates.cosmetic.sortNetworkDestinations", "alphabeticNetworks");
        CONFIG_CONVERSIONS.put("gates.cosmetic.mainSignColor", "signStyle.defaultForeground");
        CONFIG_CONVERSIONS.put("gates.cosmetic.highlightSignColor", "signStyle.defaultBackground");
        CONFIG_CONVERSIONS.put("gates.integrity.destroyedByExplosion", "destroyOnExplosion");
        CONFIG_CONVERSIONS.put("gates.integrity.verifyPortals", "checkPortalValidity");
        CONFIG_CONVERSIONS.put("gates.integrity.protectEntrance", "protectEntrance");
        CONFIG_CONVERSIONS.put("gates.functionality.enableBungee", "bungee.usingBungee");
        CONFIG_CONVERSIONS.put("gates.functionality.handleVehicles", "handleVehicles");
        CONFIG_CONVERSIONS.put("gates.functionality.handleEmptyVehicles", null);
        CONFIG_CONVERSIONS.put("gates.functionality.handleCreatureTransportation", null);
        CONFIG_CONVERSIONS.put("gates.functionality.handleNonPlayerVehicles", null);
        CONFIG_CONVERSIONS.put("gates.functionality.handleLeashedCreatures", "handleLeashedCreatures");
        CONFIG_CONVERSIONS.put("economy.useEconomy", "useEconomy");
        CONFIG_CONVERSIONS.put("economy.createCost", "creationCost");
        CONFIG_CONVERSIONS.put("economy.destroyCost", "destructionCost");
        CONFIG_CONVERSIONS.put("economy.useCost", "usageCost");
        CONFIG_CONVERSIONS.put("economy.toOwner", "gateOwnerRevenue");
        CONFIG_CONVERSIONS.put("economy.chargeFreeDestination", "chargeFreeDestination");
        CONFIG_CONVERSIONS.put("economy.freeGatesColor", null);
        CONFIG_CONVERSIONS.put("economy.freeGatesColored", "signStyle.listing");

        // cybertiger
        CONFIG_CONVERSIONS.put("enableEconomy", "useEconomy");
    }

    @Override
    public Map<String, Object> run(Map<String, Object> oldConfig) {
        try {
            String[] possiblePortalFolders = {
                    "portal-folder",
                    "folders.portalFolder"};
            for (String portalFolder : possiblePortalFolders) {
                if (oldConfig.get(portalFolder) != null) {
                    LegacyPortalStorageLoader.loadPortalsFromStorage((String) oldConfig.get(portalFolder));
                    break;
                }
            }
        } catch (IOException e) {}

        Level logLevel = Level.INFO;
        if ((oldConfig.get("permdebug") != null && (boolean) oldConfig.get("permdebug"))
                || (oldConfig.get("debugging.permdebug") != null) && (boolean) oldConfig.get("debugging.permdebug"))
            logLevel = Level.CONFIG;
        if ((oldConfig.get("debug") != null && (boolean) oldConfig.get("debug"))
                || (oldConfig.get("debugging.debug") != null && (boolean) oldConfig.get("debugging.debug")))
            logLevel = Level.FINE;
        Map<String, Object> newConfig = super.run(oldConfig);
        newConfig.put("loggingLevel", logLevel.toString());
        return newConfig;
    }

    @Override
    protected SettingSet getNewSetting(SettingSet oldSetting) {
        if (!CONFIG_CONVERSIONS.containsKey(oldSetting.key)) {
            return oldSetting;
        }
        String newKey = CONFIG_CONVERSIONS.get(oldSetting.key);

        if (newKey == null)
            return null;

        if (oldSetting.key.equals("freegatesgreen") || oldSetting.key.equals("economy.freeGatesColored"))
            return new SettingSet(newKey, 2);

        return new SettingSet(newKey, oldSetting.value);
    }

    @Override
    public int getConfigNumber() {
        return 6;
    }

}
