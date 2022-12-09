package org.sgrewritten.stargate.network;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.gate.structure.GateStructureType;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.RealPortal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The API for the stargate registry.
 *
 * @author Thorin
 */
@SuppressWarnings("unused")
public interface RegistryAPI {
    /**
     * Loads all portals from storage
     */
    void loadPortals();

    /**
     * Removes the given portal from storage
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to be removed</p>
     */
    void removePortal(Portal portal, StorageType portalType);

    /**
     * Saves the given portal to the database
     *
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     */
    void savePortal(RealPortal portal, StorageType portalType);


    /**
     * Update all portals handled by this registry
     */
    void updateAllPortals();

    /**
     * Updates all portals in the given networks
     *
     * @param networkMap <p>A map of networks</p>
     */
    void updatePortals(Map<String, ? extends Network> networkMap);

    /**
     * Get the portal with the given structure type at the given location
     *
     * @param blockLocation <p>The location the portal is located at</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    RealPortal getPortal(BlockLocation blockLocation, GateStructureType structureType);

    /**
     * Get the portal with any of the given structure types at the given location
     *
     * @param blockLocation  <p>The location the portal is located at</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    RealPortal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes);

    /**
     * Gets the portal with the given structure type at the given location
     *
     * @param location      <p>The location to check for portal structures</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    RealPortal getPortal(Location location, GateStructureType structureType);

    /**
     * Gets the portal with any of the given structure types at the given location
     *
     * @param location       <p>The location to check for portal structures</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    RealPortal getPortal(Location location, GateStructureType[] structureTypes);

    /**
     * Get the portal at the given location
     *
     * @param location <p>The location to check for portal structures</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    RealPortal getPortal(Location location);

    /**
     * Checks if any of the given blocks belong to a portal
     *
     * @param blocks <p>The blocks to check</p>
     * @return <p>True if any of the given blocks belong to a portal</p>
     */
    boolean isPartOfPortal(List<Block> blocks);

    /**
     * Checks one block away from the given location to check if it's adjacent to a portal structure
     *
     * <p>Checks North, west, south, east direction. Not up / down, as it is currently
     * not necessary and a waste of resources.</p>
     *
     * @param location      <p>The location to check for adjacency</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>True if the given location is adjacent to a location containing the given structure type</p>
     */
    boolean isNextToPortal(Location location, GateStructureType structureType);

    /**
     * Registers the existence of the given structure type in the given locations
     *
     * <p>Basically stores the portals that exist at the given locations, but using the structure type as the key to be
     * able to check locations for the given structure type.</p>
     *
     * @param structureType <p>The structure type to register</p>
     * @param locationsMap  <p>The locations and the corresponding portals to register</p>
     */
    void registerLocations(GateStructureType structureType, Map<BlockLocation, RealPortal> locationsMap);

    /**
     * Un-registers all portal blocks with the given structure type, at the given block location
     *
     * @param structureType <p>The type of structure to un-register</p>
     * @param blockLocation <p>The location to un-register</p>
     */
    void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation);


    /**
     * Creates a new network assigned to this registry
     * 
     * @param networkName   <p>The name of the new network</p>
     * @param type          <p>The type of network to create</p>
     * @param isInterserver <p>Whether to create it as a BungeeCord network</p>
     * @param isForced      <p>The authority for the creation </p>
     * @return <p> The network created </p>
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    Network createNetwork(String networkName, NetworkType type, boolean isInterserver, boolean isForced) throws NameErrorException;

    /**
     * Creates a new network assigned to this registry
     *
     * @param targetNetwork <p>The this network will attempt creation under</p>
     * @param flags       <p>The flags containing the network's enabled options</p>
     * @param isForced    <p>The authority for the creation </p>
     * @return <p> The network created </p>
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    Network createNetwork(String targetNetwork, Set<PortalFlag> flags, boolean isForced) throws NameErrorException;
    
    /**
     * Checks whether the given network name exists
     *
     * @param networkName <p>The network name to check</p>
     * @param isBungee    <p>Whether to look for a BungeeCord network</p>
     * @return <p>True if the network exists</p>
     */
    boolean networkExists(String networkName, boolean isBungee);

    /**
     * Gets the network with the given
     *
     * @param name     <p>The name of the network to get</p>
     * @param isBungee <p>Whether the network is a BungeeCord network</p>
     * @return <p>The network with the given name</p>
     */
    Network getNetwork(String name, boolean isBungee);

    /**
     * Gets the map storing all BungeeCord networks
     *
     * @return <p>All BungeeCord networks</p>
     */
    HashMap<String, Network> getBungeeNetworkMap();

    /**
     * Gets the map storing all non-BungeeCord networks
     *
     * @return <p>All non-BungeeCord networks</p>
     */
    HashMap<String, Network> getNetworkMap();

    /**
     * Rename the network to specified name
     * @param network   <p> The network to rename </p>
     * @param newName   <p> The new name of the network </p>
     * @throws NameErrorException 
     */
    void rename(Network network, String newName) throws NameErrorException;

    /**
     * 
     * @param portal    <p> The portal to rename</p>
     * @param newName   <p> The new name of the portal </p>
     * @throws NameErrorException
     */
    void rename(Portal portal, String newName) throws NameErrorException;


    /**
     * Rename the network to a non clashing name
     * @param network   <p>The network to rename </p>
     */
    void rename(Network network);
}
