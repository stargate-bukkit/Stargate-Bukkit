package org.sgrewritten.stargate.database;

import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.exception.StargateInitializationException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.PortalType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.network.portal.RealPortal;

import java.util.Set;

/**
 * A generic API for Stargate's storage methods
 */
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
    void load(SQLDatabaseAPI database, Stargate stargate) throws StargateInitializationException;

    /**
     * Set misc data of a portal
     *
     * @param portal <p> A portal </p>
     * @param data   <p> Any data </p>
     * @throws SQLException
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void setPortalMetaData(Portal portal, String data, PortalType portalType);

    /**
     * Get misc data of a portal
     *
     * @param portal <p> A portal </p>
     * @return <p> Data </p>
     * @throws StorageReadException 
     * @throws SQLException
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    String getPortalMetaData(Portal portal, PortalType portalType) throws StorageReadException;

    /**
     * Set misc data of a portalposition
     *
     * @param portal         <p> A portal </p>
     * @param portalPosition <p> A portalPosition </p>
     * @param data           <p> Any data </p>
     * @throws StorageWriteException 
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void setPortalPositionMetaData(RealPortal portal, PortalPosition portalPosition, String data,
                                   PortalType portalType) throws StorageWriteException;

    /**
     * Get misc data of a portalposition
     *
     * @param portal         <p> A portal </p>
     * @param portalPosition <p> A portalPosition </p>
     * @return <p> Data </p>
     * @throws StorageReadException 
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition,
                                     PortalType portalType) throws StorageReadException;

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
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void addFlagType(Character flagChar);

    /**
     * Add a new type of portalPosition
     *
     * @param portalPositionTypeName
     * @throws SQLException
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void addPortalPositionType(String portalPositionTypeName);

    /**
     * Add a flag to a portal in the database
     *
     * @param flagChar   <p> The character representation of that flag </p>
     * @param portal     <p> A portal </p>
     * @param portalType <p>How the portal should be considered by the database </p>
     * @throws SQLException
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void addFlag(Character flagChar, Portal portal, PortalType portalType);

    /**
     * Remove a flag to a portal in the database
     *
     * @param flagChar   <p> The character representation of that flag </p>
     * @param portal     <p> A portal </p>
     * @param portalType <p>How the portal should be considered by the database </p>
     * @throws SQLException
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void removeFlag(Character flagChar, Portal portal, PortalType portalType);

    /**
     * Add a portalPosition to a portal in the database
     *
     * @param portal         <p> A portal</p>
     * @param portalType     <p> How the portal should be considered by the database </p>
     * @param portalPosition <p>A portal position</p>
     * @throws SQLException
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void addPortalPosition(RealPortal portal, PortalType portalType, PortalPosition portalPosition);

    /**
     * Remove a portalPosition to a portal in the database
     *
     * @param portal         <p> A portal</p>
     * @param portalType     <p> How the portal should be considered by the database </p>
     * @param portalPosition <p> A portal position</p>
     * @throws SQLException
     */
    //TODO: A generic storage API should never throw specific exceptions such as SQLException. It makes no sense to 
    // throw an SQL exception if using YML storage
    void removePortalPosition(RealPortal portal, PortalType portalType, PortalPosition portalPosition);


}
