package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.ParsingError;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

public class GateFormat {
    public static HashMap<Material, List<GateFormat>> controlMaterialFormatsMap;
    public EnumMap<GateStructureType, GateStructure> portalParts;

    public final String name;
    public boolean isIronDoorBlockable;

    public GateFormat(GateIris iris, GateFrame frame, GateControll controll, HashMap<String, String> config,
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
    public boolean matches(Gate.VectorOperation converter, Location loc) {
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

    public static HashMap<Material, List<GateFormat>> loadGateFormats(String gateFolder) {
        HashMap<Material, List<GateFormat>> controlToGateMap = new HashMap<>();
        File dir = new File(gateFolder);
        File[] files = dir.exists() ? dir.listFiles(new StargateFilenameFilter()) : new File[0];

        for (File file : files) {
            Stargate.log(Level.FINE, "Reading gateFormat from " + file.getName());
            GateFormatParser gateParser = new GateFormatParser(file);
            try {
                gateParser.open();
                GateFormat format = gateParser.parse();
                addGateFormat(controlToGateMap, format, gateParser.controlMaterials);
            } catch (FileNotFoundException | ParsingError e) {
                Stargate.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + e.getMessage());
            } finally {
                gateParser.close();
            }
        }
        return controlToGateMap;
    }

    private static void addGateFormat(HashMap<Material, List<GateFormat>> register, GateFormat format,
                                      HashSet<Material> controlMaterials) {
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

    private static class GateFormatParser {
        File file;
        Scanner scanner;
        String line;
        HashMap<Character, HashSet<Material>> frameMaterials;

        final static char NOTINGATE = ' ';
        final static char EXIT = '*';
        final static char ENTRANCE = '.';
        final static char CONTROL = '-';


        boolean gateHasEntrance = false;
        int amountOfControlBlocks = 0;

        HashSet<Material> irisOpen;
        HashSet<Material> irisClosed;
        GateIris iris;
        GateFrame frame;
        GateControll control;
        HashSet<Material> controlMaterials;

        boolean isIronDoorBlockable = false;

        GateFormatParser(File file) {
            this.file = file;

            frameMaterials = new HashMap<>();
            // Default settings
            irisOpen = new HashSet<>();
            irisOpen.add(Material.WATER);
            irisClosed = new HashSet<>();
            irisClosed.add(Material.AIR);
        }

        void open() throws FileNotFoundException {
            scanner = new Scanner(file);
        }

        void close() {
            try {
                scanner.close();
            } catch (NullPointerException ignored) {
            }
        }

        GateFormat parse() throws ParsingError {
            Stargate.log(Level.FINEST, "filespace:" + file.length());
            if (file.length() > 65536L)
                throw new ParsingError("Design is too large");


            HashMap<String, String> config = parseSettings();
            HashMap<String, String> remainingConfig = setSettings(config);

            List<String> designLines = loadDesign();
            setDesign(designLines);

            if (!gateHasEntrance)
                throw new ParsingError("Design is missing an entrance ");
            if (amountOfControlBlocks < 2)
                throw new ParsingError("Design requires atleast 2 control blocks '-' ");


            return new GateFormat(iris, frame, control, remainingConfig, this.file.getName(), isIronDoorBlockable);
        }


        private HashMap<String, String> setSettings(HashMap<String, String> config) throws ParsingError {
            HashMap<String, String> remaining = new HashMap<>();
            for (String key : config.keySet()) {
                if (key.length() != 1) {
                    switch (key) {
                        case "portal-open":
                            irisOpen = parseMaterial(config.get(key));
                            break;
                        case "portal-closed":
                            irisClosed = parseMaterial(config.get(key));
                            break;
                        default:
                            remaining.put(key, config.get(key));
                            break;
                    }
                    continue;
                }

                char symbol = key.charAt(0);

                HashSet<Material> id = parseMaterial(config.get(key));
                if (symbol == '-') {
                    controlMaterials = id;
                }
                frameMaterials.put(symbol, id);
            }
            return remaining;
        }

        private HashMap<String, String> parseSettings() throws ParsingError {
            HashMap<String, String> config = new HashMap<>();
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.isBlank()) {
                    continue;
                }
                if (!line.contains("="))
                    break;

                String[] split = line.split("=");
                String key = split[0].trim();
                String value = split[1].trim();
                config.put(key, value);

            }
            return config;
        }

        private static String TAGIDENTIFIER = "#";
        private static String SPLITIDENTIFIER = ",";

        /**
         * Just parses all of the materials based from tags or material id (when a string starts with #,
         * it should be a tag). "," resembles a split in items
         *
         * @param stringIdsMsg
         * @return
         * @throws ParsingError
         */
        private HashSet<Material> parseMaterial(String stringIdsMsg) throws ParsingError {
            HashSet<Material> foundIds = new HashSet<>();
            String[] induvidialIDs = stringIdsMsg.split(SPLITIDENTIFIER);
            for (String stringId : induvidialIDs) {
                if (stringId.startsWith(TAGIDENTIFIER)) {
                    String tagString = stringId.replace(TAGIDENTIFIER, "");
                    Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS,
                            NamespacedKey.minecraft(tagString.toLowerCase()), Material.class);
                    if (tag == null) {
                        throw new ParsingError("Invalid tag in line: " + line);
                    }
                    for (Material mat : tag.getValues()) {
                        if (mat.isBlock())
                            foundIds.add(mat);
                    }
                    continue;
                }

                Material id = Material.getMaterial(stringId);
                if (id == null) {
                    throw new ParsingError("Invalid material ''" + stringId + "''");
                }
                foundIds.add(id);
            }
            if (foundIds.size() == 0) {
                throw new ParsingError("Invalid field''" + stringIdsMsg + "'': Field must include atleast one block");
            }
            return foundIds;

        }


        private List<String> loadDesign() {
            List<String> designLines = new ArrayList<>();
            /*
             * The initial line of the gateDesign has been loaded by parseSettings (acting as an endpoint of settings).
             * The 1 following line of code takes this into consideration.
             */
            designLines.add(line);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                designLines.add(line);
            }
            return designLines;
        }

        /**
         * Creates a vector-structure from the character design, following this reference system:
         * FFF    y
         * C.C    ^
         * F*F    |
         * FFF    ---->z
         * <p>
         * where F,C,.,* resembles a gatedesign and the rest is the coordinate system used by the vectors.
         * Note that origo is at the topleft corner of the gatedesign.
         * <p>
         * Vectors are also divided into 3 different structures:
         * - GateIris
         * - GateFrame
         * - GateControll
         * Note that some structures need a selected material
         *
         * @param lines : The lines in the .gate file with everything about positions (the actual design)
         * @throws ParsingError
         */
        private void setDesign(List<String> lines) throws ParsingError {
            iris = new GateIris(irisOpen, irisClosed);
            frame = new GateFrame();
            control = new GateControll();
            int lineNr, i;
            for (lineNr = 0; lineNr < lines.size(); lineNr++) {
                char[] charLine = lines.get(lineNr).toCharArray();
                for (i = 0; i < charLine.length; i++) {
                    BlockVector selectedLocation = new BlockVector(0, -lineNr, i);
                    setDesignPoint(charLine[i], selectedLocation.clone());
                }
            }

            /*
             * Check if iris only contains 2 blocks, where one is above the other
             * (Only viable format that can be blocked by irondoor)
             */
            List<BlockVector> irisBlocks = iris.getPartsPos();
            if (irisBlocks.size() == 2) {
                for (BlockVector block : irisBlocks) {
                    BlockVector above = block.clone();
                    above.setY(block.getBlockY() + 1);
                    if (irisBlocks.contains(above)) {
                        isIronDoorBlockable = true;
                        break;
                    }
                }
            }
        }

        /**
         * Determines how one char point in the design should be added into the gateStructure
         *
         * @param key
         * @param selectedLocation
         * @throws ParsingError
         */
        private void setDesignPoint(char key, BlockVector selectedLocation) throws ParsingError {
            switch (key) {
                case NOTINGATE:
                    break;
                case EXIT:
                    iris.addExit(selectedLocation.clone());
                    break;
                case ENTRANCE:
                    iris.addPart(selectedLocation.clone());
                    gateHasEntrance = true;
                    break;
                case CONTROL:
                    amountOfControlBlocks++;
                    frame.addPart(selectedLocation.clone(), frameMaterials.get(key));
                    BlockVector controlLocation = selectedLocation.clone();
                    controlLocation.add(new BlockVector(1, 0, 0));
                    control.addPart(controlLocation);
                    break;
                default:
                    if ((key == '?') || (!frameMaterials.containsKey(key))) {
                        throw new ParsingError("Unknown symbol '" + key + "' in gatedesign");
                    }
                    frame.addPart(selectedLocation.clone(), frameMaterials.get(key));
            }
        }
    }


}
