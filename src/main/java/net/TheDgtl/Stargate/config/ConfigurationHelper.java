package net.TheDgtl.Stargate.config;

import net.TheDgtl.Stargate.Stargate;

import java.util.List;

/**
 * Contains various methods for getting values of current settings
 */
public final class ConfigurationHelper {

    private ConfigurationHelper() {

    }

    /**
     * Gets the integer value of a setting
     *
     * @param configurationOption <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static int getInteger(ConfigurationOption configurationOption) {
        if (Stargate.getFileConfiguration().isSet(configurationOption.getConfigNode())) {
            return Stargate.getFileConfiguration().getInt(configurationOption.getConfigNode());
        } else {
            return (int) configurationOption.getDefaultValue();
        }
    }

    /**
     * Gets the double value of a setting
     *
     * @param configurationOption <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static double getDouble(ConfigurationOption configurationOption) {
        if (Stargate.getFileConfiguration().isSet(configurationOption.getConfigNode())) {
            return Stargate.getFileConfiguration().getDouble(configurationOption.getConfigNode());
        } else {
            return (double) configurationOption.getDefaultValue();
        }
    }

    /**
     * Gets the string value of a setting
     *
     * @param configurationOption <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static String getString(ConfigurationOption configurationOption) {
        if (Stargate.getFileConfiguration().isSet(configurationOption.getConfigNode())) {
            return Stargate.getFileConfiguration().getString(configurationOption.getConfigNode());
        } else {
            return (String) configurationOption.getDefaultValue();
        }
    }

    /**
     * Gets the boolean value of a setting
     *
     * @param configurationOption <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static boolean getBoolean(ConfigurationOption configurationOption) {
        if (Stargate.getFileConfiguration().isSet(configurationOption.getConfigNode())) {
            return Stargate.getFileConfiguration().getBoolean(configurationOption.getConfigNode());
        } else {
            return (boolean) configurationOption.getDefaultValue();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringList(ConfigurationOption configurationOption) {
        if (Stargate.getFileConfiguration().isSet(configurationOption.getConfigNode())) {
            return Stargate.getFileConfiguration().getStringList(configurationOption.getConfigNode());
        } else {
            if (configurationOption.getDefaultValue() == null) {
                return null;
            }
            return (List<String>) configurationOption.getDefaultValue();
        }
    }

}
