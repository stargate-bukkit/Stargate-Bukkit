package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.utility.EconomyHandler;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.utility.MaterialHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

/**
 * The gate handler keeps track of all gates
 */
public class GateHandler {

    private static final Character ANYTHING = ' ';
    private static final Character ENTRANCE = '.';
    private static final Character EXIT = '*';
    private static final Character CONTROL_BLOCK = '-';

    private static Material defaultPortalBlockOpen = Material.NETHER_PORTAL;
    private static Material defaultPortalBlockClosed = Material.AIR;
    private static Material defaultButton = Material.STONE_BUTTON;

    private static final HashMap<String, Gate> gates = new HashMap<>();
    private static final HashMap<Material, List<Gate>> controlBlocks = new HashMap<>();
    private static final HashSet<Material> frameBlocks = new HashSet<>();

    private GateHandler() {

    }

    /**
     * Gets the character used for blocks that are not part of the gate
     *
     * @return <p>The character used for blocks that are not part of the gate</p>
     */
    public static Character getAnythingCharacter() {
        return ANYTHING;
    }

    /**
     * Gets the character used for defining the entrance
     *
     * @return <p>The character used for defining the entrance</p>
     */
    public static Character getEntranceCharacter() {
        return ENTRANCE;
    }

    /**
     * Gets the character used for defining the exit
     *
     * @return <p>The character used for defining the exit</p>
     */
    public static Character getExitCharacter() {
        return EXIT;
    }


    /**
     * Gets the character used for defining control blocks
     *
     * @return <p>The character used for defining control blocks</p>
     */
    public static Character getControlBlockCharacter() {
        return CONTROL_BLOCK;
    }

    /**
     * Register a gate into the list of available gates
     *
     * @param gate <p>The gate to register</p>
     */
    private static void registerGate(Gate gate) {
        gates.put(gate.getFilename(), gate);

        Material blockID = gate.getControlBlock();

        if (!controlBlocks.containsKey(blockID)) {
            controlBlocks.put(blockID, new ArrayList<>());
        }

        controlBlocks.get(blockID).add(gate);
    }

    /**
     * Loads a gate
     *
     * @param file <p>The file containing the gate's layout</p>
     * @return <p>The loaded gate or null if unable to load the gate</p>
     */
    private static Gate loadGate(File file) {
        try (Scanner scanner = new Scanner(file)) {
            return loadGate(file.getName(), file.getParent(), scanner);
        } catch (Exception ex) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + ex.getMessage());
            return null;
        }
    }

    /**
     * Loads a gate
     *
     * @param fileName <p>The name of the file containing the gate layout</p>
     * @param parentFolder <p>The parent folder of the layout file</p>
     * @param scanner <p>The scanner to use for reading the gate layout</p>
     * @return <p>The loaded gate or null if unable to load the gate</p>
     */
    private static Gate loadGate(String fileName, String parentFolder, Scanner scanner) {
        List<List<Character>> design = new ArrayList<>();
        Map<Character, Material> types = new HashMap<>();
        Map<String, String> config = new HashMap<>();
        Set<Material> frameTypes = new HashSet<>();

        //Initialize types map
        types.put(ENTRANCE, Material.AIR);
        types.put(EXIT, Material.AIR);
        types.put(ANYTHING, Material.AIR);

        //Read the file into appropriate lists and maps
        int cols = readGateFile(scanner, types, fileName, design, frameTypes, config);
        if (cols < 0) {
            return null;
        }
        Character[][] layout = generateLayoutMatrix(design, cols);

        //Create and validate the new gate
        Gate gate = createGate(config, fileName, layout, types);
        if (gate == null) {
            return null;
        }

        //Update list of all frame blocks
        frameBlocks.addAll(frameTypes);

        gate.save(parentFolder + "/"); // Updates format for version changes
        return gate;
    }

    /**
     * Creates a new gate
     *
     * @param config <p>The config map to get configuration values from</p>
     * @param fileName <p>The name of the saved gate config file</p>
     * @param layout <p>The layout matrix of the new gate</p>
     * @param types <p>The mapping for used gate material types</p>
     * @return <p>A new gate or null if the config is invalid</p>
     */
    private static Gate createGate(Map<String, String> config, String fileName, Character[][] layout,
                                   Map<Character, Material> types) {
        Material portalOpenBlock = readConfig(config, fileName, "portal-open", defaultPortalBlockOpen);
        Material portalClosedBlock = readConfig(config, fileName, "portal-closed", defaultPortalBlockClosed);
        Material portalButton = readConfig(config, fileName, "button", defaultButton);
        int useCost = readConfig(config, fileName, "usecost", -1);
        int createCost = readConfig(config, fileName, "createcost", -1);
        int destroyCost = readConfig(config, fileName, "destroycost", -1);
        boolean toOwner = (config.containsKey("toowner") ? Boolean.valueOf(config.get("toowner")) : EconomyHandler.toOwner);

        Gate gate = new Gate(fileName, new GateLayout(layout), types, portalOpenBlock, portalClosedBlock, portalButton, useCost,
                createCost, destroyCost, toOwner);

        if (!validateGate(gate, fileName)) {
            return null;
        }
        return gate;
    }

    /**
     * Validate that a gate is valid
     *
     * @param gate <p>The gate to validate</p>
     * @param fileName <p>The filename of the loaded gate file</p>
     * @return <p>True if the gate is valid. False otherwise</p>
     */
    private static boolean validateGate(Gate gate, String fileName) {
        if (gate.getLayout().getControls().length != 2) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName +
                    " - Gates must have exactly 2 control points.");
            return false;
        }

        if (!MaterialHelper.isButtonCompatible(gate.getPortalButton())) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName +
                    " - Gate button must be a type of button.");
            return false;
        }
        return true;
    }

    /**
     * Generates a matrix storing the gate layout
     *
     * @param design <p>The design of the gate layout</p>
     * @param cols <p>The largest amount of columns in the design</p>
     * @return <p>A matrix containing the gate's layout</p>
     */
    private static Character[][] generateLayoutMatrix(List<List<Character>> design, int cols) {
        Character[][] layout = new Character[design.size()][cols];
        for (int lineIndex = 0; lineIndex < design.size(); lineIndex++) {
            List<Character> row = design.get(lineIndex);
            Character[] result = new Character[cols];

            for (int rowIndex = 0; rowIndex < cols; rowIndex++) {
                if (rowIndex < row.size()) {
                    result[rowIndex] = row.get(rowIndex);
                } else {
                    //Add spaces to all lines which are too short
                    result[rowIndex] = ' ';
                }
            }

            layout[lineIndex] = result;
        }
        return layout;
    }

    /**
     * Reads the gate file
     *
     * @param scanner <p>The scanner to read from</p>
     * @param types <p>The map of characters to store valid symbols in</p>
     * @param fileName <p>The filename of the loaded gate config file</p>
     * @param design <p>The list to store the loaded design to</p>
     * @param frameTypes <p>The set of gate frame types to store to</p>
     * @param config <p>The map of config values to store to</p>
     * @return <p>The column count/width of the loaded gate</p>
     */
    private static int readGateFile(Scanner scanner, Map<Character, Material> types, String fileName,
                                    List<List<Character>> design, Set<Material> frameTypes, Map<String, String> config) {
        boolean designing = false;
        int cols = 0;
        try {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (designing) {
                    cols = readGateDesignLine(line, cols, types, fileName, design);
                    if (cols < 0) {
                        return -1;
                    }
                } else {
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        readGateConfigValue(line, types, frameTypes, config);
                    } else if ((line.isEmpty()) || (!line.contains("=") && !line.startsWith("#"))) {
                        designing = true;
                    }
                }
            }
        } catch (Exception ex) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName + " - " + ex.getMessage());
            return -1;
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return cols;
    }

    /**
     * Reads one design line of the gate layout file
     *
     * @param line <p>The line to read</p>
     * @param cols <p>The current max columns value of the design</p>
     * @param types <p>The map of characters to check for valid symbols</p>
     * @param fileName <p>The filename of the loaded gate config file</p>
     * @param design <p>The list to store the loaded design to</p>
     * @return <p>The new max columns value of the design</p>
     */
    private static int readGateDesignLine(String line, int cols, Map<Character, Material> types, String fileName,
                                          List<List<Character>> design) {
        List<Character> row = new ArrayList<>();

        if (line.length() > cols) {
            cols = line.length();
        }

        for (Character symbol : line.toCharArray()) {
            if ((symbol.equals('?')) || (!types.containsKey(symbol))) {
                Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName + " - Unknown symbol '" + symbol + "' in diagram");
                return -1;
            }
            row.add(symbol);
        }

        design.add(row);
        return cols;
    }

    /**
     * Reads one config value from the gate layout file
     *
     * @param line <p>The line to read</p>
     * @param types <p>The map of characters to materials to store to</p>
     * @param frameTypes <p>The set of gate frame types to store to</p>
     * @param config <p>The map of config values to store to</p>
     * @throws Exception <p>If an invalid material is encountered</p>
     */
    private static void readGateConfigValue(String line, Map<Character, Material> types, Set<Material> frameTypes,
                                            Map<String, String> config) throws Exception {
        String[] split = line.split("=");
        String key = split[0].trim();
        String value = split[1].trim();

        if (key.length() == 1) {
            Character symbol = key.charAt(0);
            Material id = Material.getMaterial(value);
            if (id == null) {
                throw new Exception("Invalid material in line: " + line);
            }
            types.put(symbol, id);
            frameTypes.add(id);
        } else {
            config.put(key, value);
        }
    }

    private static int readConfig(Map<String, String> config, String fileName, String key, int defaultInteger) {
        if (config.containsKey(key)) {
            try {
                return Integer.parseInt(config.get(key));
            } catch (NumberFormatException ex) {
                Stargate.log.log(Level.WARNING, String.format("%s reading %s: %s is not numeric", ex.getClass().getName(), fileName, key));
            }
        }

        return defaultInteger;
    }

    /**
     * Gets the material defined in the config
     *
     * @param config          <p>The config to read</p>
     * @param fileName        <p>The config file the config belongs to</p>
     * @param key             <p>The config key to read</p>
     * @param defaultMaterial <p>The default material to use, in case the config is invalid</p>
     * @return <p>The material to use</p>
     */
    private static Material readConfig(Map<String, String> config, String fileName, String key, Material defaultMaterial) {
        if (config.containsKey(key)) {
            Material material = Material.getMaterial(config.get(key));
            if (material != null) {
                return material;
            } else {
                Stargate.log.log(Level.WARNING, String.format("Error reading %s: %s is not a material", fileName, key));
            }
        }
        return defaultMaterial;
    }

    /**
     * Loads all gates inside the given folder
     *
     * @param gateFolder <p>The folder containing the gates</p>
     */
    public static void loadGates(String gateFolder) {
        File directory = new File(gateFolder);
        File[] files;

        if (directory.exists()) {
            files = directory.listFiles((file) -> file.isFile() && file.getName().endsWith(".gate"));
        } else {
            files = new File[0];
        }

        if (files == null || files.length == 0) {
            //The gates folder was not found. Assume this is the first run
            if (directory.mkdir()) {
                populateDefaults(gateFolder);
            }
        } else {
            for (File file : files) {
                Gate gate = loadGate(file);
                if (gate != null) {
                    registerGate(gate);
                }
            }
        }
    }

    /**
     * Writes the default gate specifications to the given folder
     *
     * @param gateFolder <p>The folder containing gate config files</p>
     */
    private static void populateDefaults(String gateFolder) {
        loadGateFromJar("nethergate.gate", gateFolder);
        loadGateFromJar("watergate.gate", gateFolder);
    }

    /**
     * Loads the given gate file from within the Jar's resources directory
     *
     * @param gateFile   <p>The name of the gate file</p>
     * @param gateFolder <p>The folder containing gates</p>
     */
    private static void loadGateFromJar(String gateFile, String gateFolder) {
        Scanner scanner = new Scanner(Gate.class.getResourceAsStream("/gates/" + gateFile));
        Gate gate = loadGate(gateFile, gateFolder, scanner);
        if (gate != null) {
            registerGate(gate);
        }
    }

    /**
     * Gets the gates with the given control block
     *
     * <p>The control block is the block type where the sign should be placed. It is used to decide whether a user
     * is creating a new portal.</p>
     *
     * @param block <p>The control block to check</p>
     * @return <p>A list of gates using the given control block</p>
     */
    public static Gate[] getGatesByControlBlock(Block block) {
        return getGatesByControlBlock(block.getType());
    }

    /**
     * Gets the gates with the given control block
     *
     * @param type <p>The type of the control block to check</p>
     * @return <p>A list of gates using the given material for control block</p>
     */
    public static Gate[] getGatesByControlBlock(Material type) {
        Gate[] result = new Gate[0];
        List<Gate> lookup = controlBlocks.get(type);

        if (lookup != null) {
            result = lookup.toArray(result);
        }

        return result;
    }

    /**
     * Gets a portal by its name (filename before .gate)
     *
     * @param name <p>The name of the gate to get</p>
     * @return <p>The gate with the given name</p>
     */
    public static Gate getGateByName(String name) {
        return gates.get(name);
    }

    /**
     * Gets the number of loaded gate configurations
     *
     * @return <p>The number of loaded gate configurations</p>
     */
    public static int getGateCount() {
        return gates.size();
    }

    /**
     * Checks whether the given material is used for the frame of any portals
     *
     * @param type <p>The material type to check</p>
     * @return <p>True if the material is used for the frame of at least one portal</p>
     */
    public static boolean isGateBlock(Material type) {
        return frameBlocks.contains(type);
    }

    /**
     * Clears all loaded gates
     */
    public static void clearGates() {
        gates.clear();
        controlBlocks.clear();
        frameBlocks.clear();
    }

}
