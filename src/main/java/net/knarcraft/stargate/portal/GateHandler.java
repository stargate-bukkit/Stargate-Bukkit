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
import java.util.Scanner;
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

    public static void registerGate(Gate gate) {
        gates.put(gate.getFilename(), gate);

        Material blockID = gate.getControlBlock();

        if (!controlBlocks.containsKey(blockID)) {
            controlBlocks.put(blockID, new ArrayList<>());
        }

        controlBlocks.get(blockID).add(gate);
    }

    public static Gate loadGate(File file) {
        try (Scanner scanner = new Scanner(file)) {
            return loadGate(file.getName(), file.getParent(), scanner);
        } catch (Exception ex) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + ex.getMessage());
            return null;
        }
    }

    public static Gate loadGate(String fileName, String parentFolder, Scanner scanner) {
        boolean designing = false;
        List<List<Character>> design = new ArrayList<>();
        HashMap<Character, Material> types = new HashMap<>();
        HashMap<String, String> config = new HashMap<>();
        HashSet<Material> frameTypes = new HashSet<>();
        int cols = 0;

        // Init types map
        types.put(ENTRANCE, Material.AIR);
        types.put(EXIT, Material.AIR);
        types.put(ANYTHING, Material.AIR);

        try {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (designing) {
                    List<Character> row = new ArrayList<>();

                    if (line.length() > cols) {
                        cols = line.length();
                    }

                    for (Character symbol : line.toCharArray()) {
                        if ((symbol.equals('?')) || (!types.containsKey(symbol))) {
                            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName + " - Unknown symbol '" + symbol + "' in diagram");
                            return null;
                        }
                        row.add(symbol);
                    }

                    design.add(row);
                } else {
                    if (!line.isEmpty() && !line.startsWith("#")) {
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
                    } else if ((line.isEmpty()) || (!line.contains("=") && !line.startsWith("#"))) {
                        designing = true;
                    }
                }
            }
        } catch (Exception ex) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName + " - " + ex.getMessage());
            return null;
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        Character[][] layout = new Character[design.size()][cols];

        //y = relative line number of layout file
        for (int y = 0; y < design.size(); y++) {
            List<Character> row = design.get(y);
            Character[] result = new Character[cols];

            for (int x = 0; x < cols; x++) {
                if (x < row.size()) {
                    result[x] = row.get(x);
                } else {
                    result[x] = ' ';
                }
            }

            layout[y] = result;
        }

        Material portalOpenBlock = readConfig(config, fileName, "portal-open", defaultPortalBlockOpen);
        Material portalClosedBlock = readConfig(config, fileName, "portal-closed", defaultPortalBlockClosed);
        Material portalButton = readConfig(config, fileName, "button", defaultButton);
        int useCost = readConfig(config, fileName, "usecost", -1);
        int createCost = readConfig(config, fileName, "createcost", -1);
        int destroyCost = readConfig(config, fileName, "destroycost", -1);
        boolean toOwner = (config.containsKey("toowner") ? Boolean.valueOf(config.get("toowner")) : EconomyHandler.toOwner);

        Gate gate = new Gate(fileName, new GateLayout(layout), types, portalOpenBlock, portalClosedBlock, portalButton, useCost,
                createCost, destroyCost, toOwner);



        if (gate.getLayout().getControls().length != 2) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName + " - Gates must have exactly 2 control points.");
            return null;
        }

        if (!MaterialHelper.isButtonCompatible(gate.getPortalButton())) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName + " - Gate button must be a type of button.");
            return null;
        }

        // Merge frame types, add open mat to list
        frameBlocks.addAll(frameTypes);

        gate.save(parentFolder + "/"); // Updates format for version changes
        return gate;
    }

    private static int readConfig(HashMap<String, String> config, String fileName, String key, int defaultInteger) {
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
    private static Material readConfig(HashMap<String, String> config, String fileName, String key, Material defaultMaterial) {
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
    public static void populateDefaults(String gateFolder) {
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
