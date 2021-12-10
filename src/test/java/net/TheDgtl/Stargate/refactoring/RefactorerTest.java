package net.TheDgtl.Stargate.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RefactorerTest {
    static private File[] configFiles;
    static private StargateLogger logger;
    static private File defaultConfigFile;
    @BeforeAll
    public static void setUp() throws FileNotFoundException, IOException, InvalidConfigurationException {
        String configFolder = "src/test/resources/configurations";
        configFiles = new File[] {
                new File(configFolder, "config-epicknarvik.yml")
                ,new File(configFolder, "config-lclo.yml")
                ,new File(configFolder, "config-pseudoknight.yml")
                //,new File(configFolder, "config-dinnerbone.yml")
                //,new File(configFolder, "config-drakia.yml")
                };
        logger = new FakeStargate();
        defaultConfigFile = new File("src/main/resources","config.yml");
    }
    
    @AfterAll
    public static void tearDown() {
        for(File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if(oldConfigFile.exists())
                configFile.delete();
            oldConfigFile.renameTo(configFile);
        }
    }
    
    @Test
    @Order(0)
    public void loadConfigTest() throws FileNotFoundException, IOException, InvalidConfigurationException {
        for(File configFile : configFiles) {
            File oldConfigFile = new File(configFile.getAbsolutePath() + ".old");
            if(oldConfigFile.exists())
                oldConfigFile.delete();
            Refactorer middas = new Refactorer(configFile,logger);
            configFile.renameTo(oldConfigFile);
            Map<String,Object> config = middas.run();
            middas.insertNewValues(config);
            
        }
    }
    
}
