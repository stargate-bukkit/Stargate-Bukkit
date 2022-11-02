package net.TheDgtl.Stargate.database;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.exception.StargateInitializationException;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.PortalType;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.PortalPosition;
import net.TheDgtl.Stargate.network.portal.RealPortal;

import java.sql.SQLException;
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
     * Set misc data of a portal
     * 
     * @param portal <p> A portal </p>
     * @param data <p> Any data </p>
     */
    void setPortalMetaData(Portal portal, String data);
    
    /**
     * Get misc data of a portal
     * 
     * @param portal <p> A portal </p>
     * @return <p> Data </p>
     */
    String getPortalMetaData(Portal portal);
    
    /**
     * Set misc data of a portalposition
     * 
     * @param portal <p> A portal </p>
     * @param portalPosition <p> A portalPosition </p>
     * @param data <p> Any data </p>
     */
    void setPortalPositionMetaData(Portal portal, PortalPosition portalPosition, String data);
    
    /**
     * Get misc data of a portalposition
     * 
     * @param portal <p> A portal </p>
     * @param portalPosition <p> A portalPosition </p>
     * @return <p> Data </p>
     */
    String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition);
    
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
     * @throws SQLException 
     */
    void addFlagType(Character flagChar) throws SQLException;
    
    /**
     * Add a new type of portalPosition
     * 
     * @param portalPositionTypeName
     * @throws SQLException 
     */
    void addPortalPositionType(String portalPositionTypeName) throws SQLException;

    /**
     * Add a flag to a portal in the database
     * @param flagChar  <p> The character representation of that flag </p>
     * @param portal    <p> A portal </p>
     * @param portalType    <p>How the portal should be considered by the database </p>
     * @throws SQLException
     */
    void addFlag(Character flagChar, Portal portal, PortalType portalType) throws SQLException;

    /**
     * Remove a flag to a portal in the database
     * @param flagChar  <p> The character representation of that flag </p>
     * @param portal    <p> A portal </p>
     * @param portalType    <p>How the portal should be considered by the database </p>
     * @throws SQLException
     */
    void removeFlag(Character flagChar, Portal portal, PortalType portalType) throws SQLException;
    
    /**
     * Add a portalPosition to a portal in the database
     * @param portal    <p> A portal</p>
     * @param portalType    <p> How the portal should be considered by the database </p>
     * @param portalPosition    <p>A portal position</p>
     * @throws SQLException
     */
    void addPortalPosition(RealPortal portal, PortalType portalType, PortalPosition portalPosition) throws SQLException;

    /**
     * Remove a portalPosition to a portal in the database
     * @param portal    <p> A portal</p>
     * @param portalType    <p> How the portal should be considered by the database </p>
     * @param portalPosition    <p> A portal position</p>
     * @throws SQLException
     */
    void removePortalPosition(RealPortal portal, PortalType portalType, PortalPosition portalPosition) throws SQLException;

}
