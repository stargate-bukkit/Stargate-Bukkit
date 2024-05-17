package net.knarcraft.stargate.portal.property.gate;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.material.BukkitMaterialSpecifier;
import net.knarcraft.stargate.config.material.MaterialSpecifier;
import net.knarcraft.stargate.utility.MaterialHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;

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

    private static final Map<String, Gate> gates = new HashMap<>();
    private static final Map<Material, List<Gate>> controlBlocks = new HashMap<>();
    private static final Map<String, List<Gate>> controlBlockTags = new HashMap<>();

    private GateHandler() {

    }

    /**
     * Gets the character used for blocks that are not part of the gate
     *
     * @return <p>The character used for blocks that are not part of the gate</p>
     */
    @NotNull
    public static Character getAnythingCharacter() {
        return ANYTHING;
    }

    /**
     * Gets the character used for defining the entrance
     *
     * @return <p>The character used for defining the entrance</p>
     */
    @NotNull
    public static Character getEntranceCharacter() {
        return ENTRANCE;
    }

    /**
     * Gets the character used for defining the exit
     *
     * @return <p>The character used for defining the exit</p>
     */
    @NotNull
    public static Character getExitCharacter() {
        return EXIT;
    }


    /**
     * Gets the character used for defining control blocks
     *
     * @return <p>The character used for defining control blocks</p>
     */
    @NotNull
    public static Character getControlBlockCharacter() {
        return CONTROL_BLOCK;
    }

    /**
     * Register a gate into the list of available gates
     *
     * @param gate <p>The gate to register</p>
     */
    private static void registerGate(@NotNull Gate gate) {
        gates.put(gate.getFilename(), gate);

        Set<Material> blockTypes = MaterialHelper.specifiersToMaterials(gate.getControlBlockMaterials());
        for (Material material : blockTypes) {
            if (!controlBlocks.containsKey(material)) {
                controlBlocks.put(material, new ArrayList<>());
            }
            controlBlocks.get(material).add(gate);
        }
    }

    /**
     * Loads a gate from a file
     *
     * @param file <p>The file containing the gate data</p>
     * @return <p>The loaded gate, or null if unable to load the gate</p>
     */
    @Nullable
    private static Gate loadGate(@NotNull File file) {
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
    @Nullable
    private static Gate loadGate(@NotNull String fileName, @NotNull String parentFolder,
                                 @NotNull Scanner scanner) {
        List<List<Character>> design = new ArrayList<>();
        Map<Character, List<MaterialSpecifier>> characterMaterialMap = new HashMap<>();
        Map<String, String> config = new HashMap<>();

        //Initialize character to material map
        characterMaterialMap.put(ENTRANCE, List.of(new BukkitMaterialSpecifier(Material.AIR)));
        characterMaterialMap.put(EXIT, List.of(new BukkitMaterialSpecifier(Material.AIR)));
        characterMaterialMap.put(ANYTHING, List.of(new BukkitMaterialSpecifier(Material.AIR)));

        //Read the file into appropriate lists and maps
        int columns = readGateFile(scanner, characterMaterialMap, fileName, design, config);
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
        gate.save(parentFolder);
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
    @Nullable
    private static Gate createGate(@NotNull Map<String, String> config, @NotNull String fileName,
                                   @NotNull Character[][] layout,
                                   @NotNull Map<Character, List<MaterialSpecifier>> characterMaterialMap) {
        //Read relevant material types
        List<MaterialSpecifier> portalOpenBlock = readGateConfig(config, fileName, "portal-open", defaultPortalBlockOpen);
        List<MaterialSpecifier> portalClosedBlock = readGateConfig(config, fileName, "portal-closed", defaultPortalBlockClosed);
        List<MaterialSpecifier> portalButton = readGateConfig(config, fileName, "button", defaultButton);

        //Read economy values
        int useCost = readGateConfig(config, fileName, "usecost");
        int createCost = readGateConfig(config, fileName, "createcost");
        int destroyCost = readGateConfig(config, fileName, "destroycost");
        boolean toOwner = (config.containsKey("toowner") ? Boolean.parseBoolean(config.get("toowner")) :
                Stargate.getEconomyConfig().sendPaymentToOwner());
        GateCosts gateCosts = new GateCosts(useCost, createCost, destroyCost, toOwner);

        //Create the new gate
        Gate gate = new Gate(fileName, new GateLayout(layout), characterMaterialMap, portalOpenBlock, portalClosedBlock,
                portalButton, gateCosts);

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
    private static boolean validateGate(@NotNull Gate gate, @NotNull String fileName) {
        String failString = String.format("Could not load Gate %s", fileName) + " - %s";

        if (gate.getLayout().getControls().length != 2) {
            Stargate.logSevere(String.format(failString, "Gates must have exactly 2 control points."));
            return false;
        }
        
        if (gate.getLayout().getExit() == null) {
            Stargate.logSevere(String.format(failString, "Gates must have one specified exit point"));
            return false;
        }

        if (checkMaterialPredicateFail(gate.getPortalButtonMaterials(), MaterialHelper::isButtonCompatible)) {
            Stargate.logSevere(String.format(failString, "Gate button must be a type of button."));
            return false;
        }

        if (checkMaterialPredicateFail(gate.getPortalOpenMaterials(), Material::isBlock)) {
            Stargate.logSevere(String.format(failString, "Gate open block must be a type of block."));
            return false;
        }

        if (checkMaterialPredicateFail(gate.getPortalClosedMaterials(), Material::isBlock)) {
            Stargate.logSevere(String.format(failString, "Gate closed block must be a type of block."));
            return false;
        }

        for (List<MaterialSpecifier> materialSpecifiers : gate.getCharacterMaterialMap().values()) {
            if (checkMaterialPredicateFail(materialSpecifiers, Material::isBlock)) {
                Stargate.logSevere(String.format(failString, "Every gate border block must be a type of block."));
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether a predicate is true for a list of material specifiers
     *
     * @param materialSpecifiers <p>The material specifiers to test</p>
     * @param predicate          <p>The predicate to test</p>
     * @return <p>True if the predicate failed for any specified materials</p>
     */
    private static boolean checkMaterialPredicateFail(@NotNull List<MaterialSpecifier> materialSpecifiers,
                                                      @NotNull Predicate<Material> predicate) {
        Set<Material> closedMaterials = MaterialHelper.specifiersToMaterials(materialSpecifiers);
        for (Material material : closedMaterials) {
            if (!predicate.test(material)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads all gates inside the given folder
     *
     * @param gateFolder <p>The folder containing the gates</p>
     */
    public static void loadGates(@NotNull String gateFolder) {
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
    public static void writeDefaultGatesToFolder(@NotNull String gateFolder) {
        loadGateFromJar("nethergate.gate", gateFolder);
        loadGateFromJar("watergate.gate", gateFolder);
        loadGateFromJar("endgate.gate", gateFolder);
        loadGateFromJar("squarenetherglowstonegate.gate", gateFolder);
        loadGateFromJar("wool.gate", gateFolder);
    }

    /**
     * Loads the given gate file from within the Jar's resources directory
     *
     * @param gateFile   <p>The name of the gate file</p>
     * @param gateFolder <p>The folder containing gates</p>
     */
    private static void loadGateFromJar(@NotNull String gateFile, @NotNull String gateFolder) {
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
    @NotNull
    public static List<Gate> getGatesByControlBlock(@NotNull Block block) {
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
    @NotNull
    public static List<Gate> getGatesByControlBlock(@NotNull Material type) {
        List<Gate> result = new ArrayList<>();
        List<Gate> fromId = controlBlocks.get(type);
        List<Gate> fromTag = null;
        for (String tagString : controlBlockTags.keySet()) {
            Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(tagString.replaceFirst(
                    "minecraft:", "")), Material.class);
            if (tag != null && tag.isTagged(type)) {
                fromTag = controlBlockTags.get(tag.getKey().toString());
            }
        }

        if (fromId != null) {
            result.addAll(fromId);
        }
        if (fromTag != null) {
            result.addAll(fromTag);
        }

        return result;
    }

    /**
     * Gets a portal given its filename
     *
     * @param fileName <p>The filename of the gate to get</p>
     * @return <p>The gate with the given filename</p>
     */
    @Nullable
    public static Gate getGateByName(@NotNull String fileName) {
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
        controlBlockTags.clear();
    }

}
