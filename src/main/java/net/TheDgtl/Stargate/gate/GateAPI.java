package net.TheDgtl.Stargate.gate;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.PortalPosition;
import net.TheDgtl.Stargate.network.portal.PositionType;

public interface GateAPI {
    /**
     * Set button and draw sign
     *
     * @param signLines an array with 4 elements, representing each line of a sign
     */
    public void drawControlMechanisms(String[] signLines, boolean drawButton);
    
    /**
     * Gets a copy of this gate's portal positions
     *
     * @return <p>A copy of this gate's portal positions</p>
     */
    public List<PortalPosition> getPortalPositions();
    
    /**
     * Gets all locations of this gate containing the given structure type
     *
     * @param structureType <p>The structure type to get locations of</p>
     * @return <p>All locations containing the given structure type</p>
     */
    public List<BlockLocation> getLocations(GateStructureType structureType);
    
    /**
     * Opens this gate
     */
    public void open();
    
    /**
     * Closes this gate
     */
    public void close();
    
    /**
     * Gets the exit location of this gate
     *
     * @return <p>The exit location of this gate</p>
     */
    public Location getExit();
    
    /**
     * Gets whether this gate is currently open
     *
     * @return <p>Whether this gate is currently open</p>
     */
    public boolean isOpen();
    
    /**
     * Gets the gate format used by this gate
     *
     * @return <p>The gate format used by this gate</p>
     */
    public GateFormatAPI getFormat();
    
    /**
     * Gets the block face defining this gate's direction
     *
     * @return <p>The block face defining this gate's direction</p>
     */
    public BlockFace getFacing();
    
    /**
     * Gets a vector relative to this gate's top-left location using the given location
     *
     * @param location <p>The location to turn into a relative location</p>
     * @return <p>A location relative to this gate's top-left location</p>
     */
    public Vector getRelativeVector(Location location);
    
    /**
     * Gets whether this gate has been flipped on the z-axis
     *
     * @return <p>Whether this gate has been flipped on the z-axis</p>
     */
    public boolean getFlipZ();
    
    /**
     * Gets a location from a relative vector
     *
     * @param vector <p>The vector defining a location</p>
     * @return <p>The location corresponding to the given vector</p>
     */
    public Location getLocation(@NotNull Vector vector);
    
    /**
     * Gets this gate's top-left location
     *
     * @return <p>This gate's top-left location</p>
     */
    public Location getTopLeft();
    
    /**
     * Add a position specific for this Gate
     * 
     * @param location <p> The location of the position </p>
     * @param type     <p> The type of position </p>
     */
    public void addPortalPosition(Location location, PositionType type);
}
