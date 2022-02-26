package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.exception.ParsingErrorException;
import net.TheDgtl.Stargate.gate.structure.GateControlBlock;
import net.TheDgtl.Stargate.gate.structure.GateFrame;
import net.TheDgtl.Stargate.gate.structure.GateIris;
import net.TheDgtl.Stargate.gate.structure.GateStructure;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.vectorlogic.IVectorOperation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

/**
 * A representation of a gate's format, including all its structures
 */
public class GateFormat implements GateFormatAPI {

    public static int formatAmount = 0;

    private final Set<Material> controlMaterials;
    private final Map<GateStructureType, GateStructure> portalParts;

    private final String name;
    public final boolean isIronDoorBlockable;

    /**
     * Instantiates a new gate format
     *
     * @param iris                <p>The format's iris structure</p>
     * @param frame               <p>The format's frame structure</p>
     * @param controlBlocks       <p>The format's control block structure</p>
     * @param name                <p>The name of the new gate format</p>
     * @param isIronDoorBlockable <p>Whether the gate format's iris can be blocked by a single iron door</p>
     */
    public GateFormat(GateIris iris, GateFrame frame, GateControlBlock controlBlocks, String name, boolean isIronDoorBlockable, Set<Material> controlMaterials) {
        portalParts = new EnumMap<>(GateStructureType.class);
        portalParts.put(GateStructureType.IRIS, iris);
        portalParts.put(GateStructureType.FRAME, frame);
        portalParts.put(GateStructureType.CONTROL_BLOCK, controlBlocks);
        this.name = name;
        this.isIronDoorBlockable = isIronDoorBlockable;
        GateFormat.formatAmount++;
        this.controlMaterials = controlMaterials;

        //TODO: Split this into GateFormat and GateFormatHandler
    }

    /**
     * Checks if the structure of a physical stargate matches this one
     *
     * @param converter <p></p>
     * @param topLeft   <p>The top-left location of the physical stargate to match</p>
     * @return <p>True if the stargate matches this format</p>
     */
    public boolean matches(IVectorOperation converter, Location topLeft) {
        for (GateStructureType structureType : portalParts.keySet()) {
            Stargate.log(Level.FINER, "---Validating " + structureType);
            if (!(portalParts.get(structureType).isValidState(converter, topLeft))) {
                Stargate.log(Level.FINER, structureType + " returned negative");
                return false;
            }
        }
        return true;
    }

    /**
     * Loads all gate formats from the gate folder
     *
     * @param dir <p>The folder to load gates from</p>
     * @return <p>A map between a control block material and the corresponding gate format</p>
     */
    public static List<GateFormat> loadGateFormats(File dir, StargateLogger logger) {
        List<GateFormat> gateFormatMap = new ArrayList<>();
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".gate")) : new File[0];

        if (files == null) {
            return null;
        }

        for (File file : files) {
            try {
                gateFormatMap.add(loadGateFormat(file, logger));
            } catch (FileNotFoundException | ParsingErrorException e) {
                Stargate.log(Level.WARNING, "Could not load Gate " + file.getName() + " - " + e.getMessage());
            }
        }
        return gateFormatMap;
    }

    /**
     * Loads the given gate format file
     *
     * @param file <p>The gate format file to load</p>
     * @throws ParsingErrorException <p>If unable to load the gate format</p>
     * @throws FileNotFoundException <p>If the gate file does not exist</p>
     */
    private static GateFormat loadGateFormat(File file, StargateLogger logger) throws ParsingErrorException, FileNotFoundException {
        Stargate.log(Level.CONFIG, "Loaded gate format " + file.getName());
        try (Scanner scanner = new Scanner(file)) {
            Stargate.log(Level.FINER, "Gate file size:" + file.length());
            if (file.length() > 65536L) {
                throw new ParsingErrorException("Design is too large");
            }

            GateFormatParser gateParser = new GateFormatParser(scanner, file.getName(), logger);
            return gateParser.parse();
        }
    }

    /**
     * Gets the locations of this gate format's control blocks
     *
     * @return <p>The locations of this gate format's control blocks</p>
     */
    public List<BlockVector> getControlBlocks() {
        GateControlBlock controlBlocks = (GateControlBlock) portalParts.get(GateStructureType.CONTROL_BLOCK);
        return controlBlocks.getStructureTypePositions();
    }

    @Override
    public Material getIrisMaterial(boolean getOpenMaterial) {
        return ((GateIris) portalParts.get(GateStructureType.IRIS)).getMaterial(getOpenMaterial);
    }

    /**
     * Gets this gate format's exit block
     *
     * @return <p>This gate format's exit block</p>
     */
    public BlockVector getExit() {
        return ((GateIris) portalParts.get(GateStructureType.IRIS)).getExit();
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
    public GateStructure getStructure(GateStructureType type) {
        return this.portalParts.get(type);
    }

}
