package org.sgrewritten.stargate.migration;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.config.StargateYamlConfiguration;
import org.sgrewritten.stargate.container.TwoTuple;
import org.sgrewritten.stargate.database.SQLDatabase;
import org.sgrewritten.stargate.database.SQLDatabaseAPI;
import org.sgrewritten.stargate.database.SQLiteDatabase;
import org.sgrewritten.stargate.database.StorageAPI;
import org.sgrewritten.stargate.database.property.FakePropertiesDatabase;
import org.sgrewritten.stargate.database.property.PropertiesDatabase;
import org.sgrewritten.stargate.database.property.StoredPropertiesAPI;
import org.sgrewritten.stargate.database.property.StoredProperty;
import org.sgrewritten.stargate.economy.FakeEconomyManager;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.network.LocalNetwork;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.util.FakeLanguageManager;
import org.sgrewritten.stargate.util.FileHelper;
import org.sgrewritten.stargate.util.LegacyDataHandler;
import org.sgrewritten.stargate.util.SQLTestHelper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataMigratorTest {

    private static File sqlDatabaseFile;
    static private File[] configFiles;
    static private StargateLogger logger;
    static private File defaultConfigFile;
    static private SQLDatabaseAPI sqlDatabase;
    static private final Map<String, DataMigrator> migratorMap = new HashMap<>();
    static private final Map<String, StoredPropertiesAPI> propertiesMap = new HashMap<>();
    static private Map<String, TwoTuple<Map<String, Object>, Map<String, String>>> configTestMap;
    private static final File testGatesDir = new File("src/test/resources/gates");

    static private ServerMock server;
    private static StargateRegistry registry;

    @BeforeAll
    public static void setUp() throws IOException, InvalidConfigurationException, SQLException {
        Stargate.setLogLevel(Level.WARNING);
        String configFolder = "src/test/resources/configurations";
        configTestMap = getSettingTestMaps();
        configFiles = new File[configTestMap.size()];
        int i = 0;
        for (String key : configTestMap.keySet()) {
            configFiles[i++] = new File(configFolder, key);
        }

        logger = new FakeStargateLogger();
        defaultConfigFile = new File("src/main/resources", "config.yml");
        sqlDatabaseFile = new File("src/test/resources", "migrate-test.db");
        sqlDatabase = new SQLiteDatabase(sqlDatabaseFile);
        StorageAPI storageAPI = new SQLDatabase(sqlDatabase, false, false, logger, new FakeLanguageManager());
        registry = new StargateRegistry(storageAPI);


        defaultConfigFile = new File("src/main/resources", "config.yml");
        server = MockBukkit.mock();
        server.addSimpleWorld("epicknarvik");
        server.addSimpleWorld("lclo");
        server.addSimpleWorld("pseudoknigth");
        server.addPlayer(new PlayerMock(server,"Thorinwasher",UUID.fromString("d2b440c3-edde-4443-899e-6825c31d0919")));
        Stargate.getFileConfiguration().load(defaultConfigFile);

        GateFormatHandler.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(testGatesDir, logger)));
    }

    private static Map<String, TwoTuple<Map<String, Object>, Map<String, String>>> getSettingTestMaps() {
        Map<String, TwoTuple<Map<String, Object>, Map<String, String>>> output = new HashMap<>();

        Map<String, Object> knarvikConfigChecks = new HashMap<>();
        knarvikConfigChecks.put("defaultGateNetwork", "knarvik");
        knarvikConfigChecks.put("handleVehicles", false);
        Map<String, String> knarvikPortalChecks = new HashMap<>();
        knarvikPortalChecks.put("§6knarvik1", LocalNetwork.DEFAULT_NET_ID);
        knarvikPortalChecks.put("knarvik2", LocalNetwork.DEFAULT_NET_ID);
        knarvikPortalChecks.put("knarvik3", LocalNetwork.DEFAULT_NET_ID);
        TwoTuple<Map<String, Object>, Map<String, String>> knarvikChecks = new TwoTuple<>(knarvikConfigChecks,
                knarvikPortalChecks);
        output.put("config-epicknarvik.yml", knarvikChecks);

        Map<String, Object> pseudoConfigChecks = new HashMap<>();
        pseudoConfigChecks.put("defaultGateNetwork", "pseudoknight");
        pseudoConfigChecks.put("destroyOnExplosion", true);
        Map<String, String> pseudoPortalChecks = new HashMap<>();
        pseudoPortalChecks.put("pseudo1", "pseudo");
        pseudoPortalChecks.put("pseudo2", "pseudo");
        TwoTuple<Map<String, Object>, Map<String, String>> pseudoChecks = new TwoTuple<>(pseudoConfigChecks,
                pseudoPortalChecks);
        output.put("config-pseudoknight.yml", pseudoChecks);

        Map<String, Object> lcloConfigChecks = new HashMap<>();
        lcloConfigChecks.put("defaultGateNetwork", "lclco");
        Map<String, String> lcloPortalChecks = new HashMap<>();
        lcloPortalChecks.put("§6lclo1", "d2b440c3-edde-4443-899e-6825c31d0919");
        lcloPortalChecks.put("lclo2", "lclo");
        TwoTuple<Map<String, Object>, Map<String, String>> lcloChecks = new TwoTuple<>(lcloConfigChecks,
                lcloPortalChecks);
        output.put("config-lclo.yml", lcloChecks);


        output.put("config-legacyOldest.yml", new TwoTuple<>(new HashMap<>(), new HashMap<>()));

        return output;
    }

    @AfterAll
    public static void tearDown() throws IOException, SQLException, InvalidConfigurationException, InterruptedException {
        MockBukkit.unmock();
        sqlDatabase.getConnection().close();
        
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (!oldConfigFile.exists()) {
                continue;
            }
            if (!configFile.delete()) {
                throw new IOException("Unable to delete test-generated config file");
            }
            if (!oldConfigFile.renameTo(configFile)) {
                throw new IOException("Unable to rename backup config file to config file");
            }
            FileConfiguration fileConfig = new YamlConfiguration();
            fileConfig.load(configFile);
        }
        Map<String,String> fileMovements = new HashMap<>();
        fileMovements.put("plugins/Stargate/debug/legacy_portals/epicknarvik.db", "src/test/resources/oldsaves/epicknarvik/epicknarvik.db");
        fileMovements.put("plugins/Stargate/debug/legacy_portals/lclo.db", "src/test/resources/oldsaves/lclo/lclo.db");
        fileMovements.put("plugins/Stargate/debug/legacy_portals/pseudoknigth.db", "src/test/resources/oldsaves/pseudoknight/pseudoknigth.db");
        
        for(String fileToMoveName : fileMovements.keySet()) {
            File fileToMove = new File(fileToMoveName);
            File destination = new File(fileMovements.get(fileToMoveName));
            Stargate.log(Level.FINER,destination.getAbsolutePath());
            destination.getParentFile().mkdirs();
            fileToMove.renameTo(destination);
        }
        
        if (sqlDatabaseFile.exists() && !sqlDatabaseFile.delete()) {
            throw new IOException("Unable to remove database file");
        }
        Stargate.setLogLevel(Level.INFO);
    }

    @Test
    @Order(1)
    public void convertConfigCheck() throws IOException, InvalidConfigurationException {
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldConfigFile.exists() && !oldConfigFile.delete()) {
                throw new IOException("Unable to delete old config file");
            }
            FakePropertiesDatabase properties = new FakePropertiesDatabase();
            DataMigrator dataMigrator = new DataMigrator(configFile, logger, server, registry, new FakeLanguageManager(), new FakeEconomyManager(),properties);
            if (!configFile.renameTo(oldConfigFile)) {
                throw new IOException("Unable to rename existing config for backup");
            }

            Map<String, Object> config = dataMigrator.getUpdatedConfig();
            Files.copy(defaultConfigFile, configFile);
            FileConfiguration fileConfig = new StargateYamlConfiguration();
            fileConfig.load(configFile);
            for (String key : config.keySet()) {
                Assertions.assertTrue(
                        fileConfig.getKeys(true).contains(key) || key.contains(StargateYamlConfiguration.START_OF_COMMENT), String.format("The key %s was added to the new config of %s", key, configFile.getName()));
            }

            dataMigrator.updateFileConfiguration(fileConfig, config);
            migratorMap.put(configFile.getName(), dataMigrator);
            propertiesMap.put(configFile.getName(), properties);
            fileConfig.load(configFile);

            for (ConfigurationOption option : ConfigurationOption.values()) {
                if (option.isHidden()) {
                    continue;
                }
                Assertions.assertTrue(fileConfig.getKeys(true).contains(option.getConfigNode()), String.format("The option %s is missing in the configuration", option.getConfigNode()));
            }
            
            logger.logMessage(Level.FINEST, "End config for file '" + configFile.getName() + "': \n" + fileConfig.saveToString());
        }
    }

    @ParameterizedTest
    @MethodSource("getTestedConfigNames")
    @Order(2)
    public void doOtherRefactorCheck(String key) throws SQLException {
        Stargate.log(Level.FINE,
                String.format("####### Performing misc. refactoring based on the config-file %s%n", key));
        DataMigrator dataMigrator = migratorMap.get(key);
        Connection connection = sqlDatabase.getConnection();
        SQLTestHelper.printTableInfo(Level.WARNING, "PortalPosition", connection, false);
        connection.close();
        dataMigrator.run(sqlDatabase);
    }

    @Test
    @Order(3)
    public void configDoubleCheck() throws IOException, InvalidConfigurationException {
        for (File configFile : configFiles) {
            Map<String, Object> testMap = configTestMap.get(configFile.getName()).getFirstValue();
            FileConfiguration config = new StargateYamlConfiguration();
            config.load(configFile);
            for (String settingKey : testMap.keySet()) {
                Object value = testMap.get(settingKey);
                Assertions.assertEquals(value, config.get(settingKey));
            }
        }
    }

    @Test
    @Order(3)
    public void portalPrintCheck() throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM Portal;");
        ResultSet set = statement.executeQuery();
        ResultSetMetaData meta = set.getMetaData();
        int count = 0;
        while (set.next()) {
            count++;
            String msg = "";
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                msg = msg + meta.getColumnLabel(i) + ":" + set.getObject(i) + ",";
            }
            Stargate.log(Level.FINE, msg);
        }
        conn.close();
        Assertions.assertTrue(count > 0, "There was no portals loaded from old database");
    }

    @ParameterizedTest
    @MethodSource("getTestedConfigNames")
    @Order(2)
    public void portalLoadCheck(String key) {
        Map<String, String> testMap = configTestMap.get(key).getSecondValue();
        Stargate.log(Level.FINE, String.format("--------- Checking portal loaded from %s configuration%n", key));
        for (String portalName : testMap.keySet()) {
            String netName = testMap.get(portalName);
            Network net = registry.getNetwork(netName, false);
            Assertions.assertNotNull(net, String.format("Network %s for portal %s was null", netName, portalName));
            Portal portal = net.getPortal(portalName);
            Assertions.assertNotNull(portal, String.format("Portal %s in network %s was null", portalName, netName));
        }
    }
    
    @ParameterizedTest
    @MethodSource("getTestedConfigNames")
    @Order(3)
    public void knarvikNagPropertySet(String key) {
        StoredPropertiesAPI properties = propertiesMap.get(key);
        if (key.contains("config-epicknarvik.yml")) {
            Assertions.assertEquals("true", properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
        } else {
            Assertions.assertNull(properties.getProperty(StoredProperty.PARITY_UPGRADES_AVAILABLE));
        }
    }
    
    private static Stream<String> getTestedConfigNames(){
        return configTestMap.keySet().stream();
    }

}
