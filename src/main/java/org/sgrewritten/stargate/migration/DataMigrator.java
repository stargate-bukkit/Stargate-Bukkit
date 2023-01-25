package org.sgrewritten.stargate.migration;

import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.network.RegistryAPI;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A migrator for migrating legacy data to new formats
 */
public class DataMigrator {

    private int configVersion;
    private final File configFile;
    private Map<String, Object> configModifications;
    private final FileConfiguration fileConfig;
    private final DataMigration[] MIGRATIONS;

    /**
     * Instantiates a new data migrator
     *
     * @param configurationFile <p>The configuration file to migrate to a newer format</p>
     * @param logger            <p>The logger used for logging any error or debug messages</p>
     * @param server            <p>A server object</p>
     * @param registry          <p>The registry that manages all portals</p>
     * @throws IOException                   <p>If unable to read or write to a file</p>
     * @throws InvalidConfigurationException <p>If unable to load the given configuration file</p>
     */
    public DataMigrator(@NotNull File configurationFile, @NotNull StargateLogger logger, @NotNull Server server,
                        @NotNull RegistryAPI registry, @NotNull LanguageManager languageManager,
                        @NotNull StargateEconomyAPI economyManager, @NotNull StoredPropertiesAPI properties)
            throws IOException, InvalidConfigurationException {
        // WARNING: Migrators must be defined from oldest to newest to prevent partial
        // migration
        MIGRATIONS = new DataMigration[]{
                new DataMigration_1_0_0(server, registry, logger, languageManager, economyManager, properties),
                new DataMigration_1_0_14()
        };

        //Not StargateConfiguration, as we don't want to save comments
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.load(configurationFile);
        this.fileConfig = fileConfig;
        this.configModifications = fileConfig.getValues(true);
        this.configVersion = fileConfig.getInt("configVersion", 0);
        this.configFile = configurationFile;
    }

    /**
     * Gets whether a migration is necessary
     *
     * @return <p>True if a migration is necessary</p>
     */
    public boolean isMigrationNecessary() {
        for (DataMigration migration : MIGRATIONS) {
            if (isMigrationNecessary(migration)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets updated configuration values
     *
     * <p>Some configuration values may be lost during the config migration</p>
     *
     * @return <p>The update configuration values</p>
     */
    public Map<String, Object> getUpdatedConfig() {
        for (DataMigration migration : MIGRATIONS) {
            if (isMigrationNecessary(migration)) {
                configModifications = migration.getUpdatedConfigValues(configModifications);
            }
        }
        return configModifications;
    }

    /**
     * Runs all relevant config migrations
     */
    public void run(@NotNull SQLDatabaseAPI database) {
        for (DataMigration migration : MIGRATIONS) {
            if (isMigrationNecessary(migration)) {
                migration.run(database);
                configVersion = migration.getConfigVersion();
            }
        }
    }

    /**
     * Updates the given file configuration with the given updated config values
     *
     * @param config        <p>The file configuration to update</p>
     * @param updatedConfig <p>The new configuration values to write</p>
     * @throws IOException                   <p>If unable to load the file configuration</p>
     * @throws InvalidConfigurationException <p>If unable to load the file configuration</p>
     */
    public void updateFileConfiguration(FileConfiguration config, Map<String, Object> updatedConfig) throws IOException,
            InvalidConfigurationException {
        fileConfig.load(configFile);
        for (String configKey : updatedConfig.keySet()) {
            config.set(configKey, updatedConfig.get(configKey));
        }
        config.set("configVersion", Stargate.getCurrentConfigVersion());
        config.save(configFile);
    }

    /**
     * Gets whether a migration is necessary for the given data migration
     *
     * @param dataMigration <p>The data migration to check</p>
     * @return <p>True if a migration is necessary</p>
     */
    private boolean isMigrationNecessary(DataMigration dataMigration) {
        return dataMigration.getConfigVersion() > configVersion;
    }

}