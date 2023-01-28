package org.sgrewritten.stargate.network.portal;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.api.network.StorageType;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;

import java.util.Set;
import java.util.UUID;

/**
 * The data contained within a Portal.
 */
public class PortalData {

    /**
     * The name of the portal.
     */
    public String name;
    /**
     * The name of the network that the portal is associated with.
     */
    public String networkName;
    /**
     * The name of the portal stored as this portal's destination.
     */
    public String destination;
    /**
     * The name of the world that this portal is located in.
     */
    public String worldName;
    /**
     * This portal's left-most block's position on the X axis.
     */
    public int topLeftX;
    /**
     * This portal's left-most block's position on the Y axis.
     */
    public int topLeftY;
    /**
     * This portal's left-most block's position on the Y axis.
     */
    public int topLeftZ;
    /**
     * A string containing all the flags associated with this portal.
     */
    public String flagString;
    /**
     * A set containing all the flags associated with this portal.
     */
    public Set<PortalFlag> flags;
    /**
     * The UUID of the player who owns this portal.
     */
    public UUID ownerUUID;
    /**
     * The name of the gate file that defines this portal's layout/format/design.
     */
    public String gateFileName;
    /**
     * Whether this portal is flipped on the Z axis.
     */
    public boolean flipZ;
    /**
     * The direction that this portal is facing.
     */
    public BlockFace facing;
    /**
     * The UUID of the server this portal was constructed on.
     */
    public String serverUUID;
    /**
     * The name of the server this portal was constructed on.
     */
    public String serverName;
    /**
     * The type associated with this portal.
     */
    public StorageType portalType;
    /**
     * The Location of the top-left block of this portal.
     */
    public Location topLeft;

}
