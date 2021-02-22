package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.BlockLocation;
import net.knarcraft.stargate.EconomyHandler;
import net.knarcraft.stargate.Stargate;
import org.bukkit.Material;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A gate describes the physical structure of a stargate
 *
 * <p>While the portal class represents a portal in space, the Gate class represents the physical gate/portal entrance.</p>
 */
public class Gate {

    private final String filename;
    private final GateLayout layout;
    private final HashMap<Character, Material> types;

    //Gate materials
    private Material portalOpenBlock;
    private Material portalClosedBlock;
    private Material portalButton;

    // Economy information
    private int useCost;
    private int createCost;
    private int destroyCost;
    private boolean toOwner;

    /**
     * Instantiates a new gate
     *
     * @param filename <p>The name of the gate which equal the name of the file</p>
     * @param layout <p>The character layout defined in the gate file</p>
     * @param types <p>The block types the different layout characters represent</p>
     * @param portalOpenBlock <p>The material to set the non-frame to when the portal is open</p>
     * @param portalClosedBlock <p>The material to set the non-frame to when the portal is closed</p>
     * @param portalButton <p>The material to use for the portal button</p>
     * @param useCost <p>The cost of using a portal with this gate layout (-1 to disable)</p>
     * @param createCost <p>The cost of creating a portal with this gate layout (-1 to disable)</p>
     * @param destroyCost <p>The cost of destroying a portal with this gate layout (-1 to disable)</p>
     * @param toOwner <p>Whether any payment should go to the owner of the gate, as opposed to just disappearing</p>
     */
    public Gate(String filename, GateLayout layout, HashMap<Character, Material> types, Material portalOpenBlock,
                Material portalClosedBlock, Material portalButton, int useCost, int createCost, int destroyCost,
                boolean toOwner) {
        this.filename = filename;
        this.layout = layout;
        this.types = types;
        this.portalOpenBlock = portalOpenBlock;
        this.portalClosedBlock = portalClosedBlock;
        this.portalButton = portalButton;
        this.useCost = useCost;
        this.createCost = createCost;
        this.destroyCost = destroyCost;
        this.toOwner = toOwner;
    }

    /**
     * Gets the layout of this gate
     *
     * @return <p>The layout of this gate</p>
     */
    public GateLayout getLayout() {
        return layout;
    }

    /**
     * Gets the material types each layout character represents
     *
     * @return <p>The material types each layout character represents</p>
     */
    public HashMap<Character, Material> getTypes() {
        return types;
    }

    public Material getControlBlock() {
        return types.get('-');
    }

    public String getFilename() {
        return filename;
    }

    public Material getPortalOpenBlock() {
        return portalOpenBlock;
    }

    public void setPortalOpenBlock(Material type) {
        portalOpenBlock = type;
    }

    public Material getPortalClosedBlock() {
        return portalClosedBlock;
    }

    public void setPortalClosedBlock(Material type) {
        portalClosedBlock = type;
    }

    public Material getPortalButton() {
        return portalButton;
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

    public boolean matches(BlockLocation topLeft, int modX, int modZ) {
        return matches(topLeft, modX, modZ, false);
    }

    public boolean matches(BlockLocation topLeft, int modX, int modZ, boolean onCreate) {
        HashMap<Character, Material> portalTypes = new HashMap<>(types);
        Character[][] layout = this.layout.getLayout();
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                Character key = layout[y][x];

                if (key.equals(GateHandler.getEntranceCharacter()) || key.equals(GateHandler.getExitCharacter())) {
                    if (Stargate.ignoreEntrance) {
                        continue;
                    }

                    Material type = topLeft.modRelative(x, y, 0, modX, 1, modZ).getType();

                    // Ignore entrance if it's air and we're creating a new gate
                    if (onCreate && type == Material.AIR) {
                        continue;
                    }

                    if (type != portalClosedBlock && type != portalOpenBlock) {
                        Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                        return false;
                    }
                } else if (!key.equals(GateHandler.getAnythingCharacter())) {
                    Material id = portalTypes.get(key);
                    if (id == null) {
                        portalTypes.put(key, topLeft.modRelative(x, y, 0, modX, 1, modZ).getType());
                    } else if (topLeft.modRelative(x, y, 0, modX, 1, modZ).getType() != id) {
                        Stargate.debug("Gate::Matches", "Block Type Mismatch: " + topLeft.modRelative(x, y, 0, modX, 1, modZ).getType() + " != " + id);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Saves this gate to a file
     *
     * <p>This method will save the gate to its filename in the given folder.</p>
     *
     * @param gateFolder <p>The folder to save the gate file in</p>
     */
    public void save(String gateFolder) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gateFolder + filename));

            writeConfig(bufferedWriter, "portal-open", portalOpenBlock.name());
            writeConfig(bufferedWriter, "portal-closed", portalClosedBlock.name());
            writeConfig(bufferedWriter, "button", portalButton.name());

            //Save the values necessary for economy
            saveEconomyValues(bufferedWriter);

            //Store type material type to use for frame blocks
            saveFrameBlockTypes(bufferedWriter);

            bufferedWriter.newLine();

            //Save the layout
            layout.save(bufferedWriter);

            bufferedWriter.close();
        } catch (IOException ex) {
            Stargate.log.log(Level.SEVERE, "Could not save Gate " + filename + " - " + ex.getMessage());
        }
    }

    /**
     * Saves current economy related values using a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write to</p>
     *      * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void saveEconomyValues(BufferedWriter bufferedWriter) throws IOException {
        if (useCost != -1) {
            writeConfig(bufferedWriter, "usecost", useCost);
        }
        if (createCost != -1) {
            writeConfig(bufferedWriter, "createcost", createCost);
        }
        if (destroyCost != -1) {
            writeConfig(bufferedWriter, "destroycost", destroyCost);
        }
        writeConfig(bufferedWriter, "toowner", toOwner);
    }

    /**
     * Saves the types of blocks used for the gate frame/border using a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write to</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void saveFrameBlockTypes(BufferedWriter bufferedWriter) throws IOException {
        for (Map.Entry<Character, Material> entry : types.entrySet()) {
            Character type = entry.getKey();
            Material value = entry.getValue();
            // Skip control values
            if (type.equals(GateHandler.getAnythingCharacter()) ||
                    type.equals(GateHandler.getEntranceCharacter()) ||
                    type.equals(GateHandler.getExitCharacter())) {
                continue;
            }

            bufferedWriter.append(type);
            bufferedWriter.append('=');
            if (value != null) {
                bufferedWriter.append(value.toString());
            }
            bufferedWriter.newLine();
        }
    }

    /**
     * Writes an integer to a config
     *
     * @param bufferedWriter <p>The buffered writer to write the config to</p>
     * @param key <p>The config key to save</p>
     * @param value <p>The value of the config key</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String key, int value) throws IOException {
        writeConfig(bufferedWriter, "%s=%d", key, value);
    }

    /**
     * Writes a boolean to a config
     *
     * @param bufferedWriter <p>The buffered writer to write the config to</p>
     * @param key <p>The config key to save</p>
     * @param value <p>The value of the config key</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String key, boolean value) throws IOException {
        writeConfig(bufferedWriter, "%s=%b", key, value);
    }

    /**
     * Writes a string to a config
     *
     * @param bufferedWriter <p>The buffered writer to write the config to</p>
     * @param key <p>The config key to save</p>
     * @param value <p>The value of the config key</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String key, String value) throws IOException {
        writeConfig(bufferedWriter, "%s=%s", key, value);
    }

    /**
     * Writes a formatted string to a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write the formatted string to</p>
     * @param format <p>The format to use</p>
     * @param key <p>The config key to save</p>
     * @param value <p>The config value to save</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String format, String key, Object value) throws IOException {
        bufferedWriter.append(String.format(format, key, value));
        bufferedWriter.newLine();
    }

}
