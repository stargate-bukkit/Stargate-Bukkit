package org.sgrewritten.stargate.api.gate;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;

/**
 * A position of a portal's control block
 */
public abstract class GatePosition {

    protected final BlockVector positionLocation;

    /**
     * Instantiates a new portal position. Note that you can get this vector by
     * doing {@link GateAPI#getRelativeVector(org.bukkit.Location)}
     *
     * @param positionLocation <p>The location of this portal position in gatespace</p>
     */
    public GatePosition(@NotNull BlockVector positionLocation) {
        this.positionLocation = positionLocation;
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
     * @return <p>The metadata of the position</p>
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
        if (!(other instanceof GatePosition otherPortalPosition)) {
            return false;
        }
        return otherPortalPosition.getPositionLocation().equals(this.getPositionLocation());
    }

    @Override
    public String toString() {
        return String.format("PortalPosition{x=%d,y=%d,z=%d, %s}", positionLocation.getBlockX(), positionLocation.getBlockY(), positionLocation.getBlockZ(), getName());
    }

    /**
     * What should happen when a player clicks on this block?
     *
     * @param event  <p>A PlayerInteractEvent</p>
     * @param portal <p>The portal related</p>
     * @return <p> true if cancelled </p>
     */
    public abstract boolean onBlockClick(PlayerInteractEvent event, RealPortal portal);

    /**
     * Get the unique name that identifies this gate position. Note that it only has to be unique within a gate
     *
     * @return <p>The unique name that identifies this gate position</p>
     */
    public abstract String getName();
}
