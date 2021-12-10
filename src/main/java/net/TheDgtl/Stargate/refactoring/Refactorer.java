package net.TheDgtl.Stargate.refactoring;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.refactoring.retcons.Modificator;
import net.TheDgtl.Stargate.refactoring.retcons.RetCon1_0_0;
import net.TheDgtl.Stargate.util.FileHelper;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Refactorer {
    /*
     * This name stays
     * NOT USED CURRENTLY
     */

    private int configVersion;
    private FileConfiguration defaultConfig;
    private File configFile;
    private Map<String, Object> config;
    private StargateLogger logger;
    private FileConfiguration fileConfig;
    private static final Modificator[] RETCONS;

    static {
        RETCONS = new Modificator[]{
                new RetCon1_0_0()
        };
    }

    public Refactorer(File configFile, StargateLogger logger) throws FileNotFoundException, IOException, InvalidConfigurationException {
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.load(configFile);
        this.fileConfig = fileConfig;
        this.config = fileConfig.getValues(true);
        this.configVersion = fileConfig.getInt("configVersion");
        this.configFile = configFile;
        this.logger = logger;
    }

    public Map<String, Object> calculateNewConfig() {
        for (Modificator retCon : RETCONS) {
            int retConConfigNumber = retCon.getConfigNumber();
            if (retConConfigNumber >= configVersion) {
                config = retCon.run(config);
                configVersion = retConConfigNumber;
            }
        }
        return config;
    }

    public void insertNewValues(Map<String, Object> config) throws IOException, InvalidConfigurationException {
        fileConfig.load(configFile);
        for (String settingKey : config.keySet()) {
            fileConfig.set(settingKey, config.get(settingKey));
        }
        fileConfig.set("configVersion", Stargate.CURRENT_CONFIG_VERSION);
        fileConfig.save(configFile);
    }
    

    /**
     * Used in debug, when you want to see the state of the currently stored
     * configuration
     *
     * @throws IOException
     */
    public void dispConfig() throws IOException {
        BufferedReader bReader;
        bReader = FileHelper.getBufferedReader(configFile);

        try {
            logger.logMessage(Level.FINEST, String.format("Current config from file %s:",configFile.getName()));
            String line;
            while ((line = bReader.readLine())!= null) {
                logger.logMessage(Level.FINEST, line);
            }
        } finally {
            bReader.close();
        }
    }
}