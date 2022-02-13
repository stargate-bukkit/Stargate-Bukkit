package net.TheDgtl.Stargate.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;

import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;


/**
 * The API for the stargate registry.
 * 
 * @author Thorin
 *
 */
public interface RegistryAPI {
    /**
     * Loads all portals from storage
     */
    public void loadPortals();
    
    /**
     * Removes the given portal from storage
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to be removed</p>
     */
    public void removePortal(Portal portal, PortalType portalType);
    
    /**
     * Saves the given portal to the database
     *
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     */
    public void savePortal(RealPortal portal, PortalType portalType);
    
    
    /**
     * Update all portals handled by this registry
     */
    public void updateAllPortals();
    
    /**
     * Updates all portals in the given networks
     *
     * @param networkMap <p>A map of networks</p>
     */
    public void updatePortals(Map<String, ? extends NetworkAPI> networkMap);
    
    /**
     * Get the portal with the given structure type at the given location
     *
     * @param blockLocation <p>The location the portal is located at</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    public Portal getPortal(BlockLocation blockLocation, GateStructureType structureType);
    
    /**
     * Get the portal with any of the given structure types at the given location
     *
     * @param blockLocation  <p>The location the portal is located at</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    public Portal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes);
    
    /**
     * Gets the portal with the given structure type at the given location
     *
     * @param location      <p>The location to check for portal structures</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    public Portal getPortal(Location location, GateStructureType structureType);
    
    /**
     * Gets the portal with any of the given structure types at the given location
     *
     * @param location       <p>The location to check for portal structures</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    public Portal getPortal(Location location, GateStructureType[] structureTypes);
    
    /**
     * Get the portal at the given location
     * 
     * @param location      <p>The location to check for portal structures</p>
     * @return          <p>The found portal, or null if no portal was found</p>
     */
    public Portal getPortal(Location location);
    
    /**
     * Checks if any of the given blocks belong to a portal
     *
     * @param blocks <p>The blocks to check</p>
     * @return <p>True if any of the given blocks belong to a portal</p>
     */
    public boolean isPartOfPortal(List<Block> blocks);
    
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
    public boolean isNextToPortal(Location location, GateStructureType structureType);
    
    /**
     * Registers the existence of the given structure type in the given locations
     *
     * <p>Basically stores the portals that exist at the given locations, but using the structure type as the key to be
     * able to check locations for the given structure type.</p>
     *
     * @param structureType <p>The structure type to register</p>
     * @param locationsMap  <p>The locations and the corresponding portals to register</p>
     */
    public void registerLocations(GateStructureType structureType, Map<BlockLocation, Portal> locationsMap);

    /**
     * Un-registers all portal blocks with the given structure type, at the given block location
     *
     * @param structureType <p>The type of structure to un-register</p>
     * @param blockLocation <p>The location to un-register</p>
     */
    public void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation);
    
    /**
     * Creates a new network assigned to this registry
     *
     * @param networkName <p>The name of the new network</p>
     * @param flags       <p>The flags containing the network's enabled options</p>
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    public void createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException;

    /**
     * Checks whether the given network name exists
     *
     * @param networkName <p>The network name to check</p>
     * @param isBungee    <p>Whether to look for a BungeeCord network</p>
     * @return <p>True if the network exists</p>
     */
    public boolean networkExists(String networkName, boolean isBungee);
    
    /**
     * Gets the network with the given
     *
     * @param name     <p>The name of the network to get</p>
     * @param isBungee <p>Whether the network is a BungeeCord network</p>
     * @return <p>The network with the given name</p>
     */
    public NetworkAPI getNetwork(String name, boolean isBungee);
    
    /**
     * Gets the map storing all BungeeCord networks
     *
     * @return <p>All BungeeCord networks</p>
     */
    public HashMap<String, NetworkAPI> getBungeeNetworkList();
    
    /**
     * Gets the map storing all non-BungeeCord networks
     *
     * @return <p>All non-BungeeCord networks</p>
     */
    public HashMap<String, NetworkAPI> getNetworkList();
}
