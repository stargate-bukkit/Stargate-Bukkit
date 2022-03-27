package net.TheDgtl.Stargate.util;

import com.cryptomorin.xseries.XMaterial;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.exception.ParsingErrorException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

/**
 * Helper class for reading gate files
 */
public final class GateFormatReader {

    private static final String TAG_IDENTIFIER = "#";
    private static final String SPLIT_IDENTIFIER = ",";
    private static final Material[] allAirTypes = new Material[]{
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR,
    };

    private GateFormatReader() {

    }

    /**
     * Reads a gate file
     *
     * @param scanner              <p>The scanner to read from</p>
     * @param characterMaterialMap <p>The map of characters to store valid symbols in</p>
     * @param fileName             <p>The filename of the loaded gate config file</p>
     * @param design               <p>The list to store the loaded design/layout to</p>
     * @param config               <p>The map of config values to store to</p>
     * @param logger               <p>The logger to use for logging</p>
     * @return <p>The column count/width of the loaded gate</p>
     */
    public static int readGateFile(Scanner scanner, Map<Character, Set<Material>> characterMaterialMap, String fileName,
                                   List<List<Character>> design, Map<String, String> config,
                                   StargateLogger logger) {
        int columns;
        try {
            columns = readGateFileContents(scanner, characterMaterialMap, design, config);
        } catch (Exception exception) {
            logger.logMessage(Level.SEVERE, String.format("Could not load Gate %s - %s", fileName, exception.getMessage()));
            return -1;
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return columns;
    }

    /**
     * Parses a material string
     *
     * <p>Just parses all the materials based from tags or material id (when a string starts with #,
     * it should be a tag). "," resembles a split in items</p>
     *
     * @param materialString <p>The string describing a material/material class to parse</p>
     * @param line           <p>The line currently parsed</p>
     * @return <p>The parsed material</p>
     * @throws ParsingErrorException <p>If unable to parse the given material</p>
     */
    public static Set<Material> parseMaterial(String materialString, String line) throws ParsingErrorException {
        Set<Material> foundIDs = new HashSet<>();
        String[] individualIDs = materialString.split(SPLIT_IDENTIFIER);
        for (String stringId : individualIDs) {

            //Parse a tag
            if (stringId.startsWith(TAG_IDENTIFIER)) {
                foundIDs.addAll(parseMaterialTag(stringId.trim(), line));
                continue;
            }

            //Parse a normal material
            Material id = Material.getMaterial(stringId.toUpperCase().trim());

            if (id == Material.AIR) {
                for (Material airtype : allAirTypes) {
                    foundIDs.add(airtype);
                }
                continue;
            }

            if (id == null) {
                id = parseMaterialFromLegacyName(stringId);
                if (id == null) {
                    throw new ParsingErrorException("Invalid material ''" + stringId + "''");
                }
            }
            foundIDs.add(id);
        }
        if (foundIDs.size() == 0) {
            throw new ParsingErrorException("Invalid field''" + materialString +
                    "'': Field must include at least one block");
        }
        return foundIDs;
    }

    /**
     * Reads a gate file's contents
     *
     * @param scanner              <p>The scanner to read from</p>
     * @param characterMaterialMap <p>The map of characters to store valid symbols in</p>
     * @param design               <p>The list to store the loaded design/layout to</p>
     * @param config               <p>The map of config values to store to</p>
     * @return <p>The column count/width of the loaded gate</p>
     */
    private static int readGateFileContents(Scanner scanner, Map<Character, Set<Material>> characterMaterialMap,
                                            List<List<Character>> design, Map<String, String> config) throws Exception {
        String line;
        boolean designing = false;
        int columns = 0;

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (designing) {
                //If we have reached the gate's layout/design, read it
                columns = readGateDesignLine(line, columns, design);
                if (columns < 0) {
                    return -1;
                }
            } else {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (!line.contains("=")) {
                    designing = true;
                    //If we have reached the gate's layout/design, read it
                    columns = readGateDesignLine(line, columns, design);
                    if (columns < 0) {
                        return -1;
                    }
                } else {
                    //Read a normal config value
                    readGateConfigValue(line, characterMaterialMap, config);
                }
            }
        }
        return columns;
    }

    /**
     * Reads one design line of the gate layout file
     *
     * <p>The max columns value is sent through this method in such a way that when the last gate design line is read,
     * the max columns value contains the largest amount of columns (character) found in any of the design's lines.</p>
     *
     * @param line       <p>The line to read</p>
     * @param maxColumns <p>The current max columns value of the design</p>
     * @param design     <p>The two-dimensional list to store the loaded design to</p>
     * @return <p>The new max columns value of the design</p>
     */
    private static int readGateDesignLine(String line, int maxColumns, List<List<Character>> design) {
        List<Character> row = new ArrayList<>();

        //Update the max columns number if this line has more columns
        if (line.length() > maxColumns) {
            maxColumns = line.length();
        }

        for (Character symbol : line.toCharArray()) {
            //Add the read character to the row
            row.add(symbol);
        }

        //Add this row of the gate's design to the two-dimensional design list
        design.add(row);
        return maxColumns;
    }

    /**
     * Reads one config value from the gate layout file
     *
     * @param line                 <p>The line to read</p>
     * @param characterMaterialMap <p>The character to material map to store to</p>
     * @param config               <p>The config value map to store to</p>
     * @throws Exception <p>If an invalid material is encountered</p>
     */
    private static void readGateConfigValue(String line, Map<Character, Set<Material>> characterMaterialMap,
                                            Map<String, String> config) throws Exception {
        String[] split = line.split("=");
        String key = split[0].trim();
        String value = split[1].trim();

        if (key.length() == 1) {
            //Parse a gate frame material
            Character symbol = key.charAt(0);
            Set<Material> material = parseMaterial(value, line);
            //Register the map between the read symbol and the corresponding materials
            characterMaterialMap.put(symbol, material);
        } else {
            //Read a normal config value
            config.put(key, value);
        }
    }

    /**
     * Parses a material assuming it's defined using a legacy name
     *
     * @param stringId <p>The legacy material name to parse</p>
     * @return <p>The resulting material, or null if no such legacy material exists</p>
     */
    private static Material parseMaterialFromLegacyName(String stringId) {
        return Material.getMaterial(XMaterial.matchXMaterial(stringId).toString());
    }

    /**
     * Parses a material tag
     *
     * @param stringId <p>A string denoting a tag</p>
     * @param line     <p>The line currently parsed</p>
     * @throws ParsingErrorException <p>If unable to parse the tag</p>
     */
    private static Set<Material> parseMaterialTag(String stringId, String line) throws ParsingErrorException {
        Set<Material> foundIDs = EnumSet.noneOf(Material.class);
        String tagString = stringId.replace(TAG_IDENTIFIER, "");
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS,
                NamespacedKey.minecraft(tagString.toLowerCase()), Material.class);
        if (tag == null) {
            throw new ParsingErrorException("Invalid tag in line: " + line);
        }
        for (Material materialInTag : tag.getValues()) {
            if (materialInTag.isBlock()) {
                foundIDs.add(materialInTag);
            }
        }
        return foundIDs;
    }

}
