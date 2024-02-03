package org.sgrewritten.stargate.migration;

import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.property.StargateConstant;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * A migrator for migrating legacy data to new formats
 */
public class DataMigrator {

    private int configVersion;
    private final File configFile;
    private Map<String, Object> configModifications;
    private final FileConfiguration fileConfig;
    private final DataMigration[] migrations;

    /**
     * Instantiates a new data migrator
     *
     * @param configurationFile <p>The configuration file to migrate to a newer format</p>
     * @param pluginFolder            <p>A server object</p>
     * @throws IOException                   <p>If unable to read or write to a file</p>
     * @throws InvalidConfigurationException <p>If unable to load the given configuration file</p>
     */
    public DataMigrator(@NotNull File configurationFile, File pluginFolder, StoredPropertiesAPI storedProperties)
            throws IOException, InvalidConfigurationException {
        // WARNING: Migrators must be defined from oldest to newest to prevent partial
        // migration
        migrations = new DataMigration[]{
                new DataMigration6(pluginFolder, storedProperties),
                new DataMigration7(),
                new DataMigration9()
        };

        //Not StargateConfiguration, as we don't want to save comments
        FileConfiguration configuration = new YamlConfiguration();
        configuration.load(configurationFile);
        this.fileConfig = configuration;
        this.configModifications = configuration.getValues(true);
        this.configVersion = configuration.getInt("configVersion", 0);
        this.configFile = configurationFile;
    }

    /**
     * Gets whether a migration is necessary
     *
     * @return <p>True if a migration is necessary</p>
     */
    public boolean isMigrationNecessary() {
        for (DataMigration migration : migrations) {
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
        for (DataMigration migration : migrations) {
            if (isMigrationNecessary(migration)) {
                configModifications = migration.getUpdatedConfigValues(configModifications);
            }
        }
        return configModifications;
    }

    /**
     * Runs all relevant config migrations
     */
    public void run(@NotNull SQLDatabaseAPI database, StargateAPI stargateAPI) {
        for (DataMigration migration : migrations) {
            if (isMigrationNecessary(migration)) {
                Stargate.log(Level.INFO, String.format("Running database migration %s -> %s", migration.getVersionFrom(), migration.getVersionTo()));
                migration.run(database, stargateAPI);
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
        for (Map.Entry<String, Object> entry: updatedConfig.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        config.set("configVersion", StargateConstant.CURRENT_CONFIG_VERSION);
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