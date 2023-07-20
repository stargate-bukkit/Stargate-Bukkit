package org.sgrewritten.stargate;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.MaterialHandlerResolver;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.config.ConfigurationAPI;
import org.sgrewritten.stargate.api.database.StorageAPI;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.manager.BungeeManager;
import org.sgrewritten.stargate.api.manager.PermissionManager;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.config.ConfigurationAPIMock;
import org.sgrewritten.stargate.economy.FakeEconomyManager;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.manager.StargateBungeeManager;
import org.sgrewritten.stargate.network.StargateRegistry;
import org.sgrewritten.stargate.util.LanguageManagerMock;
import org.sgrewritten.stargate.util.StorageMock;

public class StargateAPIMock implements StargateAPI {

    private RegistryAPI registry;
    private ConfigurationAPI configurationAPI;
    private LanguageManager languageManager;
    private BungeeManager bungeeManager;
    private StorageAPI storageAPI;
    private MaterialHandlerResolver materialHandlerResolver;
    private StargateEconomyAPI economyManager;

    /**
     * @param managers <p>A set of managers as listed in this class</p>
     */
    public StargateAPIMock(Object... managers){
        for(Object aManager : managers){
            if(aManager instanceof StorageAPI storageAPI){
                this.storageAPI = storageAPI;
            }
            else if(aManager instanceof RegistryAPI registryAPI){
                this.registry = registryAPI;
            }
            else if(aManager instanceof ConfigurationAPI configurationAPI) {
                this.configurationAPI = configurationAPI;
            }
            else if(aManager instanceof LanguageManager languageManager){
                this.languageManager = languageManager;
            }
            else if(aManager instanceof  BungeeManager bungeeManager){
                this.bungeeManager = bungeeManager;
            }
            else if(aManager instanceof MaterialHandlerResolver materialHandlerResolver){
                this.materialHandlerResolver = materialHandlerResolver;
            }
            else if(aManager instanceof StargateEconomyAPI economyManager){
                this.economyManager = economyManager;
            }
        }
        if(storageAPI == null){
            this.storageAPI = new StorageMock();
        }
        if(registry == null){
            this.registry = new StargateRegistry(this.storageAPI);
        }
        if(this.configurationAPI == null){
            this.configurationAPI = new ConfigurationAPIMock();
        }
        if(this.languageManager == null){
            this.languageManager = new LanguageManagerMock();
        }
        if(this.bungeeManager == null){
            this.bungeeManager = new StargateBungeeManager(registry,languageManager);
        }
        if(this.materialHandlerResolver == null){
            this.materialHandlerResolver = new MaterialHandlerResolver(registry,storageAPI);
        }
        if(this.economyManager == null) {
            this.economyManager = new FakeEconomyManager();
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
    public MaterialHandlerResolver getMaterialHandlerResolver() {
        return materialHandlerResolver;
    }
}
