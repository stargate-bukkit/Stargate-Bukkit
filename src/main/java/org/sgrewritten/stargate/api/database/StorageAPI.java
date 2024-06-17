package org.sgrewritten.stargate.api.database;

import org.jetbrains.annotations.ApiStatus;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;

import java.util.Set;

/**
 * A generic API for Stargate's storage methods
 */
@SuppressWarnings("unused")
public interface StorageAPI {

    /**
     * Loads all portals from storage and adds them to the portal registry
     *
     * @param stargateAPI <p> The stargate API </p>
     * @throws StorageReadException <p>If unable to read from storage</p>
     */
    void loadFromStorage(StargateAPI stargateAPI) throws StorageReadException;

    /**
     * Saves the given portal to storage
     *
     * @param portal <p>The portal to save</p>
     * @return <p>Whether or not the portal was successfully saved</p>
     * @throws StorageWriteException <p>If unable to write to storage</p>
     */
    boolean savePortalToStorage(RealPortal portal) throws StorageWriteException;

    /**
     * Removes a portal and its associated data from storage
     *
     * @param portal <p>The portal to remove</p>
     * @throws StorageWriteException <p>If unable to write to storage</p>
     */
    void removePortalFromStorage(Portal portal) throws StorageWriteException;

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
    void setPortalPositionMetaData(RealPortal portal, PortalPosition portalPosition, String data,
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
    String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition,
                                     StorageType portalType) throws StorageReadException;

    /**
     * Creates a new network unassigned to a registry
     *
     * @param networkName <p>The name of the new network</p>
     * @param type        <p>The type of network to look for.</p>
     * @param storageType <p>Whether or not the network works across servers (I flag)</p>
     * @return The network that was created
     * @throws InvalidNameException       <p>If the given network name is invalid</p>
     * @throws NameLengthException        <p>If a name is too long or short</p>
     * @throws UnimplementedFlagException <p>If a selected flag is unimplemented</p>
     */
    Network createNetwork(String networkName, NetworkType type, StorageType storageType) throws InvalidNameException, NameLengthException, UnimplementedFlagException;


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
                           PortalPosition portalPosition) throws StorageWriteException;

    /**
     * Remove a portalPosition to a portal in the database
     *
     * @param portal         <p> A portal</p>
     * @param portalType     <p> How the portal should be considered by the database </p>
     * @param portalPosition <p> A portal position</p>
     * @throws StorageWriteException <p>If unable to write the portal position change to storage</p>
     */
    void removePortalPosition(RealPortal portal, StorageType portalType,
                              PortalPosition portalPosition) throws StorageWriteException;

    /**
     * Update the network name in the database
     * @param newName <p>The new name of the network</p>
     * @param networkName <p>The previous name of the network</p>
     * @param portalType <p>The storage type of the network</p>
     * @throws StorageWriteException <p>If unable to modify the database</p>
     */
    @ApiStatus.Internal
    void updateNetworkName(String newName, String networkName, StorageType portalType) throws StorageWriteException;

    /**
     * Update the name of a portal
     * @param newName <p>The name of the portal</p>
     * @param portalId <p>A portal id representing this portal</p>
     * @param portalType <p>How the portal is stored</p>
     * @throws StorageWriteException <p>If unable to modify the database</p>
     */
    @ApiStatus.Internal
    void updatePortalName(String newName, GlobalPortalId portalId, StorageType portalType) throws StorageWriteException;

    /**
     * Check if the networks exists in the database
     * @param netName <p>Network name</p>
     * @param portalType <p>The storage type of the network</p>
     * @return <p>True if exists</p>
     * @throws StorageReadException <p>If unable to read the database</p>
     */
    boolean netWorkExists(String netName, StorageType portalType) throws StorageReadException;

    /**
     * @return <p>The gate formats of the portals scheduled to be cleared</p>
     */
    Set<String> getScheduledGatesClearing();
}
