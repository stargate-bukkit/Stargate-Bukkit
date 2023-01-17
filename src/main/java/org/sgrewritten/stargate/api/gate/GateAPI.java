package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.gate.control.ControlMechanism;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;

import java.util.List;

/**
 * An API describing a Gate
 */
public interface GateAPI {

    /**
     * Set button and draw sign
     *
     * @param signLines <p>an array with 4 elements, representing each line of a sign</p>
     * @param drawButton <p>whether or not include a button.</p>
     */
    void drawControlMechanisms(String[] signLines, boolean drawButton);

    /**
     * Gets a copy of this gate's portal positions
     *
     * @return <p>A copy of this gate's portal positions</p>
     */
    List<GatePosition> getPortalPositions();

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
     * Gets this gate's top-left location
     *
     * @return <p>This gate's top-left location</p>
     */
    Location getTopLeft();

    /**
     * Add a position specific for this Gate
     *
     * @param location <p> The location of the position </p>
     */
    void addPortalPosition(Location location);
    
    /**
     * Set a portal control mechanism. Note that a portal can only have one type of each control mechanism at the same time.
     * 
     * @param mechanism <p> The mechanism to set </p>
     * @param type <p> The function of the mechanism </p>
     */
    void setPortalControlMechanism(@NotNull ControlMechanism mechanism);

    /**
     * Get a portal control mechanism.
     * @param type <p> The type of control to get </p>
     * @return <p> A ControlMechanism or null if none has been set </p>
     */
    @Nullable ControlMechanism getPortalControlMechanism(@NotNull MechanismType type);

}
