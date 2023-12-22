package org.sgrewritten.stargate.migration;

import com.google.common.io.Files;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.exception.TranslatableException;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.LegacyDataHandler;
import org.sgrewritten.stargate.util.LegacyPortalStorageLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The specification for data migration from legacy to 1.0.0
 */
public class DataMigration_1_0_0 extends DataMigration {

    private static HashMap<String, String> CONFIG_CONVERSIONS;
    private final Server server;
    private final StoredPropertiesAPI storedProperties;
    private String versionFrom;
    private Map<String, Object> oldConfig;

    /**
     * Instantiates a new Ret-Com 1.0.0
     *
     * @param server   <p>The server to use for loading legacy portals</p>
     */
    public DataMigration_1_0_0(@NotNull Server server, StoredPropertiesAPI storedProperties) {
        if (CONFIG_CONVERSIONS == null) {
            loadConfigConversions();
        }
        this.server = Objects.requireNonNull(server);
        this.storedProperties = storedProperties;
    }

    @Override
    public Map<String, Object> getUpdatedConfigValues(Map<String, Object> oldConfig) {
        Map<String, Object> newConfig = super.getUpdatedConfigValues(oldConfig);
        this.oldConfig = oldConfig;

        for (String oldMaxGatesSetting : new String[]{"maxGatesEachNetwork", "maxgates"}) {
            if (oldConfig.get(oldMaxGatesSetting) != null) {
                if ((int) oldConfig.get(oldMaxGatesSetting) == 0) {
                    newConfig.put("networkLimit", -1);
                } else {
                    newConfig.put("networkLimit", oldConfig.get(oldMaxGatesSetting));
                }
            }
        }


        if (oldConfig.get("debugging.permissionDebug") != null) {
            storedProperties.setProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE, true);
            this.versionFrom = "0.11.5.5";
            try(InputStream inputStream = Stargate.class.getResourceAsStream("/migration/paritymessage.txt")) {
                Stargate.log(Level.WARNING, "\n" + FileHelper.readStreamToString(inputStream));
            } catch (IOException e){
                Stargate.log(e);
            }
        } else {
            this.versionFrom = "~0.10.0.0";
        }

        String[] permissionDebug = {"permdebug", "debugging.permdebug", "debugging.permissionDebug"};
        Level logLevel = Level.INFO;
        String permissionDebugValue = LegacyDataHandler.findConfigKey(permissionDebug, oldConfig);
        if (permissionDebugValue != null && permissionDebugValue.equals("true")) {
            logLevel = Level.CONFIG;
        }
        String[] debug = {"debug", "debugging.debug"};
        String debugValue = LegacyDataHandler.findConfigKey(debug, oldConfig);
        if (debugValue != null && debugValue.equals("true")) {
            logLevel = Level.FINE;
        }
        newConfig.put("loggingLevel", logLevel.toString());
        return newConfig;
    }

    @Override
    public void run(@NotNull SQLDatabaseAPI database, StargateAPI stargateAPI) {
        try {
            String portalFolderName = (String) oldConfig.get(LegacyDataHandler
                    .findConfigKey(new String[]{"portal-folder", "folders.portalFolder"}, oldConfig));
            String defaultName = (String) oldConfig.get(LegacyDataHandler
                    .findConfigKey(new String[]{"gates.defaultGateNetwork", "default-gate-network"}, oldConfig));
            migratePortals(portalFolderName, defaultName, stargateAPI);
            moveFilesToDebugDirectory(portalFolderName);
        } catch (IOException | InvalidStructureException | TranslatableException e) {
            Stargate.log(e);
        } catch (NullPointerException e) {
            Stargate.log(Level.SEVERE,
                    "Invalid config: Could not get necessary config values to load portals from storage");
            Stargate.log(e);
        }
    }

    @Override
    protected TwoTuple<String, Object> getNewConfigPair(TwoTuple<String, Object> oldPair) {
        if (!CONFIG_CONVERSIONS.containsKey(oldPair.getFirstValue())) {
            return oldPair;
        }
        String newKey = CONFIG_CONVERSIONS.get(oldPair.getFirstValue());

        if (newKey == null) {
            return null;
        }

        if (oldPair.getFirstValue().equals("freegatesgreen") ||
                oldPair.getFirstValue().equals("economy.freeGatesColored")) {
            return new TwoTuple<>(newKey, 2);
        }

        if (newKey.equals("gateFolder")) {
            return new TwoTuple<>(newKey, oldPair.getSecondValue().toString().replaceAll("^plugins/Stargate/", ""));
        }


        return new TwoTuple<>(newKey, oldPair.getSecondValue());
    }

    @Override
    public String getVersionFrom() {
        return this.versionFrom;
    }

    @Override
    public String getVersionTo() {
        return "1.0.0.11";
    }

    @Override
    public int getConfigVersion() {
        return 6;
    }

    /**
     * Migrates the portals found in the given folder to the new format
     *
     * @param portalFolder <p>The portal folder to load portals from</p>
     * @throws InvalidStructureException <p>If the old portal has an invalid structure</p>
     * @throws IOException               <p>If unable to load previous portals</p>
     * @throws TranslatableException     <p>If some use input was invalid</p>
     */
    private void migratePortals(String portalFolder, String defaultNetworkName, StargateAPI stargateAPI) throws InvalidStructureException, IOException, TranslatableException {
        List<Portal> portals = LegacyPortalStorageLoader.loadPortalsFromStorage(portalFolder, server, defaultNetworkName,stargateAPI);
        if (portals == null) {
            Stargate.log(Level.WARNING, "No portals migrated!");
        } else {
            Stargate.log(Level.INFO, "The following portals have been migrated:");
            for (Portal portal : portals) {
                Stargate.log(Level.INFO, String.format("Name: %s, Network: %s, Owner: %s, Flags: %s",
                        portal.getName(), portal.getNetwork().getName(), portal.getOwnerUUID(),
                        portal.getAllFlagsString()));
            }
        }
    }

    /**
     * Moves legacy data to the debug directory to prevent confusion
     *
     * @param portalFolder <p>The folder containing all legacy portals</p>
     */
    private void moveFilesToDebugDirectory(String portalFolder) {
        Map<String, String> filesToMove = new HashMap<>();
        FileHelper.readInternalFileToMap("/migration/file-migrations-1_0_0.properties", filesToMove);
        filesToMove.put(portalFolder, "plugins/Stargate/debug/legacy_portals");

        for (String directoryString : filesToMove.keySet()) {
            moveLegacyData(directoryString, filesToMove);
        }

        Stargate instance = Stargate.getInstance();
        File gateDirectory = new File((instance != null) ? instance.getDataFolder() : new File(""), (instance != null) ? instance.getGateFolder() : "");
        if (!gateDirectory.exists()) {
            return;
        }
        File debugGateDirectory = new File((instance != null) ? instance.getGateFolder() : "", "debug/invalidGates");
        if (!debugGateDirectory.exists() && !debugGateDirectory.mkdirs()) {
            Stargate.log(Level.WARNING, "Unable to create the directory for invalid gates");
            return;
        }
        File[] gateFiles = gateDirectory.listFiles((directory, fileName) -> fileName.endsWith(".gate.invalid"));
        //Gate files being null probably happens if missing read permissions for the folder
        if (gateFiles == null) {
            Stargate.log(Level.WARNING, "Unable to list files in " + gateDirectory + ". Make sure you " +
                    "have read permission for the folder.");
            return;
        }
        for (File gateFile : gateFiles) {
            try {
                Files.copy(gateFile, new File(debugGateDirectory, gateFile.getName()));
            } catch (IOException e) {
                Stargate.log(e);
            }
        }
    }

    /**
     * Moves legacy portal data to another folder to prevent confusion
     *
     * @param directoryString <p>The directory to move in this operation</p>
     * @param filesToMove     <p>All the files that need to be moved</p>
     */
    private void moveLegacyData(String directoryString, Map<String, String> filesToMove) {
        Stargate.log(Level.FINE, String.format("Moving files in directory %s to %s", directoryString,
                filesToMove.get(directoryString)));
        File directory = new File(directoryString);
        File targetDirectory = new File(filesToMove.get(directoryString));
        if (!directory.exists()) {
            return;
        }
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            Stargate.log(Level.WARNING, "Unable to create necessary directory before moving legacy " +
                    "data. Files in " + targetDirectory + " have not been moved.");
            return;
        }
        File[] files = directory.listFiles();
        //Files being null probably happens if missing read permissions for the folder
        if (files == null) {
            Stargate.log(Level.WARNING, "Unable to list files in " + directory + ". Make sure you " +
                    "have read permission for the folder.");
            return;
        }
        boolean renameSuccessful = true;
        for (File file : files) {
            File targetFile = new File(targetDirectory, file.getName());
            if (!file.renameTo(targetFile)) {
                Stargate.log(Level.WARNING, "Unable to move the file " + file.getPath() + " to " +
                        targetFile.getPath());
                renameSuccessful = false;
            }
        }
        if (renameSuccessful) {
            if (!directory.delete()) {
                Stargate.log(Level.WARNING, "Unable to remove folder " + directory.getPath() + ". " +
                        "Make sure you have write permissions for the folder.");
            }
        }
    }

    /**
     * Loads the configuration conversions used to load legacy configuration files
     */
    private void loadConfigConversions() {
        CONFIG_CONVERSIONS = new HashMap<>();
        FileHelper.readInternalFileToMap("/migration/config-migrations-1_0_0.properties", CONFIG_CONVERSIONS);
    }

}
