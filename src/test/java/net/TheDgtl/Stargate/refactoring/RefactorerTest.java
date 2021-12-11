package net.TheDgtl.Stargate.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
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
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.network.StargateFactory;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefactorerTest {
    static private File[] configFiles;
    static private StargateLogger logger;
    static private File defaultConfigFile;

    static private StargateFactory factory;
    @BeforeAll
    public static void setUp() throws FileNotFoundException, IOException, InvalidConfigurationException, SQLException {
        String configFolder = "src/test/resources/configurations";
        configFiles = new File[]{
                new File(configFolder, "config-epicknarvik.yml")
                , new File(configFolder, "config-lclo.yml")
                , new File(configFolder, "config-pseudoknight.yml")
                //,new File(configFolder, "config-dinnerbone.yml")
                //,new File(configFolder, "config-drakia.yml")
        };
        logger = new FakeStargate();
        defaultConfigFile = new File("src/main/resources","config.yml");
        File databaseFile = new File("src/test/resources", "test.db");
        Database sqlDatabase = new SQLiteDatabase(databaseFile);
        factory = new StargateFactory(sqlDatabase,false,false,logger);
        
        defaultConfigFile = new File("src/main/resources", "config.yml");
        MockBukkit.mock();
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldConfigFile.exists()) {
                configFile.delete();
            }
            oldConfigFile.renameTo(configFile);
        }
    }

    @Test
    @Order(0)
    public void loadConfigTest() throws FileNotFoundException, IOException, InvalidConfigurationException {
        ServerMock server = MockBukkit.mock();
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldConfigFile.exists()) {
                oldConfigFile.delete();
                Refactorer middas = new Refactorer(configFile, logger, server, factory);
                configFile.renameTo(oldConfigFile);
                Map<String, Object> config = middas.run();
                Files.copy(defaultConfigFile, configFile);
                middas.insertNewValues(config);
            }
        }
    }
}
