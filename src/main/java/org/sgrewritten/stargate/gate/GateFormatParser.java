package org.sgrewritten.stargate.gate;

import org.bukkit.Material;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.exception.ParsingErrorException;
import org.sgrewritten.stargate.gate.structure.GateControlBlock;
import org.sgrewritten.stargate.gate.structure.GateFrame;
import org.sgrewritten.stargate.gate.structure.GateIris;
import org.sgrewritten.stargate.util.GateFormatReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

/**
 * The gate format parser is responsible for parsing gate format files
 */
public class GateFormatParser {

    private final Scanner scanner;
    private final String filename;
    private final Map<Character, Set<Material>> frameMaterials;

    private static final char NOTHING = ' ';
    private static final char EXIT = '*';
    private static final char ENTRANCE = '.';
    private static final char CONTROL = '-';
    private boolean gateHasExit = false;
    private int amountOfControlBlocks = 0;
    private boolean canBeBlockedByIronDoor = false;

    private Set<Material> irisOpen;
    private Set<Material> irisClosed;
    private GateIris iris;
    private GateFrame frame;
    private GateControlBlock controlBlocks;
    private Set<Material> controlMaterials;

    /**
     * Instantiates a new gate format parser
     *
     * @param scanner  <p>The scanner to read the gate file from</p>
     * @param filename <p>The name of the parsed gate file</p>
     */
    public GateFormatParser(Scanner scanner, String filename) {
        frameMaterials = new HashMap<>();
        // Set default materials in case any config keys are missing
        irisOpen = new HashSet<>();
        irisOpen.add(Material.WATER);
        irisClosed = new HashSet<>();
        irisClosed.add(Material.AIR);
        this.scanner = scanner;
        this.filename = filename;
    }

    /**
     * Parses the gate file given during instantiation
     *
     * @return <p>The parsed gate format</p>
     * @throws ParsingErrorException <p>If unable to parse the file</p>
     */
    public GateFormat parseGateFormat() throws ParsingErrorException {
        Map<Character, Set<Material>> characterMaterialMap = new HashMap<>();
        List<List<Character>> design = new ArrayList<>();
        Map<String, String> config = new HashMap<>();
        GateFormatReader.readGateFile(scanner, characterMaterialMap, design, config);

        loadGateConfigValues(config);
        iris = new GateIris(irisOpen, irisClosed);
        frame = new GateFrame();
        controlBlocks = new GateControlBlock();

        loadFrameAndControlMaterials(characterMaterialMap);
        loadDesignLocations(design);
        checkIfCanBeBlockedByIronDoor();

        if (!gateHasExit) {
            throw new ParsingErrorException("Design is missing an exit ");
        }

        if (amountOfControlBlocks < 2) {
            throw new ParsingErrorException("Design requires at least 2 control blocks '-' ");
        }

        return new GateFormat(iris, frame, controlBlocks, filename, canBeBlockedByIronDoor, controlMaterials);
    }

    /**
     * Loads the frame and control materials defined in the gate format file
     *
     * @param characterMaterialMap <p>The full map between characters and materials loaded from the gate file</p>
     */
    private void loadFrameAndControlMaterials(Map<Character, Set<Material>> characterMaterialMap) {
        for (Character character : characterMaterialMap.keySet()) {
            if (character == CONTROL) {
                controlMaterials = characterMaterialMap.get(CONTROL);
                frameMaterials.put(character, characterMaterialMap.get(character));
            } else if (character != NOTHING && character != EXIT && character != ENTRANCE) {
                frameMaterials.put(character, characterMaterialMap.get(character));
            }
        }
    }


    /**
     * Stores all configuration options to relevant variables/sets
     *
     * @param config <p>The configuration map to read</p>
     * @throws ParsingErrorException <p>If unable to parse one of the materials given in the options</p>
     */
    private void loadGateConfigValues(Map<String, String> config) throws ParsingErrorException {
        for (String key : config.keySet()) {
            String line = key + "=" + config.get(key);
            switch (key) {
                case "portal-open" -> irisOpen = GateFormatReader.parseMaterial(config.get(key), line);
                case "portal-closed" -> irisClosed = GateFormatReader.parseMaterial(config.get(key), line);
                default -> {
                    //Any unknown config values are ignored
                }
            }

        }
    }

    /**
     * <p>Creates a vector-structure from the character design, following this reference system:
     * FFF         y
     * C.C         ^
     * F*F         |
     * FFF    z<---x
     * <p>
     * where F,C,.,* resembles a gate design and the rest is the coordinate system used by the vectors.
     * Note that origin is in the top-left corner of the gate design.
     * <p>
     * Vectors are also divided into 3 different structures:
     * - GateIris
     * - GateFrame
     * - GateControl
     * Note that some structures need a selected material</p>
     *
     * @param design <p>The design in the .gate file</p>
     * @throws ParsingErrorException <p>If encountering an unknown character</p>
     */
    private void loadDesignLocations(List<List<Character>> design) throws ParsingErrorException {
        for (int rowIndex = 0; rowIndex < design.size(); rowIndex++) {
            List<Character> row = design.get(rowIndex);
            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
                parseDesignCharacter(row.get(columnIndex), new BlockVector(0, -rowIndex, -columnIndex));
                Stargate.log(Level.FINEST, "Loading design location: C:" + row.get(columnIndex) + " V: " +
                        0 + "," + -rowIndex + "," + -columnIndex);
            }
        }
    }

    /**
     * Checks and stores whether this gate format can be blocked by a single iron door
     *
     * <p>Check if iris only contains 2 blocks, where one is above the other (Only viable format that can be blocked
     * by iron door)</p>
     */
    private void checkIfCanBeBlockedByIronDoor() {
        List<BlockVector> irisBlocks = iris.getStructureTypePositions();
        if (irisBlocks.size() != 2) {
            return;
        }
        for (BlockVector block : irisBlocks) {
            BlockVector above = block.clone();
            above.setY(block.getBlockY() + 1);
            if (irisBlocks.contains(above)) {
                canBeBlockedByIronDoor = true;
                break;
            }
        }
    }

    /**
     * Determines how one character point in the design should be added into the gateStructure
     *
     * @param key              <p>The character key to take care of</p>
     * @param selectedLocation <p>The vector location of the character's position in the design</p>
     * @throws ParsingErrorException <p>If the character cannot be understood</p>
     */
    private void parseDesignCharacter(char key, BlockVector selectedLocation) throws ParsingErrorException {
        switch (key) {
            case NOTHING:
                break;
            case EXIT:
                iris.addExit(selectedLocation.clone());
                gateHasExit = true;
                break;
            case ENTRANCE:
                iris.addPart(selectedLocation.clone());
                break;
            case CONTROL:
                amountOfControlBlocks++;
                frame.addPart(selectedLocation.clone(), frameMaterials.get(key));
                BlockVector controlLocation = selectedLocation.clone();
                controlLocation.add(new BlockVector(1, 0, 0));
                controlBlocks.addPart(controlLocation);
                break;
            default:
                if (key == '?' || !frameMaterials.containsKey(key)) {
                    throw new ParsingErrorException("Unknown symbol '" + key + "' in gate design");
                }
                frame.addPart(selectedLocation.clone(), frameMaterials.get(key));
        }
    }

}
