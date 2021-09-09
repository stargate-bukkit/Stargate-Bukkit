package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.BlockLocation;
import net.knarcraft.stargate.RelativeBlockVector;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.utility.DirectionHelper;
import net.knarcraft.stargate.utility.EconomyHandler;
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
    private final Map<Character, Material> types;

    //Gate materials
    private Material portalOpenBlock;
    private Material portalClosedBlock;
    private final Material portalButton;

    // Economy information
    private final int useCost;
    private final int createCost;
    private final int destroyCost;
    private final boolean toOwner;

    /**
     * Instantiates a new gate
     *
     * @param filename          <p>The name of the gate which equal the name of the file</p>
     * @param layout            <p>The character layout defined in the gate file</p>
     * @param types             <p>The block types the different layout characters represent</p>
     * @param portalOpenBlock   <p>The material to set the non-frame to when the portal is open</p>
     * @param portalClosedBlock <p>The material to set the non-frame to when the portal is closed</p>
     * @param portalButton      <p>The material to use for the portal button</p>
     * @param useCost           <p>The cost of using a portal with this gate layout (-1 to disable)</p>
     * @param createCost        <p>The cost of creating a portal with this gate layout (-1 to disable)</p>
     * @param destroyCost       <p>The cost of destroying a portal with this gate layout (-1 to disable)</p>
     * @param toOwner           <p>Whether any payment should go to the owner of the gate, as opposed to just disappearing</p>
     */
    public Gate(String filename, GateLayout layout, Map<Character, Material> types, Material portalOpenBlock,
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
    public Map<Character, Material> getTypes() {
        return types;
    }

    /**
     * Gets the material type used for this gate's control blocks
     *
     * @return <p>The material type used for control blocks</p>
     */
    public Material getControlBlock() {
        return types.get(GateHandler.getControlBlockCharacter());
    }

    /**
     * Gets the filename of this gate
     *
     * @return <p>The filename of this gate</p>
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the block type to use for the opening when a portal using this gate is open
     *
     * @return <p>The block type to use for the opening when open</p>
     */
    public Material getPortalOpenBlock() {
        return portalOpenBlock;
    }

    /**
     * Sets the block to use for the opening when a portal using this gate is open
     *
     * @param type <p>The block type to use for the opening when open</p>
     */
    public void setPortalOpenBlock(Material type) {
        portalOpenBlock = type;
    }

    /**
     * Gets the block type to use for the opening when a portal using this gate is closed
     *
     * @return <p>The block type to use for the opening when closed</p>
     */
    public Material getPortalClosedBlock() {
        return portalClosedBlock;
    }

    /**
     * Sets the block type to use for the opening when a portal using this gate is closed
     *
     * @param type <p>The block type to use for the opening when closed</p>
     */
    public void setPortalClosedBlock(Material type) {
        portalClosedBlock = type;
    }

    /**
     * Gets the material to use for a portal's button if using this gate type
     *
     * @return <p>The material to use for a portal's button if using this gate type</p>
     */
    public Material getPortalButton() {
        return portalButton;
    }

    /**
     * Gets the cost of using a portal with this gate
     *
     * @return <p>The cost of using a portal with this gate</p>
     */
    public int getUseCost() {
        return useCost < 0 ? EconomyHandler.getUseCost() : useCost;
    }

    /**
     * Gets the cost of creating a portal with this gate
     *
     * @return <p>The cost of creating a portal with this gate</p>
     */
    public Integer getCreateCost() {
        return createCost < 0 ? EconomyHandler.getCreateCost() : createCost;
    }

    /**
     * Gets the cost of destroying a portal with this gate
     *
     * @return <p>The cost of destroying a portal with this gate</p>
     */
    public Integer getDestroyCost() {
        if (destroyCost < 0) return EconomyHandler.getDestroyCost();
        return destroyCost;
    }

    /**
     * Gets whether portal payments go to this portal's owner
     *
     * @return <p>Whether portal payments go to the owner</p>
     */
    public Boolean getToOwner() {
        return toOwner;
    }

    /**
     * Checks whether a portal's gate matches this gate type
     *
     * @param topLeft <p>The top-left block of the portal's gate</p>
     * @param modX    <p>The x modifier used</p>
     * @param modZ    <p>The z modifier used</p>
     * @return <p>True if this gate matches the portal</p>
     */
    public boolean matches(BlockLocation topLeft, int modX, int modZ) {
        return matches(topLeft, modX, modZ, false);
    }

    /**
     * Checks whether a portal's gate matches this gate type
     *
     * @param topLeft  <p>The top-left block of the portal's gate</p>
     * @param modX     <p>The x modifier used</p>
     * @param modZ     <p>The z modifier used</p>
     * @param onCreate <p>Whether this is used in the context of creating a new gate</p>
     * @return <p>True if this gate matches the portal</p>
     */
    public boolean matches(BlockLocation topLeft, int modX, int modZ, boolean onCreate) {
        return verifyGateEntrancesMatch(topLeft, modX, modZ, onCreate) && verifyGateBorderMatches(topLeft, modX, modZ);
    }

    /**
     * Verifies that all border blocks of a portal gate matches this gate type
     *
     * @param topLeft <p>The top-left block of the portal</p>
     * @param modX    <p>The x modifier used</p>
     * @param modZ    <p>The z modifier used</p>
     * @return <p>True if all border blocks of the gate match the layout</p>
     */
    private boolean verifyGateBorderMatches(BlockLocation topLeft, int modX, int modZ) {
        Map<Character, Material> portalTypes = new HashMap<>(types);
        for (RelativeBlockVector borderVector : layout.getBorder()) {
            int rowIndex = borderVector.getRight();
            int lineIndex = borderVector.getDepth();
            Character key = layout.getLayout()[lineIndex][rowIndex];

            Material materialInLayout = portalTypes.get(key);
            Material materialAtLocation = getBlockAt(topLeft, borderVector, modX, modZ).getType();
            if (materialInLayout == null) {
                portalTypes.put(key, materialAtLocation);
            } else if (materialAtLocation != materialInLayout) {
                Stargate.debug("Gate::Matches", String.format("Block Type Mismatch: %s != %s",
                        materialAtLocation, materialInLayout));
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies that all entrances of a portal gate matches this gate type
     *
     * @param topLeft  <p>The top-left block of this portal</p>
     * @param modX     <p>The x modifier used</p>
     * @param modZ     <p>The z modifier used</p>
     * @param onCreate <p>Whether this is used in the context of creating a new gate</p>
     * @return <p>Whether this is used in the context of creating a new gate</p>
     */
    private boolean verifyGateEntrancesMatch(BlockLocation topLeft, int modX, int modZ, boolean onCreate) {
        if (Stargate.ignoreEntrance) {
            return true;
        }
        for (RelativeBlockVector entranceVector : layout.getEntrances()) {
            Material type = getBlockAt(topLeft, entranceVector, modX, modZ).getType();

            // Ignore entrance if it's air and we're creating a new gate
            if (onCreate && type == Material.AIR) {
                continue;
            }

            if (type != portalClosedBlock && type != portalOpenBlock) {
                Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the block at a relative block vector location
     *
     * @param vector <p>The relative block vector</p>
     * @return <p>The block at the given relative position</p>
     */
    private BlockLocation getBlockAt(BlockLocation topLeft, RelativeBlockVector vector, int modX, int modZ) {
        return DirectionHelper.getBlockAt(topLeft, vector, modX, modZ);
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
     *                       * @throws IOException <p>If unable to write to the buffered writer</p>
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
     * @param key            <p>The config key to save</p>
     * @param value          <p>The value of the config key</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String key, int value) throws IOException {
        writeConfig(bufferedWriter, "%s=%d", key, value);
    }

    /**
     * Writes a boolean to a config
     *
     * @param bufferedWriter <p>The buffered writer to write the config to</p>
     * @param key            <p>The config key to save</p>
     * @param value          <p>The value of the config key</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String key, boolean value) throws IOException {
        writeConfig(bufferedWriter, "%s=%b", key, value);
    }

    /**
     * Writes a string to a config
     *
     * @param bufferedWriter <p>The buffered writer to write the config to</p>
     * @param key            <p>The config key to save</p>
     * @param value          <p>The value of the config key</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String key, String value) throws IOException {
        writeConfig(bufferedWriter, "%s=%s", key, value);
    }

    /**
     * Writes a formatted string to a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write the formatted string to</p>
     * @param format         <p>The format to use</p>
     * @param key            <p>The config key to save</p>
     * @param value          <p>The config value to save</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String format, String key, Object value) throws IOException {
        bufferedWriter.append(String.format(format, key, value));
        bufferedWriter.newLine();
    }

}
