/*
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.TheDgtl.Stargate;

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
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;

public class Gate {
    private static final Character ANYTHING = ' ';
    private static final Character ENTRANCE = '.';
    private static final Character EXIT = '*';

    private static final HashMap<String, Gate> gates = new HashMap<>();
    private static final HashMap<Material, ArrayList<Gate>> controlBlocks = new HashMap<>();

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

    // Economy information
    private int useCost = -1;
    private int createCost = -1;
    private int destroyCost = -1;
    private boolean toOwner = false;

    // di
    private final Stargate stargate;
	private HashMap<Character, Tag<Material>> tagTypes;

    public Gate(Stargate stargate, String filename, Character[][] layout, HashMap<Character, Material> types,HashMap<Character,Tag<Material>> tagTypes ) {
        this.stargate = stargate;
        this.filename = filename;
        this.layout = layout;
        this.types = types;
        this.tagTypes = tagTypes;
        
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

                if (key.equals(ANYTHING)) continue;

                if (key.equals(ENTRANCE) || key.equals(EXIT)) {
                    entranceList.add(new RelativeBlockVector(x, y, 0));
                    exitDepths[x] = y;

                    if (key.equals(EXIT)) {
                        this.exitBlock = new RelativeBlockVector(x, y, 0);
                    }

                    continue;
                }

                borderList.add(new RelativeBlockVector(x, y, 0));
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
            stargate.getStargateLogger().log(Level.SEVERE, "Could not save Gate " + filename + " - " + ex.getMessage());
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

    public RelativeBlockVector[] getEntrances() {
        return entrances;
    }

    public RelativeBlockVector[] getBorder() {
        return border;
    }

    public RelativeBlockVector[] getControls() {
        return controls;
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

    private void setPortalBlockClosed(Material type) {
        portalBlockClosed = type;
    }

    public int getUseCost() {
        if (useCost < 0) return stargate.getEconomyHandler().getUseCost();
        return useCost;
    }

    public Integer getCreateCost() {
        if (createCost < 0) return stargate.getEconomyHandler().getCreateCost();
        return createCost;
    }

    public Integer getDestroyCost() {
        if (destroyCost < 0) return stargate.getEconomyHandler().getDestroyCost();
        return destroyCost;
    }

    public Boolean getToOwner() {
        return toOwner;
    }

    public boolean matches(Blox topleft, int modX, int modZ) {
        return matches(topleft, modX, modZ, false);
    }
    
    /**
     * Checks if this gate layout matches with its surrounding
     * @param topleft 
     * @param modX identifier which gives the direction of the portal
     * @param modZ identifier which gives the direction of the portal
     * @param onCreate
     * @return true if it matched
     */
    public boolean matches(Blox topleft, int modX, int modZ, boolean onCreate) {
        HashMap<Character, Material> portalTypes = new HashMap<>(types);
        HashMap<Character,Tag<Material>> tagPortalTypes = new HashMap<>(tagTypes);
        
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                Character key = layout[y][x];

                if (key.equals(ANYTHING)) {
                    continue;
                }

                if (key.equals(ENTRANCE) || key.equals(EXIT)) {
                    if (stargate.isIgnoreEntrance()) continue;

                    Material type = topleft.modRelative(x, y, 0, modX, 1, modZ).getType();
                    
                    if(type.isAir())
                    	type = Material.AIR;

                    if (type != portalBlockClosed && type != portalBlockOpen) {
                        Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                        return false;
                    }

                    continue;
                }
                Material id = portalTypes.get(key);
            	Material blockType = topleft.modRelative(x, y, 0, modX, 1, modZ).getType();
                boolean matches = false;
                if (id != null) {
                	
	                String idString = id.toString();
	                String blockString = blockType.toString();
	
	                matches = blockType == id;
	
	                // Hack 7/5/2020
	                // using LEGACY_* as a wildcard
	                // Using LEGACY_CONCRETE will match ALL concrete colours
	                if (idString.contains("LEGACY") && !matches) {
	                    String noLegacy = idString.replace("LEGACY_", "");
	                    matches = blockString.contains(noLegacy);
	                }
                } else {
                	Stargate.debug("Gate::Matches", "Checking through Tags");
                	Tag<Material> tag = tagPortalTypes.get(key);
                	if(tag == null) {
                		Stargate.debug("Gate::Matches", "Tag not found");
                        portalTypes.put(key, topleft.modRelative(x, y, 0, modX, 1, modZ).getType());
                        continue;
                	}
                	matches = tag.isTagged(blockType);
                	
                }

                if (!matches) {
                    Stargate.debug("Gate::Matches", "Block Type Mismatch: " + topleft.modRelative(x, y, 0, modX, 1, modZ).getType() + " != " + id);
                    return false;
                }
            }
        }

        return true;
    }

    private static void registerGate(Gate gate) {
        gates.put(gate.getFilename(), gate);

        Material blockID = gate.getControlBlock();

        if (!controlBlocks.containsKey(blockID)) {
            controlBlocks.put(blockID, new ArrayList<>());
        }

        controlBlocks.get(blockID).add(gate);
    }

    private static Gate loadGate(Stargate stargate, File file) {
        Scanner scanner = null;
        boolean designing = false;
        ArrayList<ArrayList<Character>> design = new ArrayList<>();
        HashMap<Character, Material> types = new HashMap<>();
        HashMap<Character,Tag<Material>> tagTypes = new HashMap<>();
        HashMap<String, String> config = new HashMap<>();
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
                            stargate.getStargateLogger().log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Unknown symbol '" + symbol + "' in diagram");
                            return null;
                        }

                        row.add(symbol);
                    }

                    design.add(row);
                    continue;
                }

                if ((line.isEmpty()) || (!line.contains("="))) {
                    designing = true;
                    continue;
                }

                String[] split = line.split("=");
                String key = split[0].trim();
                String value = split[1].trim();

                if (key.length() != 1) {
                    config.put(key, value);
                    continue;
                }

                Character symbol = key.charAt(0);
                // Load material from string
                // Load tag from string
                if(value.startsWith("#")) {
                	Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.fromString(value), Material.class);
                	if(tag != null) {
                		tagTypes.put(symbol, tag);
                		continue;
                	}
                }
                else {
                	Material id = Material.getMaterial(value);
                    if (id != null) {
                    	types.put(symbol, id);
                        continue;
                    }
                }
                
                throw new Exception("Invalid material or tag in line: " + line);
                
            }
        } catch (Exception ex) {
            stargate.getStargateLogger().log(Level.SEVERE, "Could not load Gate " + file.getName() + " - " + ex.getMessage());
            return null;
        } finally {
            if (scanner != null) scanner.close();
        }

        Character[][] layout = new Character[design.size()][cols];

        for (int y = 0; y < design.size(); y++) {
            ArrayList<Character> row = design.get(y);
            Character[] result = new Character[cols];

            int rowSize = row.size();

            for (int x = 0; x < cols; x++) {
                result[x] = (x < rowSize) ? row.get(x) : ' ';
            }

            layout[y] = result;
        }

        Gate gate = new Gate(stargate, file.getName(), layout, types, tagTypes);

        gate.portalBlockOpen = readConfig(stargate, config, file, "portal-open", gate.portalBlockOpen);
        gate.portalBlockClosed = readConfig(stargate, config, file, "portal-closed", gate.portalBlockClosed);
        gate.useCost = readConfig(stargate, config, file, "usecost", -1);
        gate.destroyCost = readConfig(stargate, config, file, "destroycost", -1);
        gate.createCost = readConfig(stargate, config, file, "createcost", -1);
        gate.toOwner = (config.containsKey("toowner") ? Boolean.parseBoolean(config.get("toowner")) : stargate.getEconomyHandler().isToOwner());

        if (gate.getControls().length != 2) {
            stargate.getStargateLogger().log(Level.SEVERE, "Could not load Gate " + file.getName() + " - Gates must have exactly 2 control points.");
            return null;
        }

        gate.save(file.getParent() + "/"); // Updates format for version changes
        return gate;
    }

    private static int readConfig(Stargate stargate, HashMap<String, String> config, File file, String key, int def) {
        if (config.containsKey(key)) {
            try {
                return Integer.parseInt(config.get(key));
            } catch (NumberFormatException ex) {
                stargate.getStargateLogger().log(Level.WARNING, String.format("%s reading %s: %s is not numeric", ex.getClass().getName(), file, key));
            }
        }

        return def;
    }

    private static Material readConfig(Stargate stargate, HashMap<String, String> config, File file, String key, Material def) {
        if (config.containsKey(key)) {
            Material mat = Material.getMaterial(config.get(key));
            if (mat != null) return mat;
            stargate.getStargateLogger().log(Level.WARNING, String.format("Error reading %s: %s is not a material", file, key));
        }

        return def;
    }

    public static void loadGates(Stargate stargate, String gateFolder) {
        File dir = new File(gateFolder);
        File[] files = dir.exists() ? dir.listFiles(new StargateFilenameFilter()) : new File[0];

        if (files == null || files.length == 0) {
            if (dir.mkdir()) {
                populateDefaults(stargate, gateFolder);
            }

            return;
        }

        for (File file : files) {
            Gate gate = loadGate(stargate, file);
            if (gate != null) registerGate(gate);
        }
    }

    private static void populateDefaults(Stargate stargate, String gateFolder) {
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

        Gate netherGate = new Gate(stargate, "nethergate.gate", layout, types, new HashMap<Character,Tag<Material>>());
        netherGate.save(gateFolder);
        registerGate(netherGate);
        Stargate.debug("Gate.populateDefaults"," created a nether gate");
        
        types.put(ENTRANCE, Material.WATER);
        types.put(EXIT, Material.WATER);
        types.put(ANYTHING, Material.WATER);
        types.put('X', Material.SEA_LANTERN);
        types.put('-', Material.SEA_LANTERN);

        Gate waterGate = new Gate(stargate, "water.gate", layout, types, new HashMap<Character,Tag<Material>>());
        waterGate.setPortalBlockClosed(Material.WATER);
        waterGate.setPortalBlockOpen(Material.KELP_PLANT);
        waterGate.save(gateFolder);
        registerGate(waterGate);
        Stargate.debug("Gate.populateDefaults"," created a water gate");
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

    static class StargateFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".gate");
        }
    }

    public static void clearGates() {
        gates.clear();
        controlBlocks.clear();
    }

}
