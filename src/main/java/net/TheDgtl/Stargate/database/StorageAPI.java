package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.StargateInitializationException;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;

import java.util.Set;

public interface StorageAPI {

    /**
     * Loads all portals from storage and adds them to the portal registry
     * 
     * @param registry <p> The registry to load the portals into </p>
     */
    void loadFromStorage(RegistryAPI registry);
    
    /**
     * Saves the given portal to storage
     *
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     */
    boolean savePortalToStorage(RealPortal portal, PortalType portalType);

    /**
     * Removes a portal and its associated data from storage
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to remove</p>
     */
    void removePortalFromStorage(Portal portal, PortalType portalType);

    /**
     * Loads all settings
     *
     * @param stargate <p>An instance of stargate</p>
     */
    void load(Database database, Stargate stargate) throws StargateInitializationException;

    /**
     * Set misc data of a portal, used be addons
     * 
     * @param data <p> Any data </p>
     */
    void setPortalData(String data);
    
    /**
     * Get misc data of a portal, used be addons
     * 
     * @return <p> Data </p>
     */
    String getPortalData();
    
    /**
     * Set misc data of a portalposition, used be addons
     * 
     * @param data <p> Any data </p>
     */
    void setPortalPositionData();
    
    /**
     * Get misc data of a portalposition, used be addons
     * 
     * @return <p> Data </p>
     */
    String getPortalPositionData();
    
    /**
     * Creates a new network unassigned to a registry
     *
     * @param networkName <p>The name of the new network</p>
     * @param flags       <p>The flag set used to look for network flags</p>
     * @return The network that was created
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    Network createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException;


    /**
     * "Starts" the inter-server connection by setting this server's portals as online
     */
    void startInterServerConnection();
    
    /**
     * Add a new flagtype
     * 
     * @param flagChar
     */
    void addFlagType(char flagChar);
    
    /**
     * Add a new type of portalPosition
     * 
     * @param portalPositionTypeName
     */
    void addPortalPositionType(String portalPositionTypeName);

}
