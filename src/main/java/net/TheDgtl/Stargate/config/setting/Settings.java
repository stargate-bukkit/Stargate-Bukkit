package net.TheDgtl.Stargate.config.setting;

import net.TheDgtl.Stargate.Stargate;

/**
 * Contains various methods for getting values of current settings
 */
public class Settings {

    /**
     * Gets the integer value of a setting
     *
     * @param setting <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static int getInteger(Setting setting) {
        return Stargate.getConfigStatic().getInt(setting.getConfigNode());
    }

    /**
     * Gets the double value of a setting
     *
     * @param setting <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static double getDouble(Setting setting) {
        return Stargate.getConfigStatic().getDouble(setting.getConfigNode());
    }

    /**
     * Gets the string value of a setting
     *
     * @param setting <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static String getString(Setting setting) {
        return Stargate.getConfigStatic().getString(setting.getConfigNode());
    }

    /**
     * Gets the boolean value of a setting
     *
     * @param setting <p>The setting to get</p>
     * @return <p>The value of the setting</p>
     */
    public static boolean getBoolean(Setting setting) {
        return Stargate.getConfigStatic().getBoolean(setting.getConfigNode());
    }

}
