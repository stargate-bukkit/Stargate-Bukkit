package net.TheDgtl.Stargate.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.google.common.io.Files;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.TheDgtl.Stargate.FakeStargate;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.StargateConfiguration;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.StargateFactory;
import net.TheDgtl.Stargate.network.portal.Portal;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefactorerTest {
    private static File sqlDatabaseFile;
    static private File[] configFiles;
    static private StargateLogger logger;
    static private File defaultConfigFile;
    static private Database sqlDatabase;
    static private HashMap<String,RefactoringChecker> configTestMap;

    static private StargateFactory factory;
    static private ServerMock server;
    @BeforeAll
    public static void setUp() throws FileNotFoundException, IOException, InvalidConfigurationException, SQLException {
        String configFolder = "src/test/resources/configurations";
        configTestMap = getSettingTestMaps();
        configFiles = new File[configTestMap.size()];
        int i = 0;
        for(String key : configTestMap.keySet()) {
            configFiles[i++] = new File(configFolder, key);
        }
        
        logger = new FakeStargate();
        defaultConfigFile = new File("src/main/resources","config.yml");
        sqlDatabaseFile = new File("src/test/resources", "test.db");
        sqlDatabase = new SQLiteDatabase(sqlDatabaseFile);
        factory = new StargateFactory(sqlDatabase,false,false,logger);
        
        defaultConfigFile = new File("src/main/resources", "config.yml");
        server = MockBukkit.mock();
        server.addSimpleWorld("epicknarvik");
        server.addSimpleWorld("lclo");
        server.addSimpleWorld("pseudoknigth");
        Stargate.getConfigStatic().load(defaultConfigFile);
    }
    
    private static HashMap<String,RefactoringChecker> getSettingTestMaps(){
        HashMap<String,RefactoringChecker> output = new HashMap<>();
        
        RefactoringChecker knarvikChecker = new RefactoringChecker();
        HashMap<String,Object> knarvikConfigChecker = new HashMap<>();
        knarvikConfigChecker.put("defaultGateNetwork", "knarvik");
        knarvikConfigChecker.put("handleVehicles", false);
        knarvikChecker.settingCheckers = knarvikConfigChecker;
        HashMap<String,String> knarvikPortalChecker = new HashMap<>();
        knarvikPortalChecker.put("knarvik1", "knarvik");
        knarvikPortalChecker.put("knarvik2", "knarvik");
        knarvikPortalChecker.put("knarvik3", "knarvik");
        knarvikChecker.portalChecker = knarvikPortalChecker;
        output.put("config-epicknarvik.yml", knarvikChecker);
        
        RefactoringChecker pseudoChecker = new RefactoringChecker();
        HashMap<String,Object> pseudoConfigChecker = new HashMap<>();
        pseudoConfigChecker.put("defaultGateNetwork", "pseudoknight");
        pseudoConfigChecker.put("destroyOnExplosion", true);
        pseudoChecker.settingCheckers = pseudoConfigChecker;
        HashMap<String,String> pseudoPortalChecker = new HashMap<>();
        pseudoPortalChecker.put("pseudo1", "pseudo");
        pseudoPortalChecker.put("pseudo2", "pseudo");
        pseudoChecker.portalChecker = pseudoPortalChecker;
        output.put("config-pseudoknight.yml", pseudoChecker);
        
        RefactoringChecker lcloChecker = new RefactoringChecker();
        HashMap<String,Object> lcloConfigChecker = new HashMap<>();
        lcloConfigChecker.put("defaultGateNetwork", "lclco");
        lcloChecker.settingCheckers = lcloConfigChecker;
        HashMap<String,String> lcloPortalChecker = new HashMap<>();
        lcloPortalChecker.put("lclo1", "lclo");
        lcloPortalChecker.put("lclo2", "lclo");
        pseudoChecker.portalChecker = lcloPortalChecker;
        output.put("config-lclo.yml", lcloChecker);
        
        return output;
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        MockBukkit.unmock();
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldConfigFile.exists()) {
                configFile.delete();
            }
            oldConfigFile.renameTo(configFile);
        }
        //sqlDatabaseFile.delete();
    }

    @Test
    @Order(1)
    public void loadConfigTest() throws FileNotFoundException, IOException, InvalidConfigurationException {
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldConfigFile.exists())
                oldConfigFile.delete();
            Refactorer middas = new Refactorer(configFile, logger, server, factory);
            configFile.renameTo(oldConfigFile);
            
            Map<String, Object> config = middas.run();
            Files.copy(defaultConfigFile, configFile);
            FileConfiguration fileConfig = new StargateConfiguration();
            fileConfig.load(configFile);
            for (String key : config.keySet()) {
                Assert.assertTrue(
                        String.format("The key %s was added to the new config of %s", key, configFile.getName()),
                        fileConfig.getKeys(true).contains(key) || key.contains(StargateConfiguration.START_OF_COMMENT));
            }

            middas.insertNewValues(config);
            fileConfig.load(configFile);
        }
    }
    
    @Test
    @Order(2)
    public void configDoubleCheck() throws FileNotFoundException, IOException, InvalidConfigurationException {
        for(File configFile : configFiles) {
            HashMap<String, Object> testMap = configTestMap.get(configFile.getName()).settingCheckers;
            FileConfiguration config = new StargateConfiguration();
            config.load(configFile);
            for(String settingKey : testMap.keySet()) {
                Object value = testMap.get(settingKey);
                Assert.assertEquals(value, config.get(settingKey));
            }
        }
    }
    
    @Test
    @Order(2)
    public void portalPrintCheck() throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM Portal;");
        ResultSet set = statement.executeQuery();
        ResultSetMetaData meta = set.getMetaData();
        int count = 0;
        while(set.next()) {
            count++;
            for(int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.print(meta.getColumnLabel(i) + ":" + set.getObject(i) + ",");
            }
            System.out.println();
        }
        Assert.assertTrue("There was no portals loaded from old database",count > 0);
    }
    
    @Test
    @Order(2)
    public void portalLoadCheck() throws FileNotFoundException, IOException, InvalidConfigurationException {
        for(String key : configTestMap.keySet()) {
            HashMap<String, String> testMap = configTestMap.get(key).portalChecker;
            for(String portalName : testMap.keySet()) {
                String netName = testMap.get(portalName);
                Network net = factory.getNetwork(netName, false);
                Assert.assertNotNull(String.format("Network %s for portal %s was null", netName,portalName),net);
                Portal portal = net.getPortal(portalName);
                Assert.assertNotNull(String.format("Portal %s in network %s was null",portalName,netName),portal);
            }
            
        }
    }
}
