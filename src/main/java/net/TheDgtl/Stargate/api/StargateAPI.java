package net.TheDgtl.Stargate.api;

import org.bukkit.configuration.Configuration;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.StorageAPI;
import net.TheDgtl.Stargate.network.RegistryAPI;

/**
 * An API to facilitate addons and integrations
 *
 * <p>API specs currently being discussed on issue #77</p>
 *
 * @author Thorin
 */
public class StargateAPI {

    
    public static RegistryAPI getPortalRegistry() {
        return Stargate.getRegistry();
    }
    
    public static StorageAPI getStorageAPI() {
        return Stargate.getStorageAPI();
    }
    
    public static void load() {
        Stargate.getInstance().load();
    }
    
    public static Configuration getConfig() {
        return Stargate.getConfigStatic();
    }
}
