package net.TheDgtl.Stargate.refactoring.retcons;

import net.TheDgtl.Stargate.TwoTuple;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public abstract class Modifier {

    /**
     * @param oldSetting a string where index 0 is the key, and index 1 is the value
     * @return new setting where index 0 is the key, and index 1 is the value
     */
    protected TwoTuple<String, Object> getNewSetting(TwoTuple<String, Object> oldSetting) {
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

    public abstract int getConfigVersion();

    private Map<String, Object> configScroller(Map<String, Object> config) {
        Map<String, Object> replacementConfig = new HashMap<>();
        for (String key : config.keySet()) {
            Object value = config.get(key);
            if (value instanceof ConfigurationSection) {
                continue;
            }
            TwoTuple<String, Object> oldSetting = new TwoTuple<>(key, value);

            TwoTuple<String, Object> newSetting = getNewSetting(oldSetting);
            if (newSetting == null) {
                continue;
            }
            replacementConfig.put(newSetting.getFirstValue(), newSetting.getSecondValue());
        }
        return replacementConfig;
    }

}