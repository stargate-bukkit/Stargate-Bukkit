package org.sgrewritten.stargate.api.gate;

import org.bukkit.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.config.ConfigurationOption;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.gate.GateFormat;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.util.FileHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GateFormatRegistry {

    private GateFormatRegistry() {
        throw new IllegalStateException("Utility class");
    }

    private static Map<Material, List<GateFormat>> controlMaterialToGateFormatsMap;
    private static Map<String, GateFormat> knownGateFormats;
    private static final Pattern GATE_FILE = Pattern.compile(".gate$");


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
    public static @Nullable GateFormat getFormat(@NotNull String gateDesignName) {
        return knownGateFormats.get(Objects.requireNonNull(gateDesignName));
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

    /**
     * Saves all the default gate designs to the gate folder
     *
     * @throws IOException <p>If unable to read or write the default gates</p>
     */
    private static void saveDefaultGates(File dataFolder, String gateFolder) throws IOException, URISyntaxException {
        File targetDirectory = new File(dataFolder,gateFolder);
        if(!targetDirectory.exists() && !targetDirectory.mkdirs()){
            throw new IOException("Unable to create directory: " + targetDirectory);
        }
        String internalGatesDirectory = "/" + StargateConstant.INTERNAL_GATE_FOLDER;
        List<Path> walk = FileHelper.listFilesOfInternalDirectory(internalGatesDirectory);
        for (Path path : walk) {
            Matcher gateFileMatcher = GATE_FILE.matcher(path.getFileName().toString());
            if (!gateFileMatcher.find()) {
                continue;
            }
            try (InputStream inputStream = Stargate.class.getResourceAsStream(internalGatesDirectory + "/" + path.getFileName().toString())) {
                try (OutputStream outputStream = new FileOutputStream(new File(targetDirectory, path.getFileName().toString()))) {
                    inputStream.transferTo(outputStream);
                }
            }
        }

    }

    /**
     * Load all gate formats from the specified plugin data folder
     * @param pluginDataFolder <p>The folder where gate formats reside</p>
     * @throws IOException <p>If unable to load one gate format</p>
     * @throws URISyntaxException <p>Should not be thrown really</p>
     */
    public static void loadGateFormats(File pluginDataFolder) throws IOException, URISyntaxException {
        String gateFolder = ConfigurationHelper.getString(ConfigurationOption.GATE_FOLDER);
        saveDefaultGates(pluginDataFolder, gateFolder);
        List<GateFormat> gateFormats = GateFormatHandler.loadGateFormats(new File(pluginDataFolder,gateFolder));
        if (gateFormats.isEmpty()) {
            Stargate.log(Level.SEVERE, "Unable to load gate formats from the gate format folder");

        }
        GateFormatRegistry.setFormats(gateFormats);
    }

}
