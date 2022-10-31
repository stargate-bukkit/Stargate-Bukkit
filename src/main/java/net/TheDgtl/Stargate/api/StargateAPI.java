package net.TheDgtl.Stargate.api;

import net.TheDgtl.Stargate.config.ConfigurationAPI;
import net.TheDgtl.Stargate.database.PortalStorageAPI;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.manager.PermissionManager;
import net.TheDgtl.Stargate.network.RegistryAPI;
import org.bukkit.entity.Entity;

/**
 * An API to facilitate addons and integrations
 *
 * @author Thorin
 */
@SuppressWarnings("unused")
public interface StargateAPI {

    /**
     * Gets the registry used to register and unregister Stargates
     *
     * @return <p>The registry used to register and unregister Stargates</p>
     */
    RegistryAPI getRegistry();

    /**
     * Gets the storage API used to store and load Stargates
     *
     * @return <p>The storage API used to store and load stargates</p>
     */
    PortalStorageAPI getStorageAPI();

    /**
     * Gets permission manager for specified entity
     *
     * @param entity <p> The entity to check permissions on </p>
     * @return <p> A permission manager </p>
     */
    PermissionManager getPermissionManager(Entity entity);

    /**
     * Gets the configuration API used to interact with the configuration file
     *
     * @return <p>The configuration API</p>
     */
    ConfigurationAPI getConfigurationAPI();

    /**
     * Gets the language manager used for translating strings
     *
     * @return <p>The language manager used for translating strings</p>
     */
    LanguageManager getLanguageManager();

}
