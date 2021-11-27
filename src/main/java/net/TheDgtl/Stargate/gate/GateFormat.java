package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.ParsingError;
import net.TheDgtl.Stargate.vectorlogic.VectorOperation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

public class GateFormat {
    public static Map<Material, List<GateFormat>> controlMaterialFormatsMap;
    public EnumMap<GateStructureType, GateStructure> portalParts;

    public final String name;
    public boolean isIronDoorBlockable;

    public GateFormat(GateIris iris, GateFrame frame, GateControll controll, Map<String, String> config,
                      String name, boolean isIronDoorBlockable) {
        portalParts = new EnumMap<>(GateStructureType.class);
        portalParts.put(GateStructureType.IRIS, iris);
        portalParts.put(GateStructureType.FRAME, frame);
        portalParts.put(GateStructureType.CONTROLL, controll);
        this.name = name;
        this.isIronDoorBlockable = isIronDoorBlockable;
    }

    /**
     * Checks through every structure in the format, and checks whether they are
     * valid
     *
     * @param converter
     * @param loc
     * @return true if all structures are valid
     */
    public boolean matches(VectorOperation converter, Location loc) {
        for (GateStructureType structKey : portalParts.keySet()) {
            Stargate.log(Level.FINER, "---Validating " + structKey);
            if (!(portalParts.get(structKey).isValidState(converter, loc))) {
                Stargate.log(Level.INFO, structKey + " returned negative");
                return false;
            }
        }
        return true;
    }

    private static class StargateFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".gate");
        }
    }

    public static Map<Material, List<GateFormat>> loadGateFormats(String gateFolder) {
        Map<Material, List<GateFormat>> controlToGateMap = new HashMap<>();
        File dir = new File(gateFolder);
        File[] files = dir.exists() ? dir.listFiles(new StargateFilenameFilter()) : new File[0];

        if (files == null) {
            return null;
        }

        for (File file : files) {
            loadGateFormat(file, controlToGateMap);
        }
        return controlToGateMap;
    }

    private static void loadGateFormat(File file, Map<Material, List<GateFormat>> controlToGateMap) {
        Stargate.log(Level.FINE, "Reading gateFormat from " + file.getName());
        try (Scanner scanner = new Scanner(file)) {
            Stargate.log(Level.FINEST, "fileSpace:" + file.length());
            if (file.length() > 65536L) {
                throw new ParsingError("Design is too large");
            }

            GateFormatParser gateParser = new GateFormatParser(scanner, file.getName());
            GateFormat format = gateParser.parse();
            addGateFormat(controlToGateMap, format, gateParser.getControlBlockMaterials());
        } catch (FileNotFoundException | ParsingError e) {
            Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + e.getMessage());
        }
    }

    private static void addGateFormat(Map<Material, List<GateFormat>> register, GateFormat format,
                                      Set<Material> controlMaterials) {
        for (Material mat : controlMaterials) {
            if (!(register.containsKey(mat))) {
                List<GateFormat> gateFormatList = new ArrayList<>();
                register.put(mat, gateFormatList);
            }
            register.get(mat).add(format);
        }
    }

    public static List<GateFormat> getPossibleGatesFromControll(Material controlBlockId) {
        List<GateFormat> possibleGates = controlMaterialFormatsMap.get(controlBlockId);
        if (possibleGates == null)
            return new ArrayList<>();
        return possibleGates;

    }

    public List<BlockVector> getControllBlocks() {
        GateControll controll = (GateControll) portalParts.get(GateStructureType.CONTROLL);

        return controll.parts;
    }

    public Material getIrisMat(boolean isOpen) {
        return ((GateIris) portalParts.get(GateStructureType.IRIS)).getMat(isOpen);
    }

    public BlockVector getExit() {
        return ((GateIris) portalParts.get(GateStructureType.IRIS)).getExit();
    }


}
