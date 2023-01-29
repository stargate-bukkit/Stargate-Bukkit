package org.sgrewritten.stargate.api.database;

import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.NetworkType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.StorageType;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;

/**
 * A generic API for Stargate's storage methods
 */
@SuppressWarnings("unused")
public interface StorageAPI {

    /**
     * Loads all portals from storage and adds them to the portal registry
     *
     * @param registry       <p> The registry to load the portals into </p>
     * @param economyManager <p> The handle all economical transactions from the portal will be based of</p>
     * @throws StorageReadException <p>If unable to read from storage</p>
     */
    void loadFromStorage(RegistryAPI registry, StargateEconomyAPI economyManager) throws StorageReadException;

    /**
     * Saves the given portal to storage
     *
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     * @return <p>Whether or not the portal was successfully saved</p>
     * @throws StorageWriteException <p>If unable to write to storage</p>
     */
    boolean savePortalToStorage(RealPortal portal, StorageType portalType) throws StorageWriteException;

    /**
     * Removes a portal and its associated data from storage
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to remove</p>
     * @throws StorageWriteException <p>If unable to write to storage</p>
     */
    void removePortalFromStorage(Portal portal, StorageType portalType) throws StorageWriteException;

    /**
     * Set misc data of a portal
     *
     * @param portal     <p>A portal</p>
     * @param data       <p>Any data</p>
     * @param portalType <p>The portal's expected type</p>
     * @throws StorageWriteException <p>If unable to successfully set the new portal data</p>
     */
    void setPortalMetaData(Portal portal, String data, StorageType portalType) throws StorageWriteException;

    /**
     * Get misc data of a portal
     *
     * @param portal     <p>A portal</p>
     * @param portalType <p>The portal's expected type</p>
     * @return <p>Data</p>
     * @throws StorageReadException <p>If unable to successfully get the portal data</p>
     */
    String getPortalMetaData(Portal portal, StorageType portalType) throws StorageReadException;

    /**
     * Set misc data of a portal position
     *
     * @param portal         <p>A portal</p>
     * @param portalPosition <p>A portalPosition</p>
     * @param data           <p>Any data</p>
     * @param portalType     <p>The type of portal to set the data for</p>
     * @throws StorageWriteException <p>If unable to set the metadata for the portal</p>
     */
    void setPortalPositionMetaData(RealPortal portal, GatePosition portalPosition, String data,
                                   StorageType portalType) throws StorageWriteException;

    /**
     * Get misc data of a portal position
     *
     * @param portal         <p>A portal</p>
     * @param portalPosition <p>A portalPosition</p>
     * @param portalType     <p>The portal's expected type</p>
     * @return <p>Data</p>
     * @throws StorageReadException <p>If unable to successfully read the portal metadata</p>
     */
    String getPortalPositionMetaData(Portal portal, GatePosition portalPosition,
                                     StorageType portalType) throws StorageReadException;

    /**
     * Creates a new network unassigned to a registry
     *
     * @param networkName   <p>The name of the new network</p>
     * @param type          <p>The type of network to look for.</p>
     * @param isInterServer <p>Whether or not the network works across servers (I flag)</p>
     * @return The network that was created
     * @throws InvalidNameException       <p>If the given network name is invalid</p>
     * @throws NameLengthException        <p>If a name is too long or short</p>
     * @throws UnimplementedFlagException <p>If a selected flag is unimplemented</p>
     */
    Network createNetwork(String networkName, NetworkType type, boolean isInterServer) throws InvalidNameException, NameLengthException, UnimplementedFlagException;


    /**
     * "Starts" the inter-server connection by setting this server's portals as online
     *
     * @throws StorageWriteException <p>If unable to write to storage</p>
     */
    void startInterServerConnection() throws StorageWriteException;

    /**
     * Add a new flag type
     *
     * @param flagChar <p>The character identifying the flag</p>
     * @throws StorageWriteException <p>If unable to write the flag to storage</p>
     */
    void addFlagType(char flagChar) throws StorageWriteException;

    /**
     * Add a new type of portalPosition
     *
     * @param portalPositionTypeName <p>The name of the portal type to add</p>
     * @throws StorageWriteException <p>If unable to write the portal position type to storage</p>
     */
    void addPortalPositionType(String portalPositionTypeName) throws StorageWriteException;

    /**
     * Add a flag to a portal in the database
     *
     * @param flagChar   <p> The character representation of that flag </p>
     * @param portal     <p> A portal </p>
     * @param portalType <p>How the portal should be considered by the database </p>
     * @throws StorageWriteException <p>If unable to write the flag change to storage</p>
     */
    void addFlag(Character flagChar, Portal portal, StorageType portalType) throws StorageWriteException;

    /**
     * Remove a flag to a portal in the database
     *
     * @param flagChar   <p>The character representation of that flag</p>
     * @param portal     <p>A portal</p>
     * @param portalType <p>How the portal should be considered by the database</p>
     * @throws StorageWriteException <p>Uf unable to write the flag change to storage</p>
     */
    void removeFlag(Character flagChar, Portal portal, StorageType portalType) throws StorageWriteException;

    /**
     * Add a portalPosition to a portal in the database
     *
     * @param portal         <p>A portal</p>
     * @param portalType     <p>How the portal should be considered by the database</p>
     * @param portalPosition <p>A portal position</p>
     * @throws StorageWriteException <p>If unable to write the new portal position to storage</p>
     */
    void addPortalPosition(RealPortal portal, StorageType portalType,
                           GatePosition portalPosition) throws StorageWriteException;

    /**
     * Remove a portalPosition to a portal in the database
     *
     * @param portal         <p> A portal</p>
     * @param portalType     <p> How the portal should be considered by the database </p>
     * @param portalPosition <p> A portal position</p>
     * @throws StorageWriteException <p>If unable to write the portal position change to storage</p>
     */
    void removePortalPosition(RealPortal portal, StorageType portalType,
                              GatePosition portalPosition) throws StorageWriteException;

    void updateNetworkName(String newName, String networkName, StorageType portalType) throws StorageWriteException;

    void updatePortalName(String newName, GlobalPortalId portalId, StorageType portalType) throws StorageWriteException;

    boolean netWorkExists(String netName, StorageType portalType) throws StorageReadException;


}
