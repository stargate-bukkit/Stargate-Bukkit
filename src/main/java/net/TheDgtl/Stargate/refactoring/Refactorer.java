package net.TheDgtl.Stargate.refactoring;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.config.StargateConfiguration;
import net.TheDgtl.Stargate.network.StargateRegistry;
import net.TheDgtl.Stargate.refactoring.retcons.Modifier;
import net.TheDgtl.Stargate.refactoring.retcons.RetCon1_0_0;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Refactorer {

    private int configVersion;
    private final File configFile;
    private Map<String, Object> config;
    private final FileConfiguration fileConfig;
    private final Modifier[] RETCONS;

    public Refactorer(File configFile, StargateLogger logger, Server server, StargateRegistry registry) throws IOException, InvalidConfigurationException {
        RETCONS = new Modifier[]{
                new RetCon1_0_0(server, registry, logger)
        };

        /*
         * Not StargateConfiguration, as we don't want to save comments
         */
        FileConfiguration fileConfig = new YamlConfiguration();
        fileConfig.load(configFile);
        this.fileConfig = fileConfig;
        this.config = fileConfig.getValues(true);
        this.configVersion = fileConfig.getInt("configVersion");
        this.configFile = configFile;
    }

    /**
     * @return every configuration mapping that could be transferred over to this version
     */
    public Map<String, Object> getConfigModifications() {
        for (Modifier retCon : RETCONS) {
            int retConConfigNumber = retCon.getConfigVersion();
            if (retConConfigNumber >= configVersion) {
                config = retCon.getConfigModifications(config);
            }
        }
        return config;
    }

    public void run() {
        for (Modifier retCon : RETCONS) {
            int retConConfigNumber = retCon.getConfigVersion();
            if (retConConfigNumber >= configVersion) {
                retCon.run();
                configVersion = retConConfigNumber;
            }
        }
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