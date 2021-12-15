package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.config.StargateConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class StargateConfigurationTest {

    static private File[] configFiles;
    static private StargateLogger logger;

    @BeforeAll
    public static void setUp() {
        String configFolder = "src/test/resources/configurations";
        configFiles = new File[]{
                new File(configFolder, "testConfig.yml")
                //,new File("src/main/resources","config.yml")
        };
        logger = new FakeStargate();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (!configFile.delete()) {
                throw new IOException("Unable to delete test config file");
            }
            if (!oldConfigFile.renameTo(configFile)) {
                throw new IOException("Unable to rename backup config file");
            }
        }
    }

    @Test
    public void configurationLoadReplaceEditAndSaveTest() throws IOException, InvalidConfigurationException {
        for (File configFile : configFiles) {
            FileConfiguration config = new StargateConfiguration();
            config.load(configFile);
            logger.logMessage(Level.FINER, " Current config:\n " + config.saveToString());
            File oldFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldFile.exists() && !oldFile.delete()) {
                throw new IOException("Unable to delete config file backup");
            }
            if (!configFile.renameTo(oldFile)) {
                throw new IOException("Unable to rename config file");
            }
            config.set("option", "Another value");
            config.save(configFile);
            logger.logMessage(Level.FINEST, " Current config:\n " + config.saveToString());
        }
    }

}
