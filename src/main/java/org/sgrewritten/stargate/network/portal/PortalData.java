package org.sgrewritten.stargate.network.portal;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.sgrewritten.stargate.network.PortalType;

import java.util.Set;
import java.util.UUID;

public class PortalData {

    public String name;
    public String networkName;
    public String destination;
    public String worldName;
    public int topLeftX;
    public int topLeftY;
    public int topLeftZ;
    public String flagString;
    public Set<PortalFlag> flags;
    public UUID ownerUUID;
    public String gateFileName;
    public boolean flipZ;
    public BlockFace facing;
    public String serverUUID;
    public String serverName;
    public PortalType portalType;
    public Location topLeft;

}
