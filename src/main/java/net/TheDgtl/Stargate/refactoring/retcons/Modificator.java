package net.TheDgtl.Stargate.refactoring.retcons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Modificator {
    /**
     * @param oldSetting a string where index 0 is the key, and index 1 is the value
     * @return new setting where index 0 is the key, and index 1 is the value
     */
    protected SettingSet getNewSetting(SettingSet oldSetting) {
        return oldSetting;
    }

    /**
     * @param oldConfig
     * @return a new configuration
     */
    public Map<String, Object> getConfigModifications(Map<String, Object> oldConfig) {
        return configScroller(oldConfig);
    }
    
    public abstract void run();

    public abstract int getConfigNumber();

    private Map<String, Object> configScroller(Map<String, Object> config) {
        Map<String, Object> replacementConfig = new HashMap<>();
        for (String key : config.keySet()) {
            Object value = config.get(key);
            if(value instanceof ConfigurationSection)
                continue;
            SettingSet oldSetting = new SettingSet(key, value);

            SettingSet newSetting = getNewSetting(oldSetting);
            if (newSetting == null) {
                continue;
            }
            replacementConfig.put(newSetting.key, newSetting.value);
        }
        return replacementConfig;
    }
}