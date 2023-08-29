package org.sgrewritten.stargate.gate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;
import org.sgrewritten.stargate.gate.structure.GateControlBlock;
import org.sgrewritten.stargate.gate.structure.GateFrame;
import org.sgrewritten.stargate.gate.structure.GateIris;
import org.sgrewritten.stargate.gate.structure.GateStructure;
import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;
import org.sgrewritten.stargate.vectorlogic.VectorOperation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * A representation of a gate's format, including all its structures
 */
public class GateFormat implements GateFormatAPI {

    private final Set<Material> controlMaterials;
    private final Map<GateFormatStructureType, GateStructure> portalParts;
    private final String name;
    private final boolean isIronDoorBlockable;
    private final int height;
    private final int width;

    /**
     * Instantiates a new gate format
     *
     * @param iris                <p>The format's iris structure</p>
     * @param frame               <p>The format's frame structure</p>
     * @param controlBlocks       <p>The format's control block structure</p>
     * @param name                <p>The name of the new gate format</p>
     * @param isIronDoorBlockable <p>Whether the gate format's iris can be blocked by a single iron door</p>
     * @param controlMaterials    <b>The materials to use for this gatres control blocks</b>
     * @param height              <p>The height of this gate format</p>
     * @param width               <p>The width of this gate format</p>
     */
    public GateFormat(GateIris iris, GateFrame frame, GateControlBlock controlBlocks, String name,
                      boolean isIronDoorBlockable, Set<Material> controlMaterials, int height, int width) {
        portalParts = new EnumMap<>(GateFormatStructureType.class);
        portalParts.put(GateFormatStructureType.IRIS, iris);
        portalParts.put(GateFormatStructureType.FRAME, frame);
        portalParts.put(GateFormatStructureType.CONTROL_BLOCK, controlBlocks);
        this.name = name;
        this.isIronDoorBlockable = isIronDoorBlockable;
        this.controlMaterials = controlMaterials;
        this.height = height;
        this.width = width;
    }

    /**
     * Gets the height of this gate format
     *
     * @return <p>The height of this gate format</p>
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the width of this gate format
     *
     * @return <p>The width of this gate format</p>
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets whether this gate format is iron door blockable
     *
     * <p>Iron door block-ability is defined as such: Can the Stargate's entrance be fully blocked by a single iron
     * door?</p>
     *
     * @return <p>True if iron door blockable</p>
     */
    public boolean isIronDoorBlockable() {
        return isIronDoorBlockable;
    }

    /**
     * Checks if the structure of a physical stargate matches this one
     *
     * @param converter <p></p>
     * @param topLeft   <p>The top-left location of the physical stargate to match</p>
     * @return <p>True if the stargate matches this format</p>
     */
    public boolean matches(VectorOperation converter, Location topLeft) {
        for (GateFormatStructureType structureType : portalParts.keySet()) {
            Stargate.log(Level.FINER, "---Validating " + structureType);
            if (!(portalParts.get(structureType).isValidState(converter, topLeft))) {
                Stargate.log(Level.FINER, structureType + " returned negative");
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the locations of this gate format's control blocks
     *
     * @return <p>The locations of this gate format's control blocks</p>
     */
    @Override
    public List<BlockVector> getControlBlocks() {
        GateControlBlock controlBlocks = (GateControlBlock) portalParts.get(GateFormatStructureType.CONTROL_BLOCK);
        return controlBlocks.getStructureTypePositions();
    }

    @Override
    public Material getIrisMaterial(boolean getOpenMaterial) {
        return ((GateIris) portalParts.get(GateFormatStructureType.IRIS)).getMaterial(getOpenMaterial);
    }

    /**
     * Gets this gate format's exit block
     *
     * @return <p>This gate format's exit block</p>
     */
    public BlockVector getExit() {
        return ((GateIris) portalParts.get(GateFormatStructureType.IRIS)).getExit();
    }

    @Override
    public String getFileName() {
        return name;
    }

    /**
     * @return <p>The set of materials that this format can have a control on</p>
     */
    public Set<Material> getControlMaterials() {
        return controlMaterials;
    }

    /**
     * Get the {@link GateStructure} of the specified type
     *
     * @param type <p>The specified type of {@link GateStructure}</p>
     * @return <p>The {@link GateStructure} of the specified type</p>
     */
    public GateStructure getStructure(GateFormatStructureType type) {
        return this.portalParts.get(type);
    }

}
