package net.TheDgtl.Stargate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class StargateConfigurationTest {

    static private File[] configFiles;
    static private StargateLogger logger;
    @BeforeAll
    public static void setUp() throws FileNotFoundException, IOException, InvalidConfigurationException {
        String configFolder = "src/test/resources/configurations";
        configFiles = new File[] {
                new File(configFolder, "testConfig.yml")
                //,new File("src/main/resources","config.yml")
                };
        logger = new FakeStargate();
    }
    
    @AfterAll
    public static void tearDown() {
        for(File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            configFile.delete();
            oldConfigFile.renameTo(configFile);
        }
    }
    
    
    @Test
    public void theTest() throws FileNotFoundException, IOException, InvalidConfigurationException {
        for(File configFile : configFiles) {
            FileConfiguration config = new StargateConfiguration();
            config.load(configFile);
            logger.logMessage(Level.FINEST, " Current config:\n " + config.saveToString());
            File oldFile = new File(configFile.getAbsolutePath() + ".old");
            if(oldFile.exists())
                oldFile.delete();
            configFile.renameTo(oldFile);
            config.set("option", "Another value");
            config.save(configFile);
            logger.logMessage(Level.FINEST, " Current config:\n " + config.saveToString());
        }
    }
}
