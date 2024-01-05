package org.sgrewritten.stargate.config;

import org.sgrewritten.stargate.api.config.ConfigurationAPI;
import org.sgrewritten.stargate.api.config.ConfigurationOption;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationAPIMock implements ConfigurationAPI {
    private Map<ConfigurationOption, Object> configuration = loadDefaultConfiguration();

    private static Map<ConfigurationOption, Object> loadDefaultConfiguration() {
        Map<ConfigurationOption, Object> config = new HashMap<>();
        for (ConfigurationOption configurationOption : ConfigurationOption.values()) {
            config.put(configurationOption, configurationOption.getDefaultValue());
        }
        return config;
    }

    @Override
    public void setConfigurationOptionValue(ConfigurationOption configurationOption, Object newValue) {
        configuration.put(configurationOption, newValue);
    }

    @Override
    public Object getConfigurationOptionValue(ConfigurationOption configurationOption) {
        return configuration.get(configurationOption);
    }

    @Override
    public void saveConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException();
    }
}
