package net.knarcraft.stargate;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * stargate - A portal plugin for Bukkit
 * Copyright (C) 2011 Shaun (sturmeh)
 * Copyright (C) 2011 Dinnerbone
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * Copyright (C) 2021 Kristian Knarvik
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Gate {

    private static final Character ANYTHING = ' ';
    private static final Character ENTRANCE = '.';
    private static final Character EXIT = '*';
    private static final HashMap<String, Gate> gates = new HashMap<>();
    private static final HashMap<Material, ArrayList<Gate>> controlBlocks = new HashMap<>();
    private static final HashSet<Material> frameBlocks = new HashSet<>();

    private final String filename;
    private final Character[][] layout;
    private final HashMap<Character, Material> types;
    private RelativeBlockVector[] entrances = new RelativeBlockVector[0];
    private RelativeBlockVector[] border = new RelativeBlockVector[0];
    private RelativeBlockVector[] controls = new RelativeBlockVector[0];
    private RelativeBlockVector exitBlock = null;
    private final HashMap<RelativeBlockVector, Integer> exits = new HashMap<>();
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

    private void populateCoordinates() {
        ArrayList<RelativeBlockVector> entranceList = new ArrayList<>();
        ArrayList<RelativeBlockVector> borderList = new ArrayList<>();
        ArrayList<RelativeBlockVector> controlList = new ArrayList<>();
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

    public boolean matches(BlockLocation topleft, int modX, int modZ) {
        return matches(topleft, modX, modZ, false);
    }

    public boolean matches(BlockLocation topleft, int modX, int modZ, boolean onCreate) {
        HashMap<Character, Material> portalTypes = new HashMap<>(types);
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                Character key = layout[y][x];

                if (key.equals(ENTRANCE) || key.equals(EXIT)) {
                    if (Stargate.ignoreEntrance) continue;

                    Material type = topleft.modRelative(x, y, 0, modX, 1, modZ).getType();

                    // Ignore entrance if it's air and we're creating a new gate
                    if (onCreate && type == Material.AIR) continue;

                    if (type != portalBlockClosed && type != portalBlockOpen) {
                        Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                        return false;
                    }
                } else if (!key.equals(ANYTHING)) {
                    Material id = portalTypes.get(key);
                    if (id == null) {
                        portalTypes.put(key, topleft.modRelative(x, y, 0, modX, 1, modZ).getType());
                    } else if (topleft.modRelative(x, y, 0, modX, 1, modZ).getType() != id) {
                        Stargate.debug("Gate::Matches", "Block Type Mismatch: " + topleft.modRelative(x, y, 0, modX, 1, modZ).getType() + " != " + id);
                        return false;
                    }
                }
            }
        }

        return true;
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
        boolean designing = false;
        ArrayList<ArrayList<Character>> design = new ArrayList<>();
        HashMap<Character, Material> types = new HashMap<>();
        HashMap<String, String> config = new HashMap<>();
        HashSet<Material> frameTypes = new HashSet<>();
        int cols = 0;

        // Init types map
        types.put(ENTRANCE, Material.AIR);
        types.put(EXIT, Material.AIR);
        types.put(ANYTHING, Material.AIR);

        try {
            scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (designing) {
                    ArrayList<Character> row = new ArrayList<>();

                    if (line.length() > cols) {
                        cols = line.length();
                    }

                    for (Character symbol : line.toCharArray()) {
                        if ((symbol.equals('?')) || (!types.containsKey(symbol))) {
                            Stargate.log.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Unknown symbol '" + symbol + "' in diagram");
                            return null;
                        }
                        row.add(symbol);
                    }

                    design.add(row);
                } else {
                    if ((line.isEmpty()) || (!line.contains("="))) {
                        designing = true;
                    } else {
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
                    }
                }
            }
        } catch (Exception ex) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + ex.getMessage());
            return null;
        } finally {
            if (scanner != null) scanner.close();
        }

        Character[][] layout = new Character[design.size()][cols];

        for (int y = 0; y < design.size(); y++) {
            ArrayList<Character> row = design.get(y);
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

        Gate gate = new Gate(file.getName(), layout, types);

        gate.portalBlockOpen = readConfig(config, gate, file, "portal-open", gate.portalBlockOpen);
        gate.portalBlockClosed = readConfig(config, gate, file, "portal-closed", gate.portalBlockClosed);
        gate.button = readConfig(config, gate, file, "button", gate.button);
        gate.useCost = readConfig(config, gate, file, "usecost", -1);
        gate.destroyCost = readConfig(config, gate, file, "destroycost", -1);
        gate.createCost = readConfig(config, gate, file, "createcost", -1);
        gate.toOwner = (config.containsKey("toowner") ? Boolean.valueOf(config.get("toowner")) : EconomyHandler.toOwner);

        if (gate.getControls().length != 2) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Gates must have exactly 2 control points.");
            return null;
        }

        if (!Tag.BUTTONS.isTagged(gate.button)) {
            Stargate.log.log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Gate button must be a type of button.");
            return null;
        }

        // Merge frame types, add open mat to list
        frameBlocks.addAll(frameTypes);

        gate.save(file.getParent() + "/"); // Updates format for version changes
        return gate;
    }

    private static int readConfig(HashMap<String, String> config, Gate gate, File file, String key, int def) {
        if (config.containsKey(key)) {
            try {
                return Integer.parseInt(config.get(key));
            } catch (NumberFormatException ex) {
                Stargate.log.log(Level.WARNING, String.format("%s reading %s: %s is not numeric", ex.getClass().getName(), file, key));
            }
        }

        return def;
    }

    private static Material readConfig(HashMap<String, String> config, Gate gate, File file, String key, Material def) {
        if (config.containsKey(key)) {
            Material mat = Material.getMaterial(config.get(key));
            if (mat != null) {
                return mat;
            }
            Stargate.log.log(Level.WARNING, String.format("Error reading %s: %s is not a material", file, key));
        }
        return def;
    }

    public static void loadGates(String gateFolder) {
        File dir = new File(gateFolder);
        File[] files;

        if (dir.exists()) {
            files = dir.listFiles(new StargateFilenameFilter());
        } else {
            files = new File[0];
        }

        if (files == null || files.length == 0) {
            if (dir.mkdir()) {
                populateDefaults(gateFolder);
            }
        } else {
            for (File file : files) {
                Gate gate = loadGate(file);
                if (gate != null) registerGate(gate);
            }
        }
    }

    public static void populateDefaults(String gateFolder) {
        Character[][] layout = new Character[][]{
                {' ', 'X', 'X', ' '},
                {'X', '.', '.', 'X'},
                {'-', '.', '.', '-'},
                {'X', '*', '.', 'X'},
                {' ', 'X', 'X', ' '},
        };
        HashMap<Character, Material> types = new HashMap<>();
        types.put(ENTRANCE, Material.AIR);
        types.put(EXIT, Material.AIR);
        types.put(ANYTHING, Material.AIR);
        types.put('X', Material.OBSIDIAN);
        types.put('-', Material.OBSIDIAN);

        Gate gate = new Gate("nethergate.gate", layout, types);
        gate.save(gateFolder);
        registerGate(gate);
    }

    public static Gate[] getGatesByControlBlock(Block block) {
        return getGatesByControlBlock(block.getType());
    }

    public static Gate[] getGatesByControlBlock(Material type) {
        Gate[] result = new Gate[0];
        ArrayList<Gate> lookup = controlBlocks.get(type);

        if (lookup != null) result = lookup.toArray(result);

        return result;
    }

    public static Gate getGateByName(String name) {
        return gates.get(name);
    }

    public static int getGateCount() {
        return gates.size();
    }

    public static boolean isGateBlock(Material type) {
        return frameBlocks.contains(type);
    }

    static class StargateFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".gate");
        }
    }

    public static void clearGates() {
        gates.clear();
        controlBlocks.clear();
        frameBlocks.clear();
    }
}
