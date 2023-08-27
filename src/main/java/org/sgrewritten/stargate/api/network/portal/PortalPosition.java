package org.sgrewritten.stargate.api.network.portal;

import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.PositionType;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;

import java.util.Objects;

/**
 * A position of a portal's control block
 */
public class PortalPosition {

    private final PositionType positionType;
    private final BlockVector relativePositionLocation;
    private final String pluginName;
    private boolean active;

    /**
     * Instantiates a new active portal position
     *
     * @param positionType     <p>The type of this portal position</p>
     * @param relativePositionLocation <p> The relative location of this portal to that of the gate</p>
     * @param pluginName <p> The name of the plugin this position relates to</p>
     */
    public PortalPosition(@NotNull PositionType positionType, @NotNull BlockVector relativePositionLocation, @NotNull String pluginName) {
        this(positionType,relativePositionLocation,pluginName,true);
    }

    /**
     * Instantiates a new active portal position
     *
     * @param positionType     <p>The type of this portal position</p>
     * @param relativePositionLocation <p> The relative location of this portal to that of the gate</p>
     * @param pluginName <p> The name of the plugin this position relates to</p>
     * @param active <p> If the position is active </p>
     */
    public PortalPosition(@NotNull PositionType positionType, @NotNull BlockVector relativePositionLocation, @NotNull String pluginName, boolean active) {
        this.positionType = Objects.requireNonNull(positionType);
        this.relativePositionLocation = Objects.requireNonNull(relativePositionLocation);
        this.pluginName = Objects.requireNonNull(pluginName);
        this.active = active;
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
     * Gets this portal position's relative location to that of the gate
     *
     * @return <p>This portal position's relative location</p>
     */
    public BlockVector getRelativePositionLocation() {
        return this.relativePositionLocation;
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

    public String getPluginName() {
        return this.pluginName;
    }
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PortalPosition otherPortalPosition)) {
            return false;
        }
        return otherPortalPosition.getRelativePositionLocation().equals(this.getRelativePositionLocation());
    }

    @Override
    public String toString() {
        return String.format("{x=%d,y=%d,z=%d,%s}", relativePositionLocation.getBlockX(), relativePositionLocation.getBlockY(), relativePositionLocation.getBlockZ(), positionType);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
