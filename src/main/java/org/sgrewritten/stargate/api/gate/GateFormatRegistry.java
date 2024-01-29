package org.sgrewritten.stargate.api.gate;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.sgrewritten.stargate.gate.GateFormat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GateFormatRegistry {

    private GateFormatRegistry(){
        throw new IllegalStateException("Utility class");
    }

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
    @ApiStatus.Internal
    public static void setFormats(List<GateFormat> gateFormats) {
        controlMaterialToGateFormatsMap = new EnumMap<>(Material.class);
        knownGateFormats = new HashMap<>();
        for (GateFormat format : gateFormats) {
            addGateFormat(controlMaterialToGateFormatsMap, format, format.getControlMaterials());
            knownGateFormats.put(format.getFileName(), format);
        }
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
            controlToGateMap.putIfAbsent(controlMaterial, new ArrayList<>());
            controlToGateMap.get(controlMaterial).add(format);
        }
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

}
