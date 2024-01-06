package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;
import org.sgrewritten.stargate.api.gate.structure.GateStructure;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;

import java.util.List;

/**
 * Represents a specific gate format
 */
public interface GateFormatAPI {
    /**
     * @return <p> The name of the file this format was loaded from </p>
     */
    String getFileName();

    /**
     * @return <p>The positions of the control blocks in format space</p>
     */
    List<BlockVector> getControlBlocks();

    /**
     * Gets the material used for this gate format's iris when in the given state
     *
     * @param getOpenMaterial <p>Whether to get the open-material or the closed-material</p>
     * @return <p>The material used for this gate format's iris</p>
     */
    Material getIrisMaterial(boolean getOpenMaterial);

    /**
     * Gets whether this gate format is iron door blockable
     *
     * <p>Iron door block-ability is defined as such: Can the Stargate's entrance be fully blocked by a single iron
     * door?</p>
     *
     * @return <p>True if iron door blockable</p>
     */
    boolean isIronDoorBlockable();

    /**
     * @return <p>The coordinates of the exit point for this gate format</p>
     */
    BlockVector getExit();

    /**
     * Get a structure based on the type of structure
     * @param gateFormatStructureType <p>A gate structure type</p>
     * @return <p>A gate structure</p>
     */
    GateStructure getStructure(GateFormatStructureType gateFormatStructureType);

    /**
     * Determines whether the format matches when provided a conversion between minecraft space and format space.
     * @param converter <p>Vector operation which converts between format space and minecraft space</p>
     * @param topLeft   <p>Origo in format space, top left of format in real space</p>
     * @return
     */
    boolean matches(VectorOperation converter, Location topLeft);

    /**
     * @return <p>the height of the format (Y axis)</p>
     */
    int getHeight();

    /**
     * @return <p>The width of the format (Z axis)</p>
     */
    int getWidth();
}
