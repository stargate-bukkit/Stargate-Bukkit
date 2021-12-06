package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.ParsingErrorException;
import net.TheDgtl.Stargate.gate.structure.GateControlBlock;
import net.TheDgtl.Stargate.gate.structure.GateFrame;
import net.TheDgtl.Stargate.gate.structure.GateIris;
import net.TheDgtl.Stargate.gate.structure.GateStructure;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.vectorlogic.VectorOperation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

/**
 * A representation of a gate's format, including all its structures
 */
public class GateFormat {

    public static Map<Material, List<GateFormat>> controlMaterialFormatsMap;
    public final Map<GateStructureType, GateStructure> portalParts;

    public final String name;
    public final boolean isIronDoorBlockable;
    public static int formatAmount = 0;

    /**
     * Instantiates a new gate format
     *
     * @param iris                <p>The format's iris structure</p>
     * @param frame               <p>The format's frame structure</p>
     * @param controlBlocks       <p>The format's control block structure</p>
     * @param name                <p>The name of the new gate format</p>
     * @param isIronDoorBlockable <p>Whether the gate format's iris can be blocked by a single iron door</p>
     */
    public GateFormat(GateIris iris, GateFrame frame, GateControlBlock controlBlocks, String name, boolean isIronDoorBlockable) {
        portalParts = new EnumMap<>(GateStructureType.class);
        portalParts.put(GateStructureType.IRIS, iris);
        portalParts.put(GateStructureType.FRAME, frame);
        portalParts.put(GateStructureType.CONTROL_BLOCK, controlBlocks);
        this.name = name;
        this.isIronDoorBlockable = isIronDoorBlockable;
        GateFormat.formatAmount++;
    }

    /**
     * Checks if the structure of a physical stargate matches this one
     *
     * @param converter <p></p>
     * @param topLeft   <p>The top-left location of the physical stargate to match</p>
     * @return <p>True if the stargate matches this format</p>
     */
    public boolean matches(VectorOperation converter, Location topLeft) {
        for (GateStructureType structureType : portalParts.keySet()) {
            Stargate.log(Level.FINER, "---Validating " + structureType);
            if (!(portalParts.get(structureType).isValidState(converter, topLeft))) {
                Stargate.log(Level.INFO, structureType + " returned negative");
                return false;
            }
        }
        return true;
    }

    /**
     * Loads all gate formats from the gate folder
     *
     * @param gateFolder <p>The folder to load gates from</p>
     * @return <p>A map between a control block material and the corresponding gate format</p>
     */
    public static Map<Material, List<GateFormat>> loadGateFormats(String gateFolder) {
        Map<Material, List<GateFormat>> controlToGateMap = new HashMap<>();
        File dir = new File(gateFolder);
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".gate")) : new File[0];

        if (files == null) {
            return null;
        }

        for (File file : files) {
            loadGateFormat(file, controlToGateMap);
        }
        return controlToGateMap;
    }

    /**
     * Loads the given gate format file
     *
     * @param file             <p>The gate format file to load</p>
     * @param controlToGateMap <p>The mapping between control blocks and gate formats to save to</p>
     */
    private static void loadGateFormat(File file, Map<Material, List<GateFormat>> controlToGateMap) {
        Stargate.log(Level.FINE, "Reading gateFormat from " + file.getName());
        try (Scanner scanner = new Scanner(file)) {
            Stargate.log(Level.FINEST, "fileSpace:" + file.length());
            if (file.length() > 65536L) {
                throw new ParsingErrorException("Design is too large");
            }

            GateFormatParser gateParser = new GateFormatParser(scanner, file.getName());
            GateFormat format = gateParser.parse();
            addGateFormat(controlToGateMap, format, gateParser.getControlBlockMaterials());
        } catch (FileNotFoundException | ParsingErrorException e) {
            Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + e.getMessage());
        }
    }

    /**
     * Adds a new gate format
     *
     * @param controlToGateMap <p>The map of registered control block material to gate format mapping</p>
     * @param format           <p>The gate format to register</p>
     * @param controlMaterials <p>The allowed control block materials for the new gate format</p>
     */
    private static void addGateFormat(Map<Material, List<GateFormat>> controlToGateMap, GateFormat format,
                                      Set<Material> controlMaterials) {
        for (Material controlMaterial : controlMaterials) {
            //Add an empty list if the material has no entry
            if (!(controlToGateMap.containsKey(controlMaterial))) {
                List<GateFormat> gateFormatList = new ArrayList<>();
                controlToGateMap.put(controlMaterial, gateFormatList);
            }
            controlToGateMap.get(controlMaterial).add(format);
        }
    }

    /**
     * Gets all gate format using the given control block material
     *
     * @param signParentBlockMaterial <p>The material of a placed sign's parent block</p>
     * @return <p>All gate formats using the given control block</p>
     */
    public static List<GateFormat> getPossibleGateFormatsFromControlBlockMaterial(Material signParentBlockMaterial) {
        List<GateFormat> possibleGates = controlMaterialFormatsMap.get(signParentBlockMaterial);
        if (possibleGates == null) {
            return new ArrayList<>();
        }
        return possibleGates;

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

    /**
     * Gets the material used for this gate format's iris when in the given state
     *
     * @param getOpenMaterial <p>Whether to get the open-material or the closed-material</p>
     * @return <p>The material used for this gate format's iris</p>
     */
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

}
