package net.TheDgtl.Stargate.refactoring;

import net.TheDgtl.Stargate.FakeStargate;
import net.TheDgtl.Stargate.StargateLogger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RefactorerTest {

    static private List<Refactorer> refactorers = new ArrayList<>();
    static private File[] configFiles;

    @BeforeAll
    public static void setUp() throws FileNotFoundException, IOException, InvalidConfigurationException {
        StargateLogger logger = new FakeStargate();
        String configFolder = "src/test/resources/configurations";
        configFiles = new File[]{
                new File(configFolder, "testConfig.yml"),
                new File("src/main/resources", "config.yml")
        };
        for (File configFile : configFiles) {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            refactorers.add(new Refactorer(configFile, logger));
        }
    }

    @AfterAll
    public static void tearDown() {
        for (File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            configFile.delete();
            oldConfigFile.renameTo(configFile);
        }
    }

    @Test
    @Order(0)
    public void loadConfigTest() {

    }

    @Test
    @Order(2)
    public void convertCommentsTest() throws IOException {
        for (Refactorer refactorer : refactorers) {
            refactorer.convertCommentsToYAMLMappings();
            refactorer.dispConfig();
        }
    }

    @Test
    @Order(4)
    public void reAddCommentsTest() throws IOException, InvalidConfigurationException {
        for (Refactorer refactorer : refactorers) {
            refactorer.convertYAMLMappingsToComments();
            refactorer.dispConfig();
        }
    }

}
