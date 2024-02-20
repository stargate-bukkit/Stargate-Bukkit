package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.material.BukkitMaterialSpecifier;
import net.knarcraft.stargate.config.material.MaterialSpecifier;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Helper class for reading gate files
 */
public final class GateReader {

    private GateReader() {

    }

    /**
     * Reads a gate file
     *
     * @param scanner              <p>The scanner to read from</p>
     * @param characterMaterialMap <p>The map of characters to store valid symbols in</p>
     * @param fileName             <p>The filename of the loaded gate config file</p>
     * @param design               <p>The list to store the loaded design/layout to</p>
     * @param config               <p>The map of config values to store to</p>
     * @return <p>The column count/width of the loaded gate</p>
     */
    public static int readGateFile(@NotNull Scanner scanner,
                                   @NotNull Map<Character, List<MaterialSpecifier>> characterMaterialMap,
                                   @NotNull String fileName, @NotNull List<List<Character>> design,
                                   @NotNull Map<String, String> config) {
        boolean designing = false;
        int columns = 0;
        try (scanner) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (designing) {
                    //If we have reached the gate's layout/design, read it
                    columns = readGateDesignLine(line, columns, characterMaterialMap, fileName, design);
                    if (columns < 0) {
                        return -1;
                    }
                } else {
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        //Read a normal config value
                        readGateConfigValue(line, characterMaterialMap, config);
                    } else if ((line.isEmpty()) || (!line.contains("=") && !line.startsWith("#"))) {
                        //An empty line marks the start of the gate's layout/design
                        designing = true;
                    }
                }
            }
        } catch (Exception exception) {
            Stargate.logSevere(String.format("Could not load Gate %s - %s", fileName, exception.getMessage()));
            return -1;
        }
        return columns;
    }

    /**
     * Reads one design line of the gate layout file
     *
     * <p>The max columns value is sent through this method in such a way that when the last gate design line is read,
     * the max columns value contains the largest amount of columns (character) found in any of the design's lines.</p>
     *
     * @param line                 <p>The line to read</p>
     * @param maxColumns           <p>The current max columns value of the design</p>
     * @param characterMaterialMap <p>The map between characters and the corresponding materials to use</p>
     * @param fileName             <p>The filename of the loaded gate config file</p>
     * @param design               <p>The two-dimensional list to store the loaded design to</p>
     * @return <p>The new max columns value of the design</p>
     */
    private static int readGateDesignLine(@NotNull String line, int maxColumns,
                                          @NotNull Map<Character, List<MaterialSpecifier>> characterMaterialMap,
                                          @NotNull String fileName, @NotNull List<List<Character>> design) {
        List<Character> row = new ArrayList<>();

        //Update the max columns number if this line has more columns
        if (line.length() > maxColumns) {
            maxColumns = line.length();
        }

        for (Character symbol : line.toCharArray()) {
            //Refuse read gate designs with unknown characters
            if (symbol.equals('?') || !characterMaterialMap.containsKey(symbol)) {
                Stargate.logSevere(String.format("Could not load Gate %s - Unknown symbol '%s' in diagram", fileName,
                        symbol));
                return -1;
            }
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
    private static void readGateConfigValue(@NotNull String line,
                                            @NotNull Map<Character, List<MaterialSpecifier>> characterMaterialMap,
                                            @NotNull Map<String, String> config) throws Exception {
        String[] split = line.split("=");
        String key = split[0].trim();
        String value = split[1].trim();

        if (key.length() == 1) {
            //Read a gate frame material
            Character symbol = key.charAt(0);

            List<MaterialSpecifier> materials = MaterialHelper.parseTagsAndMaterials(value);
            if (!materials.isEmpty()) {
                characterMaterialMap.put(symbol, materials);
            } else {
                throw new Exception("Invalid material in line: " + line);
            }
        } else {
            //Read a normal config value
            config.put(key, value);
        }
    }

    /**
     * Reads an integer configuration value
     *
     * @param config   <p>The configuration to read</p>
     * @param fileName <p>The filename of the config file</p>
     * @param key      <p>The config key to read</p>
     * @return <p>The read value, or -1 if it could not be read</p>
     */
    public static int readGateConfig(@NotNull Map<String, String> config, @NotNull String fileName,
                                     @NotNull String key) {
        if (config.containsKey(key)) {
            try {
                return Integer.parseInt(config.get(key));
            } catch (NumberFormatException exception) {
                Stargate.logWarning(String.format("%s reading %s: %s is not numeric", exception.getClass().getName(),
                        fileName, key));
            }
        }

        return -1;
    }

    /**
     * Reads a material configuration value
     *
     * @param config          <p>The configuration to read</p>
     * @param fileName        <p>The filename of the config file</p>
     * @param key             <p>The config key to read</p>
     * @param defaultMaterial <p>The default material to use, in case the config is invalid</p>
     * @return <p>The material specified in the config, or the default material if it could not be read</p>
     */
    @NotNull
    public static List<MaterialSpecifier> readGateConfig(@NotNull Map<String, String> config, @NotNull String fileName,
                                                         @NotNull String key, @NotNull Material defaultMaterial) {
        if (config.containsKey(key)) {
            List<MaterialSpecifier> materialSpecifiers = MaterialHelper.parseTagsAndMaterials(config.get(key));
            if (!materialSpecifiers.isEmpty()) {
                return materialSpecifiers;
            } else {
                Stargate.logWarning(String.format("Error reading %s: %s is not a material", fileName, key));
            }
        }
        return List.of(new BukkitMaterialSpecifier(defaultMaterial));
    }

    /**
     * Generates a matrix containing the gate layout
     *
     * <p>This basically changes the list of lists into a primitive matrix. Additionally, spaces are added to the end of
     * each row which to too short relative to the longest row.</p>
     *
     * @param design  <p>The design of the gate layout</p>
     * @param columns <p>The largest amount of columns in the design</p>
     * @return <p>A matrix containing the gate's layout</p>
     */
    @NotNull
    public static Character[][] generateLayoutMatrix(@NotNull List<List<Character>> design, int columns) {
        Character[][] layout = new Character[design.size()][columns];
        for (int lineIndex = 0; lineIndex < design.size(); lineIndex++) {
            List<Character> row = design.get(lineIndex);
            Character[] result = new Character[columns];

            for (int rowIndex = 0; rowIndex < columns; rowIndex++) {
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

}
