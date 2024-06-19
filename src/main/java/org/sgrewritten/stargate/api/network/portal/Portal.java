package org.sgrewritten.stargate.api.network.portal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargate.exception.name.NameConflictException;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargate.network.portal.GlobalPortalId;

import java.util.UUID;

/**
 * An interface describing any portal
 */
public interface Portal {

    /**
     * The method used when destroying a portal
     *
     * <p>Mainly clears the sign, see {@link org.sgrewritten.stargate.api.network.NetworkManager#destroyPortal(RealPortal)}
     * to really destroy a portal</p>
     */
    @ApiStatus.Internal
    void destroy();

    /**
     * Checks whether this portal is currently open
     *
     * @return <p>True if this portal is currently open</p>
     */
    boolean isOpen();

    /**
     * Checks whether this portal is currently open and the given target is the one the portal opened for
     *
     * @param target <p>The target to check</p>
     * @return <p>True if the portal has been opened for the given target</p>
     */
    boolean isOpenFor(Entity target);

    /**
     * Teleports an entity to this portal
     *
     * @param target <p>The target entity to teleport</p>
     * @param origin <p>The origin portal the entity is teleporting from</p>
     */
    void teleportHere(Entity target, RealPortal origin);

    /**
     * Closes this portal
     *
     * @param forceClose <p>Whether to force this portal to close, even if set to always on or similar</p>
     */
    void close(boolean forceClose);

    /**
     * Opens this portal for the given player
     *
     * @param player <p>The player to open this portal for</p>
     */
    void open(Player player);

    /**
     * Gets the name of this portal
     *
     * @return <p>The name of this portal</p>
     */
    String getName();

    /**
     * Forces this portal to temporarily go to the given destination regardless of the normal destination(s)
     *
     * @param destination <p>The destination this portal should temporarily connect ot</p>
     */
    @SuppressWarnings("unused")
    void overrideDestination(Portal destination);

    /**
     * Gets the network this portal belongs to
     *
     * @return <p>The network this portal belongs to</p>
     */
    Network getNetwork();

    /**
     * Changes the network this portal belongs to
     *
     * @param targetNetwork <p>The new network this portal should belong to</p>
     * @throws NameConflictException <p>If the given network name is invalid</p>
     */
    void setNetwork(Network targetNetwork) throws NameConflictException;

    /**
     * Changes the player this portal belongs to
     *
     * <p>DOES NOT SAVE TO DATABASE</p>
     *
     * @param targetPlayer <p>The new player this portal should belong to</p>
     */
    void setOwner(UUID targetPlayer);

    /**
     * Checks whether this portal has the given portal flag enabled
     *
     * @param flag <p>The portal flag to check for</p>
     * @return <p>True if this portal has the given portal flag enabled</p>
     */
    boolean hasFlag(PortalFlag flag);

    /**
     * Remove a flag from this portal (does not do so from storage)
     * @param flag <p>Flag to remove</p>
     */
    void removeFlag(PortalFlag flag);

    /**
     * Add a flag from this portal (does not do so to storage)
     * @param flag <p>Flag to add</p>
     */
    void addFlag(PortalFlag flag);

    /**
     * Gets all of this portal's portal flags in the form of a string
     *
     * <p>This returns the concatenation of all character representations for the flags used by this portal.</p>
     *
     * @return <p>All of this portal's portal flags in the form of a string</p>
     */
    String getAllFlagsString();

    /**
     * Gets the UUID of this portal's owner
     *
     * <p>A portal's owner is the player that created the portal.</p>
     *
     * @return <p>The UUID of this portal's owner</p>
     */
    UUID getOwnerUUID();

    /**
     * Looks into available portals to connect to, and updates appearance and behaviour accordingly
     */
    void updateState();

    /**
     * Gets the unique identifier for this portal
     *
     * @return <p>The unique identifier for this portal</p>
     */
    String getId();

    /**
     * Gets the globally unique identifier for this portal
     *
     * @return <p>The globally unique identifier for this portal</p>
     */
    GlobalPortalId getGlobalId();

    /**
     * Gets how the portal is stored
     *
     * @return <p> The storage type of the portal </p>
     */
    StorageType getStorageType();

    /**
     * Changes the name of the portal. Use {@link org.sgrewritten.stargate.api.network.NetworkManager#rename(Portal, String)}
     * instead, as this does not save to database, nor update its name to the portal network.
     *
     * @param newName <p>The new name of the portal</p>
     */
    @ApiStatus.Internal
    void setName(String newName);

    /**
     * @return <p>True if this portal has been destroyed</p>
     */
    boolean isDestroyed();

}
