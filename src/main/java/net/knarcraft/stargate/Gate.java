package net.knarcraft.stargate;

import net.knarcraft.stargate.utility.MaterialHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

public class Gate {

    private static final Character ANYTHING = ' ';
    private static final Character ENTRANCE = '.';
    private static final Character EXIT = '*';
    private static final HashMap<String, Gate> gates = new HashMap<>();
    private static final HashMap<Material, List<Gate>> controlBlocks = new HashMap<>();
    private static final HashSet<Material> frameBlocks = new HashSet<>();

    private final String filename;
    private final Character[][] layout;
    private final HashMap<Character, Material> types;
    private final HashMap<RelativeBlockVector, Integer> exits = new HashMap<>();
    private RelativeBlockVector[] entrances = new RelativeBlockVector[0];
    private RelativeBlockVector[] border = new RelativeBlockVector[0];
    private RelativeBlockVector[] controls = new RelativeBlockVector[0];
    private RelativeBlockVector exitBlock = null;
    private Material portalBlockOpen = Material.NETHER_PORTAL;
    private Material portalBlockClosed = Material.AIR;
    private Material button = Material.STONE_BUTTON;

    // Economy information
    private int useCost = -1;
    private int createCost = -1;
    private int destroyCost = -1;
    private boolean toOwner = false;

    public Gate(String filename, Character[][] layout, HashMap<Character, Material> types) {
        this.filename = filename;
        this.layout = layout;
        this.types = types;

        populateCoordinates();
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
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            return loadGate(file.getName(), file.getParent(), scanner);
        } catch (Exception ex) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + ex.getMessage());
            return null;
        } finally {
            if (scanner != null) {
                scanner.close();
            }
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

        Gate gate = new Gate(fileName, layout, types);

        gate.portalBlockOpen = readConfig(config, fileName, "portal-open", gate.portalBlockOpen);
        gate.portalBlockClosed = readConfig(config, fileName, "portal-closed", gate.portalBlockClosed);
        gate.button = readConfig(config, fileName, "button", gate.button);
        gate.useCost = readConfig(config, fileName, "usecost", -1);
        gate.destroyCost = readConfig(config, fileName, "destroycost", -1);
        gate.createCost = readConfig(config, fileName, "createcost", -1);
        gate.toOwner = (config.containsKey("toowner") ? Boolean.valueOf(config.get("toowner")) : EconomyHandler.toOwner);

        if (gate.getControls().length != 2) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + fileName + " - Gates must have exactly 2 control points.");
            return null;
        }

        if (!MaterialHelper.isButtonCompatible(gate.button)) {
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

    private void populateCoordinates() {
        List<RelativeBlockVector> entranceList = new ArrayList<>();
        List<RelativeBlockVector> borderList = new ArrayList<>();
        List<RelativeBlockVector> controlList = new ArrayList<>();
        RelativeBlockVector[] relativeExits = new RelativeBlockVector[layout[0].length];
        int[] exitDepths = new int[layout[0].length];
        RelativeBlockVector lastExit = null;

        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                Character key = layout[y][x];
                if (key.equals('-')) {
                    controlList.add(new RelativeBlockVector(x, y, 0));
                }

                if (key.equals(ENTRANCE) || key.equals(EXIT)) {
                    entranceList.add(new RelativeBlockVector(x, y, 0));
                    exitDepths[x] = y;
                    if (key.equals(EXIT)) {
                        this.exitBlock = new RelativeBlockVector(x, y, 0);
                    }
                } else if (!key.equals(ANYTHING)) {
                    borderList.add(new RelativeBlockVector(x, y, 0));
                }
            }
        }

        for (int x = 0; x < exitDepths.length; x++) {
            relativeExits[x] = new RelativeBlockVector(x, exitDepths[x], 0);
        }

        for (int x = relativeExits.length - 1; x >= 0; x--) {
            if (relativeExits[x] != null) {
                lastExit = relativeExits[x];
            } else {
                relativeExits[x] = lastExit;
            }

            if (exitDepths[x] > 0) this.exits.put(relativeExits[x], x);
        }

        this.entrances = entranceList.toArray(this.entrances);
        this.border = borderList.toArray(this.border);
        this.controls = controlList.toArray(this.controls);
    }

    public void save(String gateFolder) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(gateFolder + filename));

            writeConfig(bw, "portal-open", portalBlockOpen.name());
            writeConfig(bw, "portal-closed", portalBlockClosed.name());
            writeConfig(bw, "button", button.name());
            if (useCost != -1)
                writeConfig(bw, "usecost", useCost);
            if (createCost != -1)
                writeConfig(bw, "createcost", createCost);
            if (destroyCost != -1)
                writeConfig(bw, "destroycost", destroyCost);
            writeConfig(bw, "toowner", toOwner);

            for (Map.Entry<Character, Material> entry : types.entrySet()) {
                Character type = entry.getKey();
                Material value = entry.getValue();
                // Skip control values
                if (type.equals(ANYTHING) || type.equals(ENTRANCE) || type.equals(EXIT)) {
                    continue;
                }

                bw.append(type);
                bw.append('=');
                if (value != null) {
                    bw.append(value.toString());
                }
                bw.newLine();
            }

            bw.newLine();

            for (Character[] aLayout : layout) {
                for (Character symbol : aLayout) {
                    bw.append(symbol);
                }
                bw.newLine();
            }

            bw.close();
        } catch (IOException ex) {
            Stargate.log.log(Level.SEVERE, "Could not save Gate " + filename + " - " + ex.getMessage());
        }
    }

    private void writeConfig(BufferedWriter bw, String key, int value) throws IOException {
        bw.append(String.format("%s=%d", key, value));
        bw.newLine();
    }

    private void writeConfig(BufferedWriter bw, String key, boolean value) throws IOException {
        bw.append(String.format("%s=%b", key, value));
        bw.newLine();
    }

    private void writeConfig(BufferedWriter bw, String key, String value) throws IOException {
        bw.append(String.format("%s=%s", key, value));
        bw.newLine();
    }

    public Character[][] getLayout() {
        return layout;
    }

    public HashMap<Character, Material> getTypes() {
        return types;
    }

    public RelativeBlockVector[] getEntrances() {
        return entrances;
    }

    public RelativeBlockVector[] getBorder() {
        return border;
    }

    public RelativeBlockVector[] getControls() {
        return controls;
    }

    public HashMap<RelativeBlockVector, Integer> getExits() {
        return exits;
    }

    public RelativeBlockVector getExit() {
        return exitBlock;
    }

    public Material getControlBlock() {
        return types.get('-');
    }

    public String getFilename() {
        return filename;
    }

    public Material getPortalBlockOpen() {
        return portalBlockOpen;
    }

    public void setPortalBlockOpen(Material type) {
        portalBlockOpen = type;
    }

    public Material getPortalBlockClosed() {
        return portalBlockClosed;
    }

    public void setPortalBlockClosed(Material type) {
        portalBlockClosed = type;
    }

    public Material getButton() {
        return button;
    }

    public int getUseCost() {
        if (useCost < 0) return EconomyHandler.useCost;
        return useCost;
    }

    public Integer getCreateCost() {
        if (createCost < 0) return EconomyHandler.createCost;
        return createCost;
    }

    public Integer getDestroyCost() {
        if (destroyCost < 0) return EconomyHandler.destroyCost;
        return destroyCost;
    }

    public Boolean getToOwner() {
        return toOwner;
    }

    public boolean matches(BlockLocation topLeft, int modX, int modZ) {
        return matches(topLeft, modX, modZ, false);
    }

    public boolean matches(BlockLocation topLeft, int modX, int modZ, boolean onCreate) {
        HashMap<Character, Material> portalTypes = new HashMap<>(types);
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                Character key = layout[y][x];

                if (key.equals(ENTRANCE) || key.equals(EXIT)) {
                    if (Stargate.ignoreEntrance) {
                        continue;
                    }

                    Material type = topLeft.modRelative(x, y, 0, modX, 1, modZ).getType();

                    // Ignore entrance if it's air and we're creating a new gate
                    if (onCreate && type == Material.AIR) {
                        continue;
                    }

                    if (type != portalBlockClosed && type != portalBlockOpen) {
                        Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                        return false;
                    }
                } else if (!key.equals(ANYTHING)) {
                    Material id = portalTypes.get(key);
                    if (id == null) {
                        portalTypes.put(key, topLeft.modRelative(x, y, 0, modX, 1, modZ).getType());
                    } else if (topLeft.modRelative(x, y, 0, modX, 1, modZ).getType() != id) {
                        Stargate.debug("Gate::Matches", "Block Type Mismatch: " + topLeft.modRelative(x, y, 0, modX, 1, modZ).getType() + " != " + id);
                        return false;
                    }
                }
            }
        }

        return true;
    }

}
