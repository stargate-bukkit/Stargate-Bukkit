package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.exception.ParsingError;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * The gate format parser is responsible for parsing gate format files
 */
public class GateFormatParser {

    private final Scanner scanner;
    private final String filename;
    private String line;
    private final Map<Character, Set<Material>> frameMaterials;

    private final static char NOTHING = ' ';
    private final static char EXIT = '*';
    private final static char ENTRANCE = '.';
    private final static char CONTROL = '-';

    private final static String TAG_IDENTIFIER = "#";
    private final static String SPLIT_IDENTIFIER = ",";

    private boolean gateHasEntrance = false;
    private int amountOfControlBlocks = 0;

    private Set<Material> irisOpen;
    private Set<Material> irisClosed;
    private GateIris iris;
    private GateFrame frame;
    private GateControll control;
    private Set<Material> controlMaterials;

    private boolean canBeBlockedByIronDoor = false;

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

        //TODO: Split the parser into one reader and one parser to simplify the structure
    }

    /**
     * Gets the set of materials used for the parsed gate format's control blocks
     *
     * @return <p>The materials used for control blocks</p>
     */
    public Set<Material> getControlBlockMaterials() {
        return controlMaterials;
    }

    /**
     * Parses the gate file given during instantiation
     *
     * @return <p>The parsed gate format</p>
     * @throws ParsingError <p>If unable to parse the file</p>
     */
    public GateFormat parse() throws ParsingError {
        Map<String, String> config = parseSettings();
        Map<String, String> remainingConfig = setSettings(config);

        List<String> designLines = loadDesign();
        setDesign(designLines);
        checkIfCanBeBlockedByIronDoor();

        if (!gateHasEntrance) {
            throw new ParsingError("Design is missing an entrance ");
        }

        if (amountOfControlBlocks < 2) {
            throw new ParsingError("Design requires at least 2 control blocks '-' ");
        }

        return new GateFormat(iris, frame, control, remainingConfig, filename, canBeBlockedByIronDoor);
    }


    /**
     * Stores all configuration options to relevant variables/sets
     *
     * @param config <p>The configuration map to read</p>
     * @return <p>The remaining configuration options after the known options have been taken care of</p>
     * @throws ParsingError <p>If unable to parse one of the materials given in the options</p>
     */
    private Map<String, String> setSettings(Map<String, String> config) throws ParsingError {
        Map<String, String> remaining = new HashMap<>();
        for (String key : config.keySet()) {
            if (key.length() != 1) {
                switch (key) {
                    case "portal-open":
                        irisOpen = parseMaterial(config.get(key));
                        break;
                    case "portal-closed":
                        irisClosed = parseMaterial(config.get(key));
                        break;
                    default:
                        remaining.put(key, config.get(key));
                        break;
                }
                continue;
            }

            char symbol = key.charAt(0);

            Set<Material> id = parseMaterial(config.get(key));
            if (symbol == '-') {
                controlMaterials = id;
            }
            frameMaterials.put(symbol, id);
        }
        return remaining;
    }

    /**
     * Parses all configuration options into a key, value map
     *
     * @return <p>A key -> value map containing all defined config options</p>
     */
    private Map<String, String> parseSettings() {
        Map<String, String> config = new HashMap<>();
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.trim().isEmpty()) {
                continue;
            }
            if (!line.contains("=")) {
                break;
            }

            String[] split = line.split("=");
            String key = split[0].trim();
            String value = split[1].trim();
            config.put(key, value);

        }
        return config;
    }

    /**
     * Parses a material string
     *
     * <p>Just parses all the materials based from tags or material id (when a string starts with #,
     * it should be a tag). "," resembles a split in items</p>
     *
     * @param materialString <p>The string describing a material/material class to parse</p>
     * @return <p>The parsed material</p>
     * @throws ParsingError <p>If unable to parse the given material</p>
     */
    private Set<Material> parseMaterial(String materialString) throws ParsingError {
        Set<Material> foundIds = new HashSet<>();
        String[] individualIDs = materialString.split(SPLIT_IDENTIFIER);
        for (String stringId : individualIDs) {

            //Parse a tag
            if (stringId.startsWith(TAG_IDENTIFIER)) {
                parseMaterialTag(stringId, foundIds);
                continue;
            }

            //Parse a normal material
            Material id = Material.getMaterial(stringId);
            if (id == null) {
                throw new ParsingError("Invalid material ''" + stringId + "''");
            }
            foundIds.add(id);
        }
        if (foundIds.size() == 0) {
            throw new ParsingError("Invalid field''" + materialString + "'': Field must include atleast one block");
        }
        return foundIds;
    }

    /**
     * Parses a material tag
     *
     * @param stringId <p>A string denoting a tag</p>
     * @param foundIds <p>The set to store found material ids to</p>
     * @throws ParsingError <p>If unable to parse the tag</p>
     */
    private void parseMaterialTag(String stringId, Set<Material> foundIds) throws ParsingError {
        String tagString = stringId.replace(TAG_IDENTIFIER, "");
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS,
                NamespacedKey.minecraft(tagString.toLowerCase()), Material.class);
        if (tag == null) {
            throw new ParsingError("Invalid tag in line: " + line);
        }
        for (Material materialInTag : tag.getValues()) {
            if (materialInTag.isBlock()) {
                foundIds.add(materialInTag);
            }
        }
    }


    /**
     * Gets the gate design as a list of strings, where each string corresponds to one line in the design
     *
     * @return <p>The read gate design</p>
     */
    private List<String> loadDesign() {
        List<String> designLines = new ArrayList<>();
        /*
         * The initial line of the gateDesign has been loaded by parseSettings (acting as an endpoint of settings).
         * The 1 following line of code takes this into consideration.
         */
        designLines.add(line);
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            designLines.add(line);
        }
        return designLines;
    }

    /**
     * <p>Creates a vector-structure from the character design, following this reference system:
     * FFF    y
     * C.C    ^
     * F*F    |
     * FFF    ---->z
     * <p>
     * where F,C,.,* resembles a gate design and the rest is the coordinate system used by the vectors.
     * Note that origin is at the top-left corner of the gate design.
     * <p>
     * Vectors are also divided into 3 different structures:
     * - GateIris
     * - GateFrame
     * - GateControl
     * Note that some structures need a selected material</p>
     *
     * @param lines <p>The lines in the .gate file with everything about positions (the actual design)</p>
     * @throws ParsingError <p>If encountering an unknown character</p>
     */
    private void setDesign(List<String> lines) throws ParsingError {
        iris = new GateIris(irisOpen, irisClosed);
        frame = new GateFrame();
        control = new GateControll();
        int lineNumber, i;
        for (lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            char[] charLine = lines.get(lineNumber).toCharArray();
            for (i = 0; i < charLine.length; i++) {
                BlockVector selectedLocation = new BlockVector(0, -lineNumber, i);
                setDesignPoint(charLine[i], selectedLocation.clone());
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
        List<BlockVector> irisBlocks = iris.getPartsPos();
        if (irisBlocks.size() == 2) {
            for (BlockVector block : irisBlocks) {
                BlockVector above = block.clone();
                above.setY(block.getBlockY() + 1);
                if (irisBlocks.contains(above)) {
                    canBeBlockedByIronDoor = true;
                    break;
                }
            }
        }
    }

    /**
     * Determines how one character point in the design should be added into the gateStructure
     *
     * @param key              <p>The character key to take care of</p>
     * @param selectedLocation <p>The vector location of the character's position in the design</p>
     * @throws ParsingError <p>If the character cannot be understood</p>
     */
    private void setDesignPoint(char key, BlockVector selectedLocation) throws ParsingError {
        switch (key) {
            case NOTHING:
                break;
            case EXIT:
                iris.addExit(selectedLocation.clone());
                break;
            case ENTRANCE:
                iris.addPart(selectedLocation.clone());
                gateHasEntrance = true;
                break;
            case CONTROL:
                amountOfControlBlocks++;
                frame.addPart(selectedLocation.clone(), frameMaterials.get(key));
                BlockVector controlLocation = selectedLocation.clone();
                controlLocation.add(new BlockVector(1, 0, 0));
                control.addPart(controlLocation);
                break;
            default:
                if ((key == '?') || (!frameMaterials.containsKey(key))) {
                    throw new ParsingError("Unknown symbol '" + key + "' in gatedesign");
                }
                frame.addPart(selectedLocation.clone(), frameMaterials.get(key));
        }
    }

}
