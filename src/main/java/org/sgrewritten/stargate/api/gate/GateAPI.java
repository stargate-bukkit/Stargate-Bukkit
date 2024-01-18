package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.format.SignLine;
import org.sgrewritten.stargate.network.StorageType;

import java.util.List;

/**
 * Acts as an interface between a minecraft world, and a format space. In comparison to {@link org.sgrewritten.stargate.api.network.portal.Portal}
 * this interface has nothing to do with entities and teleportation.
 */
public interface GateAPI {

    /**
     * Set button and draw sign
     *
     * @param signLines  <p>an array with 4 elements, representing each line of a sign</p>
     * @param drawButton <p>whether or not include a button.</p>
     */
    void drawControlMechanisms(SignLine[] signLines, boolean drawButton);

    /**
     * Gets a copy of this gate's portal positions
     *
     * @return <p>A copy of this gate's portal positions</p>
     */
    List<PortalPosition> getPortalPositions();

    /**
     * Gets all locations of this gate containing the given structure type
     *
     * @param structureType <p>The structure type to get locations of</p>
     * @return <p>All locations containing the given structure type</p>
     */
    List<BlockLocation> getLocations(GateStructureType structureType);

    /**
     * Opens this gate
     */
    void open();

    /**
     * Closes this gate
     */
    void close();

    /**
     * Gets the exit location of this gate
     *
     * @return <p>The exit location of this gate</p>
     */
    Location getExit();

    /**
     * Gets whether this gate is currently open
     *
     * @return <p>Whether this gate is currently open</p>
     */
    boolean isOpen();

    /**
     * Gets the gate format used by this gate
     *
     * @return <p>The gate format used by this gate</p>
     */
    GateFormatAPI getFormat();

    /**
     * Gets the block face defining this gate's direction
     *
     * @return <p>The block face defining this gate's direction</p>
     */
    BlockFace getFacing();

    /**
     * Gets a vector relative to this gate's top-left location using the given location
     *
     * @param location <p>The location to turn into a relative location</p>
     * @return <p>A location relative to this gate's top-left location</p>
     */
    Vector getRelativeVector(Location location);

    /**
     * Gets whether this gate has been flipped on the z-axis
     *
     * @return <p>Whether this gate has been flipped on the z-axis</p>
     */
    boolean getFlipZ();

    /**
     * Gets a location from a relative vector
     *
     * @param vector <p>The vector defining a location</p>
     * @return <p>The location corresponding to the given vector</p>
     */
    Location getLocation(@NotNull Vector vector);

    /**
     * Calculates all portal positions for this gate
     *
     * @param alwaysOn <p>Whether this gate is always on</p>
     */
    void calculatePortalPositions(boolean alwaysOn);

    /**
     * Gets this gate's top-left location
     *
     * @return <p>This gate's top-left location</p>
     */
    Location getTopLeft();

    /**
     * Add a position specific for this Gate
     *
     * @param location <p> The location of the position </p>
     * @param type     <p> The type of position </p>
     * @return <p>The added portal position</p>
     */
    PortalPosition addPortalPosition(Location location, PositionType type, String pluginName);

    /**
     * Add a position specific to this Gate
     *
     * @param portalPosition <p> The portal position</p>
     */
    void addPortalPosition(PortalPosition portalPosition);

    /**
     * Remove portal position at specified location
     *
     * @param location The location of the portal position to be removed
     * @return <p> The removed portal position</p>
     */
    @Nullable PortalPosition removePortalPosition(Location location);

    /**
     * Remove a portal position from this gate. Does not remove the portal position in the registry or database.
     * Please use {@link org.sgrewritten.stargate.api.network.RegistryAPI#removePortalPosition(Location)} with conjunction
     * to {@link org.sgrewritten.stargate.api.database.StorageAPI#removePortalPosition(RealPortal, StorageType, PortalPosition)}
     *
     * @param portalPosition <p>The portal position to remove</p>
     */
    @ApiStatus.Internal
    void removePortalPosition(PortalPosition portalPosition);

    /**
     * Modify the world such that the gate follows its gateformat
     */
    void forceGenerateStructure();

    /**
     * Assign a real portal to this gate
     * @param realPortal <p>The portal to assign to this gate</p>
     */
    @ApiStatus.Internal
    void assignPortal(RealPortal realPortal);

    /**
     * Get the portal assigned to this portal position
     * @return <p>The portal assigned to this portal position</p>
     */
    RealPortal getPortal();
}
