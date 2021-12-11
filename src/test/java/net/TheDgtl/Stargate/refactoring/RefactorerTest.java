package net.TheDgtl.Stargate.refactoring;

import be.seeseemelk.mockbukkit.MockBukkit;
import net.TheDgtl.Stargate.FakeStargate;
import net.TheDgtl.Stargate.StargateLogger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefactorerTest {
    static private File[] configFiles;
    static private StargateLogger logger;
    static private File defaultConfigFile;

    @BeforeAll
    public static void setUp() {
        String configFolder = "src/test/resources/configurations";
        configFiles = new File[]{
                new File(configFolder, "config-epicknarvik.yml")
                , new File(configFolder, "config-lclo.yml")
                , new File(configFolder, "config-pseudoknight.yml")
                //,new File(configFolder, "config-dinnerbone.yml")
                //,new File(configFolder, "config-drakia.yml")
        };
        logger = new FakeStargate();
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
    public void loadConfigTest() throws IOException, InvalidConfigurationException {
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if (oldConfigFile.exists()) {
                oldConfigFile.delete();
            }
            Refactorer refactorer = new Refactorer(configFile, logger);
            configFile.renameTo(oldConfigFile);
            Map<String, Object> config = refactorer.run();
            refactorer.insertNewValues(config);
        }
    }

}
