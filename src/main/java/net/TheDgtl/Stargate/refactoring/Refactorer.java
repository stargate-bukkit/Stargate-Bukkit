package net.TheDgtl.Stargate.refactoring;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateConfiguration;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.refactoring.retcons.Modificator;
import net.TheDgtl.Stargate.refactoring.retcons.RetCon1_0_0;

import net.TheDgtl.Stargate.util.FileHelper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import java.util.logging.Level;

public class Refactorer {
    /*
     * This name stays
     * NOT USED CURRENTLY
     */

    private int configVersion;
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
        FileConfiguration fileConfig = new StargateConfiguration();
        fileConfig.load(configFile);
        this.fileConfig = fileConfig;
        this.config = fileConfig.getValues(true);
        this.configVersion = fileConfig.getInt("configVersion");
        this.configFile = configFile;
        this.logger = logger;
    }

    /**
     * 
     * @return every configuration mapping that could be transfered over to this version
     */
    public Map<String, Object> run() {
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
}