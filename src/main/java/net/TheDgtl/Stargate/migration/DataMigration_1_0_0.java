package net.TheDgtl.Stargate.migration;

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
            if(portalFolderValue != null) {
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

    private void moveFilesToDebugDirectory(String portalFolder) {
        Map<String, String> filesToMove = new HashMap<>();
        FileHelper.readInternalFileToMap("/migration/file-migrations-1_0_0.properties", filesToMove);
        filesToMove.put(portalFolder, "plugins/Stargate/debug/legacy_portals");

        for (String directoryString : filesToMove.keySet()) {
            Stargate.log(Level.FINE, String.format("Moving files in directory %s to %s", directoryString,
                    filesToMove.get(directoryString)));
            File directory = new File(directoryString);
            File targetDirectory = new File(filesToMove.get(directoryString));
            if (!directory.exists()) {
                continue;
            }
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }
            File[] files = directory.listFiles();
            for (File file : files) {
                file.renameTo(new File(targetDirectory, file.getName()));
            }
            directory.delete();
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
