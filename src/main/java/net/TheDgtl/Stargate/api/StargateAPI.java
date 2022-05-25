package net.TheDgtl.Stargate.api;

import net.TheDgtl.Stargate.database.StorageAPI;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.network.RegistryAPI;
import org.bukkit.configuration.Configuration;

/**
 * An API to facilitate addons and integrations
 *
 * @author Thorin
 */
public interface StargateAPI {

    /**
     * Gets the registry used to register and unregister Stargates
     *
     * @return <p>The registry used to register and unregister Stargates</p>
     */
    public RegistryAPI getRegistry();

    /**
     * Gets the storage API used to store and load Stargates
     *
     * @return <p>The storage API used to store and load stargates</p>
     */
    public StorageAPI getStorageAPI();

    /**
     * Reloads the Stargate configuration from disk
     */
    public void reload();

    /**
     * Gets Stargate's configuration
     *
     * @return <p>Stargate's configuration</p>
     */
    public Configuration getConfig();

    /**
     * Gets the language manager used for translating strings
     *
     * @return <p>The language manager used for stanslating strings</p>
     */
    public LanguageManager getLanguageManager();

}
