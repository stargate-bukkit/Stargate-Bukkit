package net.TheDgtl.Stargate.config;

import net.TheDgtl.Stargate.Stargate;

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
        return Stargate.getFileConfiguration().getInt(configurationOption.getConfigNode());
    }

    /**
     * Gets the double value of a setting
     *
     * @param configurationOption <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static double getDouble(ConfigurationOption configurationOption) {
        return Stargate.getFileConfiguration().getDouble(configurationOption.getConfigNode());
    }

    /**
     * Gets the string value of a setting
     *
     * @param configurationOption <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static String getString(ConfigurationOption configurationOption) {
        return Stargate.getFileConfiguration().getString(configurationOption.getConfigNode());
    }

    /**
     * Gets the boolean value of a setting
     *
     * @param configurationOption <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static boolean getBoolean(ConfigurationOption configurationOption) {
        return Stargate.getFileConfiguration().getBoolean(configurationOption.getConfigNode());
    }

}
