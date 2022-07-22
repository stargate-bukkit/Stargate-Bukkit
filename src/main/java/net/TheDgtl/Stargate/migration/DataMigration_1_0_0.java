package net.TheDgtl.Stargate.migration;

import com.google.common.io.Files;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.container.TwoTuple;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.network.StargateRegistry;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.util.FileHelper;
import net.TheDgtl.Stargate.util.LegacyPortalStorageLoader;
import org.bukkit.Server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The specification for data migration from legacy to 1.0.0
 */
public class DataMigration_1_0_0 extends DataMigration {

    private static HashMap<String, String> CONFIG_CONVERSIONS;
    private final Server server;
    private final StargateRegistry registry;
    private Map<String, Object> oldConfig;
    private final StargateLogger logger;

    /**
     * Instantiates a new Ret-Com 1.0.0
     *
     * @param server   <p>The server to use for loading legacy portals</p>
     * @param registry <p>The stargate registry to register loaded portals to</p>
     * @param logger   <p>The logger to use for logging any messages</p>
     */
    public DataMigration_1_0_0(Server server, StargateRegistry registry, StargateLogger logger) {
        if (CONFIG_CONVERSIONS == null) {
            loadConfigConversions();
        }
        this.server = server;
        this.registry = registry;
        this.logger = logger;
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
                    newConfig.put("networkLimit", (int) oldConfig.get(oldMaxGatesSetting) == 0);
                }
            }
        }


        Level logLevel = Level.INFO;
        if ((oldConfig.get("permdebug") != null && (boolean) oldConfig.get("permdebug")) ||
                (oldConfig.get("debugging.permdebug") != null) && (boolean) oldConfig.get("debugging.permdebug")) {
            logLevel = Level.CONFIG;
        }
        if ((oldConfig.get("debug") != null && (boolean) oldConfig.get("debug")) ||
                (oldConfig.get("debugging.debug") != null && (boolean) oldConfig.get("debugging.debug"))) {
            logLevel = Level.FINE;
        }
        newConfig.put("loggingLevel", logLevel.toString());
        return newConfig;
    }

    @Override
    public void run() {
        String portalFolderValue = null;
        try {
            String[] possiblePortalFolderConfigKeys = {"portal-folder", "folders.portalFolder"};
            for (String portalFolderKey : possiblePortalFolderConfigKeys) {
                portalFolderValue = (String) oldConfig.get(portalFolderKey);
                if (portalFolderValue != null) {
                    migratePortals(portalFolderValue);
                    break;
                }
            }
            if (portalFolderValue != null) {
                moveFilesToDebugDirectory(portalFolderValue);
            }
        } catch (IOException | InvalidStructureException | NameErrorException e) {
            e.printStackTrace();
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

        return new TwoTuple<>(newKey, oldPair.getSecondValue());
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
     * @throws NameErrorException        <p>If the old portal has an invalid name</p>
     * @throws IOException               <p>If unable to load previous portals</p>
     */
    private void migratePortals(String portalFolder) throws InvalidStructureException, NameErrorException, IOException {
        List<Portal> portals = LegacyPortalStorageLoader.loadPortalsFromStorage(portalFolder, server, registry, logger);
        if (portals == null) {
            logger.logMessage(Level.WARNING, "No portals migrated!");
        } else {
            logger.logMessage(Level.INFO, "The following portals have been migrated:");
            for (Portal portal : portals) {
                logger.logMessage(Level.INFO, String.format("Name: %s, Network: %s, Owner: %s, Flags: %s",
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
            moveLegacyPortals(directoryString, filesToMove);
        }

        File gateDirectory = new File(Stargate.getInstance().getDataFolder(), Stargate.getInstance().getGateFolder());
        if (!gateDirectory.exists()) {
            return;
        }
        File debugGateDirectory = new File(Stargate.getInstance().getDataFolder(), "debug/invalidGates");
        if (!debugGateDirectory.exists() && !debugGateDirectory.mkdirs()) {
            logger.logMessage(Level.WARNING, "Unable to create the directory for invalid gates");
            return;
        }
        File[] gateFiles = gateDirectory.listFiles((directory, fileName) -> fileName.endsWith(".gate.invalid"));
        //Gate files being null probably happens if missing read permissions for the folder
        if (gateFiles == null) {
            logger.logMessage(Level.WARNING, "Unable to list files in " + gateDirectory + ". Make sure you " +
                    "have read permission for the folder.");
            return;
        }
        for (File gateFile : gateFiles) {
            try {
                Files.copy(gateFile, new File(debugGateDirectory, gateFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Moves legacy portal data to another folder to prevent confusion
     *
     * @param directoryString <p>The directory to move in this operation</p>
     * @param filesToMove     <p>All the files that need to be moved</p>
     */
    private void moveLegacyPortals(String directoryString, Map<String, String> filesToMove) {
        Stargate.log(Level.FINE, String.format("Moving files in directory %s to %s", directoryString,
                filesToMove.get(directoryString)));
        File directory = new File(directoryString);
        File targetDirectory = new File(filesToMove.get(directoryString));
        if (!directory.exists()) {
            return;
        }
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            logger.logMessage(Level.WARNING, "Unable to create necessary directory before moving legacy " +
                    "data. Files in " + targetDirectory + " have not been moved.");
            return;
        }
        File[] files = directory.listFiles();
        //Files being null probably happens if missing read permissions for the folder
        if (files == null) {
            logger.logMessage(Level.WARNING, "Unable to list files in " + directory + ". Make sure you " +
                    "have read permission for the folder.");
            return;
        }
        boolean renameSuccessful = true;
        for (File file : files) {
            File targetFile = new File(targetDirectory, file.getName());
            if (!file.renameTo(targetFile)) {
                logger.logMessage(Level.WARNING, "Unable to move the file " + file.getPath() + " to " +
                        targetFile.getPath());
                renameSuccessful = false;
            }
        }
        if (renameSuccessful) {
            if (!directory.delete()) {
                logger.logMessage(Level.WARNING, "Unable to remove folder " + directory.getPath() + ". " +
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
