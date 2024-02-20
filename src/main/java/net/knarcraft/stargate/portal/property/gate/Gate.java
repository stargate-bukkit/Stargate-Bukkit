package net.knarcraft.stargate.portal.property.gate;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
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
    private final Map<Character, Tag<Material>> characterTagMap;

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
     * @param characterTagMap      <p>The material tag types the different layout characters represent</p>
     * @param portalOpenBlock      <p>The material to set the opening to when the portal is open</p>
     * @param portalClosedBlock    <p>The material to set the opening to when the portal is closed</p>
     * @param portalButton         <p>The material to use for the portal button</p>
     * @param useCost              <p>The cost of using a portal with this gate layout (-1 to disable)</p>
     * @param createCost           <p>The cost of creating a portal with this gate layout (-1 to disable)</p>
     * @param destroyCost          <p>The cost of destroying a portal with this gate layout (-1 to disable)</p>
     * @param toOwner              <p>Whether any payment should go to the owner of the gate, as opposed to just disappearing</p>
     */
    public Gate(@NotNull String filename, @NotNull GateLayout layout,
                @NotNull Map<Character, Material> characterMaterialMap,
                @NotNull Map<Character, Tag<Material>> characterTagMap, @NotNull Material portalOpenBlock,
                @NotNull Material portalClosedBlock, @NotNull Material portalButton, int useCost, int createCost,
                int destroyCost, boolean toOwner) {
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
        this.characterTagMap = characterTagMap;
    }

    /**
     * Gets this gate's layout
     *
     * @return <p>This gate's layout</p>
     */
    @NotNull
    public GateLayout getLayout() {
        return layout;
    }

    /**
     * Gets a copy of the character to material mapping for this gate
     *
     * @return <p>The character to material map</p>
     */
    @NotNull
    public Map<Character, Material> getCharacterMaterialMap() {
        return new HashMap<>(characterMaterialMap);
    }

    /**
     * Checks whether the given material is valid for control blocks
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material is valid for control blocks</p>
     */
    public boolean isValidControlBlock(@NotNull Material material) {
        return (getControlBlock() != null) ? getControlBlock().equals(material) :
                getControlBlockTag().isTagged(material);
    }

    /**
     * Gets the material tag used for this gate's control blocks
     *
     * @return <p>The material tag type used for control blocks</p>
     */
    @NotNull
    public Tag<Material> getControlBlockTag() {
        return characterTagMap.get(GateHandler.getControlBlockCharacter());
    }

    /**
     * Gets the material type used for this gate's control blocks
     *
     * @return <p>The material type used for control blocks</p>
     */
    @Nullable
    public Material getControlBlock() {
        return characterMaterialMap.get(GateHandler.getControlBlockCharacter());
    }

    /**
     * Gets the filename of this gate's file
     *
     * @return <p>The filename of this gate's file</p>
     */
    @NotNull
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the block type to use for the opening when a portal using this gate is open
     *
     * @return <p>The block type to use for the opening when open</p>
     */
    @NotNull
    public Material getPortalOpenBlock() {
        return portalOpenBlock;
    }

    /**
     * Gets the block type to use for the opening when a portal using this gate is closed
     *
     * @return <p>The block type to use for the opening when closed</p>
     */
    @NotNull
    public Material getPortalClosedBlock() {
        return portalClosedBlock;
    }

    /**
     * Gets the material to use for a portal's button if using this gate type
     *
     * @return <p>The material to use for a portal's button if using this gate type</p>
     */
    @NotNull
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
    @NotNull
    public Integer getCreateCost() {
        return createCost < 0 ? Stargate.getEconomyConfig().getDefaultCreateCost() : createCost;
    }

    /**
     * Gets the cost of destroying a portal with this gate
     *
     * @return <p>The cost of destroying a portal with this gate</p>
     */
    @NotNull
    public Integer getDestroyCost() {
        return destroyCost < 0 ? Stargate.getEconomyConfig().getDefaultDestroyCost() : destroyCost;
    }

    /**
     * Gets whether portal payments go to this portal's owner
     *
     * @return <p>Whether portal payments go to the owner</p>
     */
    @NotNull
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
    public boolean matches(@NotNull BlockLocation topLeft, double yaw) {
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
    public boolean matches(@NotNull BlockLocation topLeft, double yaw, boolean onCreate) {
        return verifyGateEntrancesMatch(topLeft, yaw, onCreate) && verifyGateBorderMatches(topLeft, yaw);
    }

    /**
     * Verifies that all border blocks of a portal matches this gate type
     *
     * @param topLeft <p>The top-left block of the portal</p>
     * @param yaw     <p>The yaw when looking directly outwards from the portal</p>
     * @return <p>True if all border blocks of the gate match the layout</p>
     */
    private boolean verifyGateBorderMatches(@NotNull BlockLocation topLeft, double yaw) {
        Map<Character, Material> characterMaterialMap = new HashMap<>(this.characterMaterialMap);
        Map<Character, Tag<Material>> characterTagMap = new HashMap<>(this.characterTagMap);
        for (RelativeBlockVector borderVector : layout.getBorder()) {
            int rowIndex = borderVector.right();
            int lineIndex = borderVector.down();
            Character key = layout.getLayout()[lineIndex][rowIndex];

            Material materialInLayout = characterMaterialMap.get(key);
            Tag<Material> tagInLayout = characterTagMap.get(key);
            Material materialAtLocation = topLeft.getRelativeLocation(borderVector, yaw).getType();

            if (materialInLayout != null) {
                if (materialAtLocation != materialInLayout) {
                    Stargate.debug("Gate::Matches", String.format("Block Type Mismatch: %s != %s",
                            materialAtLocation, materialInLayout));
                    return false;
                }
            } else if (tagInLayout != null) {
                if (!tagInLayout.isTagged(materialAtLocation)) {
                    Stargate.debug("Gate::Matches", String.format("Block Type Mismatch: %s != %s",
                            materialAtLocation, tagInLayout));
                    return false;
                }
            } else {
                /* This generally should not happen with proper checking, but just in case a material character is not
                 * recognized, but still allowed in previous checks, verify the gate as long as all such instances of
                 * the character correspond to the same material in the physical gate. All subsequent gates will also
                 * need to match the first verified gate. */
                characterMaterialMap.put(key, materialAtLocation);
                Stargate.debug("Gate::Matches", String.format("Missing layout material in %s. Using %s from the" +
                        " physical portal.", getFilename(), materialAtLocation));
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
    private boolean verifyGateEntrancesMatch(@NotNull BlockLocation topLeft, double yaw, boolean onCreate) {
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
    public void save(@NotNull String gateFolder) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(gateFolder, filename)));

            //Save main material names
            writeConfig(bufferedWriter, "portal-open", portalOpenBlock.name());
            writeConfig(bufferedWriter, "portal-closed", portalClosedBlock.name());
            writeConfig(bufferedWriter, "button", portalButton.name());

            //Save the values necessary for economy
            saveEconomyValues(bufferedWriter);

            //Store material types to use for frame blocks
            saveFrameBlockType(bufferedWriter);

            bufferedWriter.newLine();

            //Save the gate layout
            layout.saveLayout(bufferedWriter);

            bufferedWriter.close();
        } catch (IOException exception) {
            Stargate.logSevere(String.format("Could not save Gate %s - %s", filename, exception.getMessage()));
        }
    }

    /**
     * Saves current economy related values using a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write to</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void saveEconomyValues(@NotNull BufferedWriter bufferedWriter) throws IOException {
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
    private void saveFrameBlockType(@NotNull BufferedWriter bufferedWriter) throws IOException {
        for (Map.Entry<Character, Material> entry : this.characterMaterialMap.entrySet()) {
            Character key = entry.getKey();
            //Skip characters not part of the frame
            if (key.equals(GateHandler.getAnythingCharacter()) ||
                    key.equals(GateHandler.getEntranceCharacter()) ||
                    key.equals(GateHandler.getExitCharacter())) {
                continue;
            }
            saveFrameBlockType(key, entry.getValue().toString(), bufferedWriter);
        }
        for (Map.Entry<Character, Tag<Material>> entry : this.characterTagMap.entrySet()) {
            saveFrameBlockType(entry.getKey(), "#" + entry.getValue().getKey().toString().replaceFirst(
                    "minecraft:", ""), bufferedWriter);
        }
    }

    /**
     * Saves a type of block used for the gate frame/border using a buffered writer
     *
     * @param key            <p>The character key to store</p>
     * @param value          <p>The string value to store</p>
     * @param bufferedWriter <p>The buffered writer to write to</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void saveFrameBlockType(@NotNull Character key, @Nullable String value,
                                    @NotNull BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.append(key.toString());
        bufferedWriter.append('=');
        if (value != null) {
            bufferedWriter.append(value);
        }
        bufferedWriter.newLine();
    }

    /**
     * Writes a formatted string to a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write the formatted string to</p>
     * @param key            <p>The config key to save</p>
     * @param value          <p>The config value to save</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    private void writeConfig(@NotNull BufferedWriter bufferedWriter, @NotNull String key,
                             @NotNull Object value) throws IOException {
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
