package net.knarcraft.stargate.portal.property.gate;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.utility.GateReader;
import net.knarcraft.stargate.utility.MaterialHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static net.knarcraft.stargate.utility.GateReader.generateLayoutMatrix;
import static net.knarcraft.stargate.utility.GateReader.readGateConfig;
import static net.knarcraft.stargate.utility.GateReader.readGateFile;

/**
 * The gate handler keeps track of all gates
 */
public class GateHandler {

    private static final Character ANYTHING = ' ';
    private static final Character ENTRANCE = '.';
    private static final Character EXIT = '*';
    private static final Character CONTROL_BLOCK = '-';

    private static final Material defaultPortalBlockOpen = Material.NETHER_PORTAL;
    private static final Material defaultPortalBlockClosed = Material.AIR;
    private static final Material defaultButton = Material.STONE_BUTTON;

    private static final HashMap<String, Gate> gates = new HashMap<>();
    private static final HashMap<Material, List<Gate>> controlBlocks = new HashMap<>();

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
     * Loads a gate from a file
     *
     * @param file <p>The file containing the gate data</p>
     * @return <p>The loaded gate, or null if unable to load the gate</p>
     */
    private static Gate loadGate(File file) {
        try (Scanner scanner = new Scanner(file)) {
            return loadGate(file.getName(), file.getParent(), scanner);
        } catch (Exception exception) {
            Stargate.logSevere(String.format("Could not load Gate %s - %s", file.getName(), exception.getMessage()));
            return null;
        }
    }

    /**
     * Loads a gate from a file
     *
     * @param fileName     <p>The name of the file containing the gate data</p>
     * @param parentFolder <p>The parent folder of the gate data file</p>
     * @param scanner      <p>The scanner to use for reading the gate data</p>
     * @return <p>The loaded gate or null if unable to load the gate</p>
     */
    private static Gate loadGate(String fileName, String parentFolder, Scanner scanner) {
        List<List<Character>> design = new ArrayList<>();
        Map<Character, Material> characterMaterialMap = new HashMap<>();
        Map<String, String> config = new HashMap<>();
        Set<Material> frameTypes = new HashSet<>();

        //Initialize character to material map
        characterMaterialMap.put(ENTRANCE, Material.AIR);
        characterMaterialMap.put(EXIT, Material.AIR);
        characterMaterialMap.put(ANYTHING, Material.AIR);

        //Read the file into appropriate lists and maps
        int columns = readGateFile(scanner, characterMaterialMap, fileName, design, frameTypes, config);
        if (columns < 0) {
            return null;
        }
        Character[][] layout = generateLayoutMatrix(design, columns);

        //Create and validate the new gate
        Gate gate = createGate(config, fileName, layout, characterMaterialMap);
        if (gate == null) {
            return null;
        }

        //Update gate file in case the format has changed between versions
        gate.save(parentFolder + "/");
        return gate;
    }

    /**
     * Creates a new gate
     *
     * @param config               <p>The config map to get configuration values from</p>
     * @param fileName             <p>The name of the saved gate config file</p>
     * @param layout               <p>The layout matrix of the new gate</p>
     * @param characterMaterialMap <p>A map between layout characters and the material to use</p>
     * @return <p>A new gate, or null if the config is invalid</p>
     */
    private static Gate createGate(Map<String, String> config, String fileName, Character[][] layout,
                                   Map<Character, Material> characterMaterialMap) {
        //Read relevant material types
        Material portalOpenBlock = readGateConfig(config, fileName, "portal-open", defaultPortalBlockOpen);
        Material portalClosedBlock = readGateConfig(config, fileName, "portal-closed", defaultPortalBlockClosed);
        Material portalButton = readGateConfig(config, fileName, "button", defaultButton);

        //Read economy values
        int useCost = GateReader.readGateConfig(config, fileName, "usecost");
        int createCost = GateReader.readGateConfig(config, fileName, "createcost");
        int destroyCost = GateReader.readGateConfig(config, fileName, "destroycost");
        boolean toOwner = (config.containsKey("toowner") ? Boolean.parseBoolean(config.get("toowner")) :
                Stargate.getEconomyConfig().sendPaymentToOwner());

        //Create the new gate
        Gate gate = new Gate(fileName, new GateLayout(layout), characterMaterialMap, portalOpenBlock, portalClosedBlock,
                portalButton, useCost, createCost, destroyCost, toOwner);

        if (!validateGate(gate, fileName)) {
            return null;
        }
        return gate;
    }

    /**
     * Validates that a gate is valid
     *
     * @param gate     <p>The gate to validate</p>
     * @param fileName <p>The filename of the loaded gate file</p>
     * @return <p>True if the gate is valid. False otherwise</p>
     */
    private static boolean validateGate(Gate gate, String fileName) {
        if (gate.getLayout().getControls().length != 2) {
            Stargate.logSevere(String.format("Could not load Gate %s - Gates must have exactly 2 control points.",
                    fileName));
            return false;
        }

        if (!MaterialHelper.isButtonCompatible(gate.getPortalButton())) {
            Stargate.logSevere(String.format("Could not load Gate %s - Gate button must be a type of button.", fileName));
            return false;
        }
        return true;
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
            //Get all files with a .gate extension
            files = directory.listFiles((file) -> file.isFile() && file.getName().endsWith(".gate"));
        } else {
            //Set files to empty list to signal that default gates need to be copied
            files = new File[0];
        }

        if (files == null || files.length == 0) {
            //The gates-folder was not found. Assume this is the first run
            if (directory.mkdir()) {
                writeDefaultGatesToFolder(gateFolder);
            }
        } else {
            //Load and register the corresponding gate for each file
            for (File file : files) {
                Gate gate = loadGate(file);
                if (gate != null) {
                    registerGate(gate);
                }
            }
        }
    }

    /**
     * Writes the default gates to the given folder
     *
     * @param gateFolder <p>The folder containing gate config files</p>
     */
    public static void writeDefaultGatesToFolder(String gateFolder) {
        loadGateFromJar("nethergate.gate", gateFolder);
        loadGateFromJar("watergate.gate", gateFolder);
        loadGateFromJar("endgate.gate", gateFolder);
    }

    /**
     * Loads the given gate file from within the Jar's resources directory
     *
     * @param gateFile   <p>The name of the gate file</p>
     * @param gateFolder <p>The folder containing gates</p>
     */
    private static void loadGateFromJar(String gateFile, String gateFolder) {
        //Get an input stream for the internal file
        InputStream gateFileStream = Gate.class.getResourceAsStream("/gates/" + gateFile);
        if (gateFileStream != null) {
            Scanner scanner = new Scanner(gateFileStream);
            //Load and register the gate
            Gate gate = loadGate(gateFile, gateFolder, scanner);
            if (gate != null) {
                registerGate(gate);
            }
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
     * <p>The control block is the block type where the sign should be placed. It is used to decide whether a user
     * is creating a new portal.</p>
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
     * Gets a portal given its filename
     *
     * @param fileName <p>The filename of the gate to get</p>
     * @return <p>The gate with the given filename</p>
     */
    public static Gate getGateByName(String fileName) {
        return gates.get(fileName);
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
     * Clears all loaded gates and control blocks
     */
    public static void clearGates() {
        gates.clear();
        controlBlocks.clear();
    }

}
