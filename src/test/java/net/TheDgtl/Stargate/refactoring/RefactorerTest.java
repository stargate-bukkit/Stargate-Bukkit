package net.TheDgtl.Stargate.refactoring;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.google.common.io.Files;

import net.TheDgtl.Stargate.FakeLanguageManager;
import net.TheDgtl.Stargate.FakeStargate;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.TwoTuple;
import net.TheDgtl.Stargate.config.StargateConfiguration;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.PortalDatabaseAPI;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.database.StorageAPI;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.NetworkAPI;
import net.TheDgtl.Stargate.network.StargateRegistry;
import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefactorerTest {

    private static File sqlDatabaseFile;
    static private File[] configFiles;
    static private StargateLogger logger;
    static private File defaultConfigFile;
    static private Database sqlDatabase;
    static private final Map<String, Refactorer> refactorerMap = new HashMap<>();
    static private Map<String, TwoTuple<Map<String, Object>, Map<String, String>>> configTestMap;
    private static final File testGatesDir = new File("src/test/resources/gates");

    static private ServerMock server;
    private static StargateRegistry registry;

    @BeforeAll
    public static void setUp() throws IOException, InvalidConfigurationException, SQLException {
        String configFolder = "src/test/resources/configurations";
        configTestMap = getSettingTestMaps();
        configFiles = new File[configTestMap.size()];
        int i = 0;
        for (String key : configTestMap.keySet()) {
            configFiles[i++] = new File(configFolder, key);
        }

        logger = new FakeStargate();
        Stargate.languageManager = new FakeLanguageManager();
        defaultConfigFile = new File("src/main/resources", "config.yml");
        sqlDatabaseFile = new File("src/test/resources", "test.db");
        sqlDatabase = new SQLiteDatabase(sqlDatabaseFile);
        StorageAPI storageAPI = new PortalDatabaseAPI(sqlDatabase, false, false, logger);
        registry = new StargateRegistry(storageAPI);


        defaultConfigFile = new File("src/main/resources", "config.yml");
        server = MockBukkit.mock();
        server.addSimpleWorld("epicknarvik");
        server.addSimpleWorld("lclo");
        server.addSimpleWorld("pseudoknigth");
        Stargate.getConfigStatic().load(defaultConfigFile);

        GateFormat.setFormats(Objects.requireNonNull(GateFormat.loadGateFormats(testGatesDir)));
    }

    private static Map<String, TwoTuple<Map<String, Object>, Map<String, String>>> getSettingTestMaps() {
        Map<String, TwoTuple<Map<String, Object>, Map<String, String>>> output = new HashMap<>();

        Map<String, Object> knarvikConfigChecks = new HashMap<>();
        knarvikConfigChecks.put("defaultGateNetwork", "knarvik");
        knarvikConfigChecks.put("handleVehicles", false);
        Map<String, String> knarvikPortalChecks = new HashMap<>();
        knarvikPortalChecks.put("knarvik1", "knarvik");
        knarvikPortalChecks.put("knarvik2", "knarvik");
        knarvikPortalChecks.put("knarvik3", "knarvik");
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
        lcloPortalChecks.put("lclo1", "lclo");
        lcloPortalChecks.put("lclo2", "lclo");
        TwoTuple<Map<String, Object>, Map<String, String>> lcloChecks = new TwoTuple<>(lcloConfigChecks,
                lcloPortalChecks);
        output.put("config-lclo.yml", lcloChecks);

        return output;
    }

    @AfterAll
    public static void tearDown() throws IOException, SQLException {
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
        }
        if (sqlDatabaseFile.exists() && !sqlDatabaseFile.delete()) {
            throw new IOException("Unable to remove database file");
        }

    }

    @Test
    @Order(1)
    public void convertConfigCheck() throws IOException, InvalidConfigurationException {
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldConfigFile.exists() && !oldConfigFile.delete()) {
                throw new IOException("Unable to delete old config file");
            }
            Refactorer refactorer = new Refactorer(configFile, logger, server, registry);
            if (!configFile.renameTo(oldConfigFile)) {
                throw new IOException("Unable to rename existing config for backup");
            }

            Map<String, Object> config = refactorer.getConfigModifications();
            Files.copy(defaultConfigFile, configFile);
            FileConfiguration fileConfig = new StargateConfiguration();
            fileConfig.load(configFile);
            for (String key : config.keySet()) {
                Assertions.assertTrue(
                        fileConfig.getKeys(true).contains(key) || key.contains(StargateConfiguration.START_OF_COMMENT), String.format("The key %s was added to the new config of %s", key, configFile.getName()));
            }

            refactorer.insertNewValues(config);
            refactorerMap.put(configFile.getName(), refactorer);
            fileConfig.load(configFile);
        }
    }

    @Test
    @Order(2)
    public void doOtherRefactorCheck() {
        for (String key : refactorerMap.keySet()) {
            System.out.printf("####### Performing misc. refactoring based on the config-file %s%n", key);
            Refactorer refactorer = refactorerMap.get(key);
            refactorer.run();
        }
    }

    @Test
    @Order(3)
    public void configDoubleCheck() throws IOException, InvalidConfigurationException {
        for (File configFile : configFiles) {
            Map<String, Object> testMap = configTestMap.get(configFile.getName()).getFirstValue();
            FileConfiguration config = new StargateConfiguration();
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
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.print(meta.getColumnLabel(i) + ":" + set.getObject(i) + ",");
            }
            System.out.println();
        }
        conn.close();
        Assertions.assertTrue(count > 0, "There was no portals loaded from old database");
    }

    @Test
    @Order(3)
    public void portalLoadCheck() {
        for (String key : configTestMap.keySet()) {
            Map<String, String> testMap = configTestMap.get(key).getSecondValue();

            System.out.printf("--------- Checking portal loaded from %s configuration%n", key);
            for (String portalName : testMap.keySet()) {
                String netName = testMap.get(portalName);
                NetworkAPI net = registry.getNetwork(netName, false);
                Assertions.assertNotNull(net, String.format("Network %s for portal %s was null", netName, portalName));
                Portal portal = net.getPortal(portalName);
                Assertions.assertNotNull(portal, String.format("Portal %s in network %s was null", portalName, netName));
            }

        }
    }

}
