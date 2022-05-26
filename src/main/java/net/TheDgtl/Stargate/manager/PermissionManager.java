package net.TheDgtl.Stargate.manager;

import java.util.Set;

import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.RealPortal;

public interface PermissionManager {
    /**
     * Gets all flags usable by the entity
     *
     * @param flags <p>The flags to check if the entity can use</p>
     * @return <p>The flags usable by the entity</p>
     */
    public Set<PortalFlag> returnDisallowedFlags(Set<PortalFlag> flags);
    
    /**
     * Check if entity has permission to access portal
     *
     * @param portal <p> The portal to be accessed </p>
     * @return <p> If entity has permission </p>
     */
    public boolean hasAccessPermission(RealPortal portal);
    
    /**
     * Check if entity has permission to create portal
     *
     * @param portal <p> The portal to be created </p>
     * @return <p> If entity has permission </p>
     */
    public boolean hasCreatePermissions(RealPortal portal);
    
    /**
     * Check if entity has permission to destroy portal
     *
     * @param portal <p> The portal to be destroyed </p>
     * @return <p> If entity has permission </p>
     */
    public boolean hasDestroyPermissions(RealPortal portal);
    
    /**
     * Check if entity has permission to open portal
     *
     * @param entrance <p> The portal the entity is opening </p>
     * @param exit     <p> The destination portal </p>
     * @return <p> If entity has permission </p>
     */
    public boolean hasOpenPermissions(RealPortal entrance, Portal exit);
    
    /**
     * Check if the entity has permission to teleport through portal
     *
     * @param entrance <p> The portal the entity is entering </p>
     * @return <p> If entity has permission </p>
     */
    public boolean hasTeleportPermissions(RealPortal entrance);
    
    /**
     * Checks whether the entity is allowed to create stargates in the given network
     *
     * @param network <p>The formatted name of the network to check</p>
     * @return <p>True if the entity is allowed to create stargates</p>
     */
    public boolean canCreateInNetwork(String network);
    
    /**
     * Gets the deny-message to display if a previous permission check returned false
     *
     * @return <p>The message to display when telling the player the action has been denied</p>
     */
    public String getDenyMessage();
}
