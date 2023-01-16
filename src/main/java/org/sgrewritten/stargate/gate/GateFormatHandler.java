package org.sgrewritten.stargate.gate;

import org.bukkit.Material;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.exception.ParsingErrorException;

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
 * A handler that keeps track of all known gate formats
 */
public class GateFormatHandler {

    private static Map<Material, List<GateFormat>> controlMaterialToGateFormatsMap;
    private static Map<String, GateFormat> knownGateFormats;

    /**
     * Gets the number of stored gate formats
     *
     * @return <p>The number of stored gate formats</p>
     */
    public static int formatsStored() {
        return knownGateFormats.size();
    }

    /**
     * Gets the names of all known gate formats
     *
     * @return <p>The names of all known gate formats</p>
     */
    public static Set<String> getAllGateFormatNames() {
        return knownGateFormats.keySet();
    }

    /**
     * Gets the gate format corresponding to the given gate design name
     *
     * @param gateDesignName <p>The gate design name to get the format of</p>
     * @return <p>The gate format, or null if no such gate format</p>
     */
    public static GateFormat getFormat(String gateDesignName) {
        return knownGateFormats.get(gateDesignName);
    }

    /**
     * Sets the gate formats known by the gate format handler
     *
     * @param gateFormats <p>The new list of known gate formats</p>
     */
    public static void setFormats(List<GateFormat> gateFormats) {
        controlMaterialToGateFormatsMap = new EnumMap<>(Material.class);
        knownGateFormats = new HashMap<>();
        for (GateFormat format : gateFormats) {
            addGateFormat(controlMaterialToGateFormatsMap, format, format.getControlMaterials());
            knownGateFormats.put(format.getFileName(), format);
        }
    }

    /**
     * Loads all gate formats from the gate folder
     *
     * @param dir <p>The folder to load gates from</p>
     * @param logger
     * @return <p>A map between a control block material and the corresponding gate format</p>
     */
    public static List<GateFormat> loadGateFormats(File dir, StargateLogger logger) {
        Stargate.log(Level.FINE,"Loading gates from " + dir.getAbsolutePath());
        List<GateFormat> gateFormatMap = new ArrayList<>();
        File[] files = dir.exists() ? dir.listFiles((directory, name) -> name.endsWith(".gate")) : new File[0];

        if (files == null) {
            return null;
        }

        for (File file : files) {
            try {
                gateFormatMap.add(loadGateFormat(file, logger));
            } catch (FileNotFoundException | ParsingErrorException exception) {
                logger.logMessage(Level.WARNING, "Could not load Gate " + file.getName() + " - " + exception.getMessage());
                if (exception instanceof ParsingErrorException && file.exists()) {
                    if (!file.renameTo(new File(dir, file.getName() + ".invalid"))) {
                        logger.logMessage(Level.WARNING, "Could not add .invalid to gate. Make sure file " +
                                "permissions are set correctly.");
                    }
                }
            }
        }
        return gateFormatMap;
    }

    /**
     * Gets all gate format using the given control block material
     *
     * @param signParentBlockMaterial <p>The material of a placed sign's parent block</p>
     * @return <p>All gate formats using the given control block</p>
     */
    public static List<GateFormat> getPossibleGateFormatsFromControlBlockMaterial(Material signParentBlockMaterial) {
        List<GateFormat> possibleGates = controlMaterialToGateFormatsMap.get(signParentBlockMaterial);
        if (possibleGates == null) {
            return new ArrayList<>();
        }
        return possibleGates;

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
     * Loads the given gate format file
     *
     * @param file   <p>The gate format file to load</p>
     * @param logger <p>The logger used for logging</p>
     * @throws ParsingErrorException <p>If unable to load the gate format</p>
     * @throws FileNotFoundException <p>If the gate file does not exist</p>
     */
    private static GateFormat loadGateFormat(File file, StargateLogger logger) throws ParsingErrorException,
            FileNotFoundException {
        Stargate.log(Level.CONFIG, "Loaded gate format " + file.getName());
        try (Scanner scanner = new Scanner(file)) {
            Stargate.log(Level.FINER, "Gate file size:" + file.length());
            if (file.length() > 65536L) {
                throw new ParsingErrorException("Design is too large");
            }

            GateFormatParser gateParser = new GateFormatParser(scanner, file.getName(), logger);
            return gateParser.parseGateFormat();
        }
    }

}
