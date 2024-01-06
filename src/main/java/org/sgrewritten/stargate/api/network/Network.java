package org.sgrewritten.stargate.api.network;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.proxy.PluginMessageSender;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.network.StorageType;
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
     * @param portal <p>The portal to remove</p>
     */
    void removePortal(Portal portal);

    /**
     * Adds the given portal to this network
     *
     * @param portal <p>The portal to add</p>
     * @throws NameConflictException <p> if portal a portal with that name already exist in the network </p>
     */
    void addPortal(Portal portal) throws NameConflictException;

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

    /**
     * Gets the NetworkType of this network
     *
     * @return <p> The NetworkType of this network </p>
     */
    NetworkType getType();

    /**
     * Gets how the network is stored
     *
     * @return <p> The storage type of the portal </p>
     */
    StorageType getStorageType();

    /**
     * Change the name of the network
     * <p>
     * Does not save to database. Use {@link NetworkManager#rename(Network, String)} instead. </p>
     *
     * @param newName <p>The new name of the network</p>
     * @throws InvalidNameException
     * @throws NameLengthException
     * @throws UnimplementedFlagException
     */
    @ApiStatus.Internal
    void setID(String newName) throws InvalidNameException, NameLengthException, UnimplementedFlagException;

    /**
     *
     * @return <p>The plugin message sender</p>
     */
    PluginMessageSender getPluginMessageSender();

    /**
     * Renames the portal, does not save to database and is also not cross server compatible.
     * Instead use {@link org.sgrewritten.stargate.api.network.NetworkManager#rename(Portal, String)}
     *
     * @param newName
     * @param oldName
     */
    @ApiStatus.Internal
    void renamePortal(String newName, String oldName) throws InvalidNameException;
}
