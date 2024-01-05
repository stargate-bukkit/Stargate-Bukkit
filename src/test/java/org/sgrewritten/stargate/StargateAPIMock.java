package org.sgrewritten.stargate;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.BlockHandlerResolver;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationAPI;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.manager.BungeeManager;
import org.sgrewritten.stargate.api.network.NetworkManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.permission.PermissionManager;
import org.sgrewritten.stargate.config.ConfigurationAPIMock;
import org.sgrewritten.stargate.database.StorageMock;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.economy.StargateEconomyManagerMock;
import org.sgrewritten.stargate.manager.StargateBungeeManager;
import org.sgrewritten.stargate.network.StargateNetworkManager;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.util.LanguageManagerMock;

public class StargateAPIMock implements StargateAPI {

    private NetworkManager networkManager;
    private RegistryAPI registry;
    private ConfigurationAPI configurationAPI;
    private LanguageManager languageManager;
    private BungeeManager bungeeManager;
    private StorageAPI storageAPI;
    private BlockHandlerResolver blockHandlerResolver;
    private StargateEconomyAPI economyManager;

    /**
     * @param managers <p>A set of managers as listed in this class</p>
     */
    public StargateAPIMock(Object... managers) {
        for (Object aManager : managers) {
            if (aManager instanceof StorageAPI storageAPI) {
                this.storageAPI = storageAPI;
            } else if (aManager instanceof RegistryAPI registryAPI) {
                this.registry = registryAPI;
            } else if (aManager instanceof ConfigurationAPI configurationAPI) {
                this.configurationAPI = configurationAPI;
            } else if (aManager instanceof LanguageManager languageManager) {
                this.languageManager = languageManager;
            } else if (aManager instanceof BungeeManager bungeeManager) {
                this.bungeeManager = bungeeManager;
            } else if (aManager instanceof BlockHandlerResolver blockHandlerResolver) {
                this.blockHandlerResolver = blockHandlerResolver;
            } else if (aManager instanceof StargateEconomyAPI economyManager) {
                this.economyManager = economyManager;
            } else if (aManager instanceof NetworkManager networkManager) {
                this.networkManager = networkManager;
            }
        }
        if (storageAPI == null) {
            this.storageAPI = new StorageMock();
        }
        if (this.blockHandlerResolver == null) {
            this.blockHandlerResolver = new BlockHandlerResolver(storageAPI);
        }
        if (registry == null) {
            this.registry = new StargateRegistry(this.storageAPI, this.blockHandlerResolver);
        }
        if (this.configurationAPI == null) {
            this.configurationAPI = new ConfigurationAPIMock();
        }
        if (this.languageManager == null) {
            this.languageManager = new LanguageManagerMock();
        }
        if (this.networkManager == null) {
            this.networkManager = new StargateNetworkManager(registry, storageAPI);
        }
        if (this.bungeeManager == null) {
            this.bungeeManager = new StargateBungeeManager(registry, languageManager, networkManager);
        }
        if (this.economyManager == null) {
            this.economyManager = new StargateEconomyManagerMock();
        }
    }

    @Override
    public RegistryAPI getRegistry() {
        return registry;
    }

    @Override
    public StorageAPI getStorageAPI() {
        return storageAPI;
    }

    @Override
    public PermissionManager getPermissionManager(Entity entity) {
        return null;
    }

    @Override
    public ConfigurationAPI getConfigurationAPI() {
        return configurationAPI;
    }

    @Override
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    @Override
    public BungeeManager getBungeeManager() {
        return bungeeManager;
    }

    @Override
    public StargateEconomyAPI getEconomyManager() {
        return economyManager;
    }

    @Override
    public BlockHandlerResolver getMaterialHandlerResolver() {
        return blockHandlerResolver;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }
}
