package org.sgrewritten.stargate.api.network.portal;

import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.MetadataHolder;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;

import java.util.Objects;

/**
 * A position of a portal's control block
 */
public class PortalPosition implements MetadataHolder {

    private final PositionType positionType;
    private final BlockVector relativePositionLocation;
    private final String pluginName;
    private boolean active;
    private @Nullable String metaData = null;
    private RealPortal portal;
    private PortalPositionAttachment attachment;

    /**
     * Instantiates a new active portal position
     *
     * @param positionType             <p>The type of this portal position</p>
     * @param relativePositionLocation <p> The relative location of this portal to that of the gate</p>
     * @param pluginName               <p> The name of the plugin this position relates to</p>
     */
    public PortalPosition(@NotNull PositionType positionType, @NotNull BlockVector relativePositionLocation, @NotNull String pluginName) {
        this(positionType, relativePositionLocation, pluginName, true);
    }

    /**
     * Instantiates a new active portal position
     *
     * @param positionType             <p>The type of this portal position</p>
     * @param relativePositionLocation <p> The relative location of this portal to that of the gate</p>
     * @param pluginName               <p> The name of the plugin this position relates to</p>
     * @param active                   <p> If the position is active </p>
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
    @NotNull
    public PositionType getPositionType() {
        return this.positionType;
    }

    /**
     * Gets this portal position's relative location to that of the gate
     *
     * @return <p>This portal position's relative location</p>
     */
    @NotNull
    public BlockVector getRelativePositionLocation() {
        return this.relativePositionLocation;
    }

    /**
     *
     * @param portal <p>The portal to assign to this portal position</p>
     */
    @ApiStatus.Internal
    public void assignPortal(RealPortal portal) {
        this.portal = Objects.requireNonNull(portal);
    }

    /**
     * @return <p>The name of the plugin that owns this portal position</p>
     */
    public @NotNull String getPluginName() {
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
    public int hashCode() {
        return relativePositionLocation.hashCode();
    }

    @Override
    public String toString() {
        return String.format("{x=%d,y=%d,z=%d,%s}", relativePositionLocation.getBlockX(), relativePositionLocation.getBlockY(), relativePositionLocation.getBlockZ(), positionType);
    }

    /**
     * @return <p> Whether the portal position is currently active</p>
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Activate/deactive this portal position
     *
     * @param active <p>Whether to activate or deactivate the portal position</p>
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void setMetadata(@Nullable String data) {
        try {
            this.metaData = Objects.requireNonNull(data);
            Stargate.getStorageAPIStatic().setPortalPositionMetaData(portal, this, data, portal.getStorageType());
        } catch (StorageWriteException e) {
            Stargate.log(e);
        }
    }

    @Override
    public String getMetadata() {
        if (metaData != null) {
            return metaData;
        }
        try {
            metaData = Stargate.getStorageAPIStatic().getPortalPositionMetaData(portal, this, portal.getStorageType());
            return metaData;
        } catch (StorageReadException e) {
            Stargate.log(e);
            return null;
        }
    }

    /**
     * @return <p>The portal owning this portal position</p>
     */
    public RealPortal getPortal() {
        return this.portal;
    }

    @ApiStatus.Internal
    public @Nullable PortalPositionAttachment getAttachment(){
        return this.attachment;
    }

    @ApiStatus.Internal
    public void setAttachment(@NotNull PortalPositionAttachment attachment){
        Objects.requireNonNull(attachment);
        if(this.attachment != null && this.attachment.getType() != attachment.getType()){
            throw new IllegalArgumentException("Can't change attachment type");
        }
        this.attachment = attachment;
    }
}
