package org.sgrewritten.stargate.migration;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockBukkitInject;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import net.joshka.junit.json.params.JsonFileSource;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateAPIMock;
import org.sgrewritten.stargate.StargateExtension;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.config.StargateYamlConfiguration;
import org.sgrewritten.stargate.database.SQLDatabase;
import org.sgrewritten.stargate.database.SQLiteDatabase;
import org.sgrewritten.stargate.database.property.PropertiesDatabaseMock;
import org.sgrewritten.stargate.database.property.StoredProperty;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.util.StargateTestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@ExtendWith(StargateExtension.class)
class DataMigratorTest {

    @MockBukkitInject
    private ServerMock server;
    private File workFolder;
    private File defaultConfigFile;
    private File databaseFile;
    private StargateRegistry registry;
    private SQLiteDatabase sqlDatabase;
    private StargateAPIMock stargateAPI;
    private File configFile;
    private File oldDatabaseFile;
    private PropertiesDatabaseMock properties;

    @BeforeEach
    void setup() throws SQLException {
        Plugin plugin = MockBukkit.createMockPlugin();
        this.workFolder = plugin.getDataFolder();
        this.defaultConfigFile = new File("src/main/resources", "config.yml");
        this.databaseFile = new File(workFolder, "stargate.db");
        this.properties = new PropertiesDatabaseMock();
        this.sqlDatabase = new SQLiteDatabase(databaseFile);
        StorageAPI storageAPI = new SQLDatabase(sqlDatabase, false, false, properties);
        this.registry = new StargateRegistry(storageAPI, new BlockHandlerResolver(storageAPI));
        this.stargateAPI = new StargateAPIMock(storageAPI, registry);
        server.addPlayer(new PlayerMock(server, "Thorinwasher", UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919")));
    }

    @ParameterizedTest
    @JsonFileSource(resources = "/migrationConfigCheck.json")
    void runMigration(JsonObject data) throws IOException, InvalidConfigurationException {
        insertFiles(data);
        server.addSimpleWorld(data.getString("world"));
        DataMigrator dataMigrator = new DataMigrator(configFile, workFolder, properties);
        doConfigMigration(data, dataMigrator);
        otherRefactor(dataMigrator);
        portalRegistryCheck(data);
        nagKnarvikCheck(data);
    }

    private void nagKnarvikCheck(JsonObject data) {
        if (data.getBoolean("nagKnarvik")) {
            Assertions.assertEquals("true", properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
        } else {
            Assertions.assertNull(properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
        }
    }

    private void doConfigMigration(JsonObject data, DataMigrator dataMigrator) throws IOException, InvalidConfigurationException {
        Map<String, Object> config = dataMigrator.getUpdatedConfig();
        Files.copy(defaultConfigFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        FileConfiguration fileConfiguration = new StargateYamlConfiguration();
        fileConfiguration.load(configFile);

        for (String key : config.keySet()) {
            /*
             * Checks if no weird keys got added to the configuration
             */
            Assertions.assertTrue(fileConfiguration.contains(key) || key.contains(StargateYamlConfiguration.START_OF_COMMENT),
                    String.format("The key %s was added to the new config of %s", key, configFile.getName()));
        }


        dataMigrator.updateFileConfiguration(fileConfiguration, config);
        fileConfiguration.load(configFile);
        JsonObject expectedConfigData = data.getJsonObject("configOptions");

        for (ConfigurationOption option : ConfigurationOption.values()) {
            if (option.isHidden()) {
                continue;
            }
            Assertions.assertTrue(fileConfiguration.getKeys(true).contains(option.getConfigNode()), String.format("The option %s is missing in the configuration", option.getConfigNode()));
        }
        for (String key : expectedConfigData.keySet()) {
            JsonValue.ValueType valueType = expectedConfigData.get(key).getValueType();
            switch (valueType) {
                case TRUE -> Assertions.assertTrue(fileConfiguration.getBoolean(key));
                case FALSE -> Assertions.assertFalse(fileConfiguration.getBoolean(key));
                case NUMBER -> Assertions.assertEquals(expectedConfigData.getInt(key), fileConfiguration.getInt(key));
                case STRING ->
                        Assertions.assertEquals(expectedConfigData.getString(key), fileConfiguration.getString(key));
                default -> throw new IllegalStateException("Unexpected datatype: " + valueType);
            }
        }
    }

    private void otherRefactor(DataMigrator dataMigrator) {
        StargateTestHelper.runAllTasks();
        Assertions.assertDoesNotThrow(() -> dataMigrator.run(sqlDatabase, stargateAPI));
        StargateTestHelper.runAllTasks();
    }

    private void portalStorageCheck(JsonObject data) throws SQLException {
        try (Connection conn = sqlDatabase.getConnection()) {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Portal;");
            ResultSet set = statement.executeQuery();
            ResultSetMetaData meta = set.getMetaData();
            int count = 0;
            while (set.next()) {
                count++;
                StringBuilder msg = new StringBuilder();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    msg.append(meta.getColumnLabel(i)).append(":").append(set.getObject(i)).append(",");
                }
                Stargate.log(Level.FINE, msg.toString());
            }
            Assertions.assertTrue(count > 0, "There was no portals loaded from old database");
        }
    }

    void portalRegistryCheck(JsonObject data) {
        JsonArray expectedPortalsData = data.getJsonArray("portals");
        for (JsonValue portalValue : expectedPortalsData) {
            JsonObject portalObject = portalValue.asJsonObject();
            String portalName = portalObject.getString("name");
            String netName = portalObject.getString("networkID");
            Network net = registry.getNetwork(netName, StorageType.LOCAL);
            Assertions.assertNotNull(net, String.format("Network %s for portal %s was null", netName, portalName));
            Portal portal = net.getPortal(portalName);
            Assertions.assertNotNull(portal, String.format("Portal %s in network %s was null", portalName, netName));
        }
    }


    private void insertFiles(JsonObject data) throws IOException {
        File configFileFrom = new File("src/test/resources/configurations", data.getString("configLocation"));
        File portalsFolder = new File(workFolder, "portals");
        if(!portalsFolder.exists() && !portalsFolder.mkdirs()){
            throw new IOException("Unable to create directory: " + portalsFolder);
        }
        this.configFile = new File(workFolder, "config.yml");
        Files.copy(configFileFrom.toPath(), configFile.toPath());
        File databaseFrom = new File("src/test/resources/oldsaves", data.getString("database"));
        this.oldDatabaseFile = new File(portalsFolder, databaseFrom.getName());
        Files.copy(databaseFrom.toPath(), oldDatabaseFile.toPath());
    }
}
