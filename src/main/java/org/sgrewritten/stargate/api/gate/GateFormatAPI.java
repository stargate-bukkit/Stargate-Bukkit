package org.sgrewritten.stargate.api.gate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;
import org.sgrewritten.stargate.api.gate.structure.GateStructure;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;

import java.util.List;

public interface GateFormatAPI {
    /**
     * @return <p> The name of the file this format was loaded from </p>
     */
    String getFileName();

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

    BlockVector getExit();

    GateStructure getStructure(GateFormatStructureType gateFormatEquivalent);

    boolean matches(VectorOperation converter, Location topLeft);

    int getHeight();

    int getWidth();
}
