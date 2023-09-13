package org.sgrewritten.stargate.api.permission;

import org.sgrewritten.stargate.network.NetworkType;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.util.Set;

/**
 * An interface describing a permission manager
 */
@SuppressWarnings("unused")
public interface PermissionManager {

    /**
     * Gets all flags usable by the entity
     *
     * @param flags <p>The flags to check if the entity can use</p>
     * @return <p>The flags usable by the entity</p>
     */
    Set<PortalFlag> returnDisallowedFlags(Set<PortalFlag> flags);

    /**
     * Check if entity has permission to access portal
     *
     * @param portal <p> The portal to be accessed </p>
     * @return <p> If entity has permission </p>
     */
    boolean hasAccessPermission(RealPortal portal);

    /**
     * Check if entity has permission to create portal
     *
     * @param portal <p> The portal to be created </p>
     * @return <p> If entity has permission </p>
     */
    boolean hasCreatePermissions(RealPortal portal);

    /**
     * Check if entity has permission to destroy portal
     *
     * @param portal <p> The portal to be destroyed </p>
     * @return <p> If entity has permission </p>
     */
    boolean hasDestroyPermissions(RealPortal portal);

    /**
     * Check if entity has permission to open portal
     *
     * @param entrance <p> The portal the entity is opening </p>
     * @param exit     <p> The destination portal </p>
     * @return <p> If entity has permission </p>
     */
    boolean hasOpenPermissions(RealPortal entrance, Portal exit);

    /**
     * Check if the entity has permission to teleport through portal
     *
     * @param entrance <p> The portal the entity is entering </p>
     * @return <p> If entity has permission </p>
     */
    boolean hasTeleportPermissions(RealPortal entrance);

    /**
     * Checks whether the entity is allowed to create stargates in the given network
     *
     * @param network <p>The name of the network to check</p>
     * @param type    <p> The type if the network to check</p>
     * @return <p>True if the entity is allowed to create stargates</p>
     */
    boolean canCreateInNetwork(String network, NetworkType type);

    /**
     * Gets the deny-message to display if a previous permission check returned false
     *
     * @return <p>The message to display when telling the player the action has been denied</p>
     */
    String getDenyMessage();
}
