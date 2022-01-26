package net.knarcraft.stargate.portal.property.gate;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import org.bukkit.Material;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A gate describes the physical structure of a stargate
 *
 * <p>While the portal class represents a portal in space, the Gate class represents the physical gate/portal entrance.</p>
 */
public class Gate {

    private final String filename;
    private final GateLayout layout;
    private final Map<Character, Material> characterMaterialMap;
    //Gate materials
    private final Material portalOpenBlock;
    private final Material portalClosedBlock;
    private final Material portalButton;
    //Economy information
    private final int useCost;
    private final int createCost;
    private final int destroyCost;
    private final boolean toOwner;

    /**
     * Instantiates a new gate
     *
     * @param filename             <p>The name of the gate file, including extension</p>
     * @param layout               <p>The gate layout defined in the gate file</p>
     * @param characterMaterialMap <p>The material types the different layout characters represent</p>
     * @param portalOpenBlock      <p>The material to set the opening to when the portal is open</p>
     * @param portalClosedBlock    <p>The material to set the opening to when the portal is closed</p>
     * @param portalButton         <p>The material to use for the portal button</p>
     * @param useCost              <p>The cost of using a portal with this gate layout (-1 to disable)</p>
     * @param createCost           <p>The cost of creating a portal with this gate layout (-1 to disable)</p>
     * @param destroyCost          <p>The cost of destroying a portal with this gate layout (-1 to disable)</p>
     * @param toOwner              <p>Whether any payment should go to the owner of the gate, as opposed to just disappearing</p>
     */
    public Gate(String filename, GateLayout layout, Map<Character, Material> characterMaterialMap, Material portalOpenBlock,
                Material portalClosedBlock, Material portalButton, int useCost, int createCost, int destroyCost,
                boolean toOwner) {
        this.filename = filename;
        this.layout = layout;
        this.characterMaterialMap = characterMaterialMap;
        this.portalOpenBlock = portalOpenBlock;
        this.portalClosedBlock = portalClosedBlock;
        this.portalButton = portalButton;
        this.useCost = useCost;
        this.createCost = createCost;
        this.destroyCost = destroyCost;
        this.toOwner = toOwner;
    }

    /**
     * Gets this gate's layout
     *
     * @return <p>This gate's layout</p>
     */
    public GateLayout getLayout() {
        return layout;
    }

    /**
     * Gets a copy of the character to material mapping for this gate
     *
     * @return <p>The character to material map</p>
     */
    public Map<Character, Material> getCharacterMaterialMap() {
        return new HashMap<>(characterMaterialMap);
    }

    /**
     * Gets the material type used for this gate's control blocks
     *
     * @return <p>The material type used for control blocks</p>
     */
    public Material getControlBlock() {
        return characterMaterialMap.get(GateHandler.getControlBlockCharacter());
    }

    /**
     * Gets the filename of this gate's file
     *
     * @return <p>The filename of this gate's file</p>
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
     * Gets the block type to use for the opening when a portal using this gate is closed
     *
     * @return <p>The block type to use for the opening when closed</p>
     */
    public Material getPortalClosedBlock() {
        return portalClosedBlock;
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
        return useCost < 0 ? Stargate.getEconomyConfig().getDefaultUseCost() : useCost;
    }

    /**
     * Gets the cost of creating a portal with this gate
     *
     * @return <p>The cost of creating a portal with this gate</p>
     */
    public Integer getCreateCost() {
        return createCost < 0 ? Stargate.getEconomyConfig().getDefaultCreateCost() : createCost;
    }

    /**
     * Gets the cost of destroying a portal with this gate
     *
     * @return <p>The cost of destroying a portal with this gate</p>
     */
    public Integer getDestroyCost() {
        return destroyCost < 0 ? Stargate.getEconomyConfig().getDefaultDestroyCost() : destroyCost;
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
     * @param yaw     <p>The yaw when looking directly outwards</p>
     * @return <p>True if this gate matches the portal</p>
     */
    public boolean matches(BlockLocation topLeft, double yaw) {
        return matches(topLeft, yaw, false);
    }

    /**
     * Checks whether a portal's gate matches this gate type
     *
     * <p>If enabling onCreate, opening blocks with materials AIR and WATER will be allowed even if the gate closed
     * material is a different one. If checking and onCreate is not enabled, any inconsistency with opening blocks
     * containing AIR or WATER will cause the gate to not match.</p>
     *
     * @param topLeft  <p>The top-left block of the portal's gate</p>
     * @param yaw      <p>The yaw when looking directly outwards</p>
     * @param onCreate <p>Whether this is used in the context of creating a new gate</p>
     * @return <p>True if this gate matches the portal</p>
     */
    public boolean matches(BlockLocation topLeft, double yaw, boolean onCreate) {
        return verifyGateEntrancesMatch(topLeft, yaw, onCreate) && verifyGateBorderMatches(topLeft, yaw);
    }

    /**
     * Verifies that all border blocks of a portal matches this gate type
     *
     * @param topLeft <p>The top-left block of the portal</p>
     * @param yaw     <p>The yaw when looking directly outwards from the portal</p>
     * @return <p>True if all border blocks of the gate match the layout</p>
     */
    private boolean verifyGateBorderMatches(BlockLocation topLeft, double yaw) {
        Map<Character, Material> characterMaterialMap = new HashMap<>(this.characterMaterialMap);
        for (RelativeBlockVector borderVector : layout.getBorder()) {
            int rowIndex = borderVector.getRight();
            int lineIndex = borderVector.getDown();
            Character key = layout.getLayout()[lineIndex][rowIndex];

            Material materialInLayout = characterMaterialMap.get(key);
            Material materialAtLocation = topLeft.getRelativeLocation(borderVector, yaw).getType();

            if (materialInLayout == null) {
                /* This generally should not happen with proper checking, but just in case a material character is not
                 * recognized, but still allowed in previous checks, verify the gate as long as all such instances of
                 * the character correspond to the same material in the physical gate. All subsequent gates will also
                 * need to match the first verified gate. */
                characterMaterialMap.put(key, materialAtLocation);
                Stargate.debug("Gate::Matches", String.format("Missing layout material in %s. Using %s from the" +
                        " physical portal.", getFilename(), materialAtLocation));
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
     * @param yaw      <p>The yaw when looking directly outwards</p>
     * @param onCreate <p>Whether this is used in the context of creating a new gate</p>
     * @return <p>Whether this is used in the context of creating a new gate</p>
     */
    private boolean verifyGateEntrancesMatch(BlockLocation topLeft, double yaw, boolean onCreate) {
        Stargate.debug("verifyGateEntrancesMatch", String.valueOf(topLeft));
        for (RelativeBlockVector entranceVector : layout.getEntrances()) {
            Stargate.debug("verifyGateEntrancesMatch", String.valueOf(entranceVector));
            Material type = topLeft.getRelativeLocation(entranceVector, yaw).getType();

            //Ignore entrance if it's air or water, and we're creating a new gate
            if (onCreate && (type.isAir() || type == Material.WATER)) {
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
     * Saves this gate to a file
     *
     * <p>This method will save the gate to its filename in the given folder.</p>
     *
     * @param gateFolder <p>The folder to save the gate file in</p>
     */
    public void save(String gateFolder) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gateFolder + filename));

            //Save main material names
            writeConfig(bufferedWriter, "portal-open", portalOpenBlock.name());
            writeConfig(bufferedWriter, "portal-closed", portalClosedBlock.name());
            writeConfig(bufferedWriter, "button", portalButton.name());

            //Save the values necessary for economy
            saveEconomyValues(bufferedWriter);

            //Store material types to use for frame blocks
            saveFrameBlockTypes(bufferedWriter);

            bufferedWriter.newLine();

            //Save the gate layout
            layout.saveLayout(bufferedWriter);

            bufferedWriter.close();
        } catch (IOException ex) {
            Stargate.logSevere(String.format("Could not save Gate %s - %s", filename, ex.getMessage()));
        }
    }

    /**
     * Saves current economy related values using a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write to</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void saveEconomyValues(BufferedWriter bufferedWriter) throws IOException {
        //Write use cost if not disabled
        if (useCost != -1) {
            writeConfig(bufferedWriter, "usecost", useCost);
        }
        //Write create cost if not disabled
        if (createCost != -1) {
            writeConfig(bufferedWriter, "createcost", createCost);
        }
        //Write destroy cost if not disabled
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
        for (Map.Entry<Character, Material> entry : characterMaterialMap.entrySet()) {
            Character type = entry.getKey();
            Material value = entry.getValue();
            //Skip characters not part of the frame
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
     * Writes a formatted string to a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write the formatted string to</p>
     * @param key            <p>The config key to save</p>
     * @param value          <p>The config value to save</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(BufferedWriter bufferedWriter, String key, Object value) throws IOException {
        //Figure out the correct formatting to use
        String format = "%s=";
        if (value instanceof Boolean) {
            format += "%b";
        } else if (value instanceof Integer) {
            format += "%d";
        } else if (value instanceof String) {
            format += "%s";
        } else {
            throw new IllegalArgumentException("Unrecognized config value type");
        }

        bufferedWriter.append(String.format(format, key, value));
        bufferedWriter.newLine();
    }

}
