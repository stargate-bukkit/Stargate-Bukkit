package net.TheDgtl.Stargate.network;

import java.util.Collection;
import java.util.Set;

import org.bukkit.entity.Player;

import net.TheDgtl.Stargate.network.portal.Portal;

public interface NetworkAPI {

    
    /**
     * Gets all portals belonging to this network
     *
     * @return <p>All portals belonging to this network</p>
     */
    public Collection<Portal> getAllPortals();
    
    /**
     * Gets the portal with the given name
     *
     * @param name <p>The name of the portal to get</p>
     * @return <p>The portal with the given name, or null if not found</p>
     */
    public Portal getPortal(String name);
    
    /**
     * Removes the given portal from this network
     *
     * @param portal             <p>The portal to remove</p>
     * @param removeFromDatabase <p>Whether to also remove the portal from the database</p>
     */
    public void removePortal(Portal portal, boolean removeFromDatabase);
    
    /**
     * Adds the given portal to this network
     *
     * @param portal         <p>The portal to add</p>
     * @param saveToDatabase <p>Whether to also save the portal to the database, only instances of RealPortal can be saved</p>
     */
    public void addPortal(Portal portal, boolean saveToDatabase);
    
    /**
     * Checks whether there is already a portal in this network with the given name
     *
     * @param name <p>The name to check for</p>
     * @return <p>True if an existing portal is already using the given name</p>
     */
    public boolean isPortalNameTaken(String name);
    
    /**
     * Updates all portals in this network
     */
    public void updatePortals();
    
    /**
     * Gets names of all portals available to the given player from the given portal
     *
     * @param player    <p>The player to get portals </p>
     * @param requester <p>The portal the player is viewing other portals from</p>
     * @return <p>The names of all portals the player is allowed to see</p>
     */
    public Set<String> getAvailablePortals(Player player, Portal requester);
    
    /**
     * Destroys this network and every portal contained in it
     */
    public void destroy();
    
    /**
     * Gets the name of this network
     *
     * @return <p>The name of this network</p>
     */
    public String getName();
    
    /**
     * Gets the current number of portals in this network
     *
     * @return <p>The size of this network</p>
     */
    public int size();
}
