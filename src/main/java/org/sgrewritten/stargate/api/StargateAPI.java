package org.sgrewritten.stargate.api;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.config.ConfigurationAPI;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.manager.BungeeManager;
import org.sgrewritten.stargate.api.manager.PermissionManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;

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
    StorageAPI getStorageAPI();

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

    /**
     * Gets the bungee manager used for dealing with bungee related events
     *
     * @return <p> The bungee manager used for dealing with bungee related events </p>
     */
    BungeeManager getBungeeManager();




}
