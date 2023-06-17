package org.sgrewritten.stargate.network.portal;

import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;

/**
 * A position of a portal's control block
 */
public class PortalPosition {

    private final PositionType positionType;
    private final BlockVector positionLocation;

    /**
     * Instantiates a new portal position
     *
     * @param positionType     <p>The type of this portal position</p>
     * @param positionLocation <p>The location of this portal position</p>
     */
    public PortalPosition(PositionType positionType, BlockVector positionLocation) {
        this.positionType = positionType;
        this.positionLocation = positionLocation;
    }

    /**
     * Gets this portal position's type
     *
     * @return <p>This portal position's type</p>
     */
    public PositionType getPositionType() {
        return this.positionType;
    }

    /**
     * Gets this portal position's location
     *
     * @return <p>This portal position's location</p>
     */
    public BlockVector getPositionLocation() {
        return this.positionLocation;
    }

    /**
     * @param portal <p> The portal which this position belongs to </p>
     * @return
     */
    public String getMetaData(RealPortal portal) {
        try {
            return Stargate.getStorageAPIStatic().getPortalPositionMetaData(portal, this, portal.getStorageType());
        } catch (StorageReadException e) {
            Stargate.log(e);
            return null;
        }
    }

    public void setMetaData(RealPortal portal, String data) {
        try {
            Stargate.getStorageAPIStatic().setPortalPositionMetaData(portal, this, data, portal.getStorageType());
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PortalPosition otherPortalPosition)) {
            return false;
        }
        return otherPortalPosition.getPositionLocation().equals(this.getPositionLocation());
    }

    @Override
    public String toString() {
        return String.format("{x=%d,y=%d,z=%d,%s}", positionLocation.getBlockX(), positionLocation.getBlockY(), positionLocation.getBlockZ(), positionType);
    }
}
