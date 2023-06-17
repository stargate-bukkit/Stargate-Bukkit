package org.sgrewritten.stargate.api.config;

/**
 * An API for interacting with Stargate's configuration file
 *
 * @author Kristian Knarvik (EpicKnarvik97)
 */
@SuppressWarnings("unused")
public interface ConfigurationAPI {

    /**
     * Sets the value of a configuration option
     *
     * @param configurationOption <p>The configuration option to update</p>
     * @param newValue            <p>The new value of the configuration option</p>
     */
    void setConfigurationOptionValue(ConfigurationOption configurationOption, Object newValue);

    /**
     * Gets the current value of a configuration option
     *
     * @param configurationOption <p>The configuration option to get the value of</p>
     * @return <p>The current value of the specified configuration option</p>
     */
    Object getConfigurationOptionValue(ConfigurationOption configurationOption);

    /**
     * Saves the current state of the configuration to disk
     *
     * <p>This needs to be run after any config changes to update the config file.</p>
     */
    void saveConfiguration();

    /**
     * Reloads the configuration from disk
     *
     * <p>This needs to be executed in order for saved config changes to take effect.</p>
     */
    void reload();

}
