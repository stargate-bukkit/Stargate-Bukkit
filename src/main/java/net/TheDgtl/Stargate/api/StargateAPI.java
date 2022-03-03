package net.TheDgtl.Stargate.api;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.StorageAPI;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.network.RegistryAPI;
import org.bukkit.configuration.Configuration;

/**
 * An API to facilitate addons and integrations
 *
 * @author Thorin
 */
public class StargateAPI {

    /**
     * Gets the registry used to register and unregister Stargates
     *
     * @return <p>The registry used to register and unregister Stargates</p>
     */
    public static RegistryAPI getRegistry() {
        return Stargate.getRegistry();
    }

    /**
     * Gets the storage API used to store and load Stargates
     *
     * @return <p>The storage API used to store and load stargates</p>
     */
    public static StorageAPI getStorageAPI() {
        return Stargate.getStorageAPI();
    }

    /**
     * Reloads the Stargate configuration from disk
     */
    public static void reload() {
        Stargate.getInstance().reload();
    }

    /**
     * Gets Stargate's configuration
     *
     * @return <p>Stargate's configuration</p>
     */
    public static Configuration getConfig() {
        return Stargate.getFileConfiguration();
    }

    /**
     * Gets the language manager used for translating strings
     *
     * @return <p>The language manager used for stanslating strings</p>
     */
    public static LanguageManager getLanguageManager() {
        return Stargate.languageManager;
    }

}
