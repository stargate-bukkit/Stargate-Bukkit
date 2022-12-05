package org.sgrewritten.stargate.network;

import org.bukkit.entity.Player;
import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.formatting.HighlightingStyle;

import java.util.Collection;
import java.util.Set;

/**
 * A description of a network
 */
@SuppressWarnings("unused")
public interface Network {

    /**
     * Gets all portals belonging to this network
     *
     * @return <p>All portals belonging to this network</p>
     */
    Collection<Portal> getAllPortals();

    /**
     * Gets the portal with the given name
     *
     * @param name <p>The name of the portal to get</p>
     * @return <p>The portal with the given name, or null if not found</p>
     */
    Portal getPortal(String name);

    /**
     * Removes the given portal from this network
     *
     * @param portal             <p>The portal to remove</p>
     * @param removeFromDatabase <p>Whether to also remove the portal from the database</p>
     */
    void removePortal(Portal portal, boolean removeFromDatabase);

    /**
     * Adds the given portal to this network
     *
     * @param portal         <p>The portal to add</p>
     * @param saveToDatabase <p>Whether to also save the portal to the database, only instances of RealPortal can be saved</p>
     * @throws NameErrorException if portal a portal with that name already exist in the network
     */
    void addPortal(Portal portal, boolean saveToDatabase) throws NameErrorException;

    /**
     * Checks whether there is already a portal in this network with the given name
     *
     * @param name <p>The name to check for</p>
     * @return <p>True if an existing portal is already using the given name</p>
     */
    boolean isPortalNameTaken(String name);

    /**
     * Updates all portals in this network
     */
    void updatePortals();

    /**
     * Gets names of all portals available to the given player from the given portal
     *
     * @param player    <p>The player to get portals </p>
     * @param requester <p>The portal the player is viewing other portals from</p>
     * @return <p>The names of all portals the player is allowed to see</p>
     */
    Set<String> getAvailablePortals(Player player, Portal requester);

    /**
     * Destroys this network and every portal contained in it
     */
    void destroy();

    /**
     * Gets the name of this network
     *
     * @return <p>The name of this network</p>
     */
    String getName();


    /**
     * Gets the current number of portals in this network
     *
     * @return <p>The size of this network</p>
     */
    int size();

    /**
     * Assign the network to a registry
     *
     * @param registry <p>The registry API to register to</p>
     */
    void assignToRegistry(RegistryAPI registry);


    /**
     * Gets the style this network should be highlighted with by default
     *
     * @return <p>The highlighting style of this network</p>
     */
    HighlightingStyle getHighlightingStyle();

    /**
     * Gets the unique identifier for this network
     *
     * @return <p>The unique identifier for this network</p>
     */
    String getId();

    NetworkType getType();

}
