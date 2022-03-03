package net.TheDgtl.Stargate.migration;

import net.TheDgtl.Stargate.container.TwoTuple;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * A specification for a data migration
 *
 * <p>A data migration migrates data from the old format to the new one</p>
 */
public abstract class DataMigration {

    /**
     * Gets an updated configuration given the old configuration
     *
     * @param oldConfig <p>The old configuration to update</p>
     * @return <p>The update configuration</p>
     */
    public Map<String, Object> getUpdatedConfigValues(Map<String, Object> oldConfig) {
        Map<String, Object> updatedConfig = new HashMap<>();
        for (String key : oldConfig.keySet()) {
            Object value = oldConfig.get(key);
            if (value instanceof ConfigurationSection) {
                continue;
            }
            TwoTuple<String, Object> oldSetting = new TwoTuple<>(key, value);

            TwoTuple<String, Object> newSetting = getNewConfigPair(oldSetting);
            if (newSetting == null) {
                continue;
            }
            updatedConfig.put(newSetting.getFirstValue(), newSetting.getSecondValue());
        }
        return updatedConfig;
    }

    /**
     * Runs this configuration modifier
     *
     * <p>This may alter files such as portal and gate files to perform the appropriate changes necessary for
     * compatibility. This does not modify the config file itself.</p>
     */
    public abstract void run();

    /**
     * Gets the configuration version this modifier will update the configuration to
     *
     * @return <p>The configuration version of this modifier</p>
     */
    public abstract int getConfigVersion();

    /**
     * Gets the new key-value pair corresponding to the old key-value pair
     *
     * <p>This basically gets the updated config key and updates the value if necessary</p>
     *
     * @param oldPair <p>The old key-value pair</p>
     * @return <p>The new and updated key-value pair</p>
     */
    protected abstract TwoTuple<String, Object> getNewConfigPair(TwoTuple<String, Object> oldPair);

}