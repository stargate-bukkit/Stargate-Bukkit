package org.sgrewritten.stargate.gate;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.action.BlockSetAction;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.util.ButtonHelper;
import org.sgrewritten.stargate.vectorlogic.MatrixVectorOperation;
import org.sgrewritten.stargate.vectorlogic.VectorOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Acts as an interface for portals to modify worlds
 *
 * @author Thorin
 */
public class Gate implements GateAPI {

    private final @NotNull GateFormat format;
    private final VectorOperation converter;
    private Location topLeft;
    private final List<PortalPosition> portalPositions = new ArrayList<>();
    private final BlockFace facing;
    private boolean isOpen = false;
    private boolean flipped;
    private final @NotNull RegistryAPI registry;


    /**
     * Instantiates a new gate
     *
     * @param format       <p>The gate format used by this gate</p>
     * @param signLocation <p>The location of this gate's sign</p>
     * @param signFace     <p>The direction this gate's sign is facing</p>
     * @param alwaysOn     <p>Whether this gate has been set as always-on</p>
     * @throws InvalidStructureException <p>If the physical stargate at the given location does not match the given format</p>
     * @throws GateConflictException     <p>If this gate is in conflict with an existing one</p>
     */
    public Gate(@NotNull GateFormat format, @NotNull Location signLocation, BlockFace signFace, boolean alwaysOn, @NotNull RegistryAPI registry)
            throws InvalidStructureException, GateConflictException {
        Objects.requireNonNull(signLocation);
        this.format = Objects.requireNonNull(format);
        this.registry = Objects.requireNonNull(registry);
        facing = signFace;
        converter = new MatrixVectorOperation(signFace);

        //Allow mirroring for non-symmetrical gates
        if (matchesFormat(signLocation, alwaysOn)) {
            return;
        }
        converter.setFlipZAxis(true);
        flipped = true;
        if (matchesFormat(signLocation, alwaysOn)) {
            return;
        }

        throw new InvalidStructureException("Format does not match with signlocation in world");
    }

    /**
     * Instantiates a gate from already predetermined parameters, no checking is done to see if format matches
     *
     * @param gateData <p> Data of the gate </p>
     * @throws InvalidStructureException <p>If the facing is invalid or if no format could be found</p>
     */
    public Gate(GateData gateData, @NotNull RegistryAPI registry) throws InvalidStructureException {
        GateFormat format = GateFormatHandler.getFormat(gateData.gateFileName());
        if (format == null) {
            Stargate.log(Level.WARNING, String.format("Could not find the format ''%s''. Check the full startup " +
                    "log for more information", gateData.gateFileName()));
            throw new InvalidStructureException("Could not find a matching gateformat");
        }
        this.topLeft = gateData.topLeft();
        this.converter = new MatrixVectorOperation(gateData.facing());
        this.converter.setFlipZAxis(gateData.flipZ());
        this.format = format;
        this.facing = gateData.facing();
        this.flipped = gateData.flipZ();
        this.registry = Preconditions.checkNotNull(registry);
    }

    @Override
    public void drawControlMechanisms(String[] signLines, boolean drawButton) {
        Stargate.addSynchronousTickAction(new SupplierAction(() -> {
            drawSigns(signLines);
            return true;
        }));

        if (drawButton) {
            drawButtons();
        }
    }

    @Override
    public List<PortalPosition> getPortalPositions() {
        return new ArrayList<>(this.portalPositions);
    }

    /**
     * Draws this gate's signs
     *
     * @param signLines <p>The lines to draw on the sign</p>
     */
    private void drawSigns(String[] signLines) {
        for (PortalPosition portalPosition : getActivePortalPositions(PositionType.SIGN)) {
            Location signLocation = getLocation(portalPosition.getRelativePositionLocation());
            BlockState signState = signLocation.getBlock().getState();
            if (!(signState instanceof Sign sign)) {
                Stargate.log(Level.FINE, "Could not find sign at position " + signLocation);
                return;
            }

            for (int i = 0; i < 4; i++) {
                sign.setLine(i, signLines[i]);
            }
            Stargate.addSynchronousTickAction(new BlockSetAction(sign, true));
        }
    }

    /**
     * Draws this gate's button
     */
    private void drawButtons() {
        for (PortalPosition portalPosition : getActivePortalPositions(PositionType.BUTTON)) {
            Location buttonLocation = getLocation(portalPosition.getRelativePositionLocation());
            Material blockType = buttonLocation.getBlock().getType();
            if (ButtonHelper.isButton(blockType)) {
                continue;
            }
            Material buttonMaterial = ButtonHelper.getButtonMaterial(getFormat().getIrisMaterial(false));
            Stargate.log(Level.FINEST, "buttonMaterial: " + buttonMaterial);
            Directional buttonData = (Directional) Bukkit.createBlockData(buttonMaterial);
            buttonData.setFacing(facing);

            buttonLocation.getBlock().setBlockData(buttonData);
        }
    }

    /**
     * @param type <p> The type of portal position</p>
     * @return <p>Portal positions of specified type controlled by this plugin</p>
     */
    private List<PortalPosition> getActivePortalPositions(PositionType type){
        List<PortalPosition> output = new ArrayList<>();
        Stargate.log(Level.FINEST, "Checking active portal positions");
        for(PortalPosition portalPosition : getPortalPositions()){
            if(portalPosition.getPositionType() != type || !portalPosition.isActive()){
                Stargate.log(Level.FINEST,type.name() + ":" + portalPosition.getPositionType() +", " + portalPosition.isActive() + ", " + portalPosition.getRelativePositionLocation());
                continue;
            }
            if(portalPosition.getPluginName().equals("Stargate")){
                Stargate.log(Level.FINEST,"Found, " + type.name() + " at " + portalPosition.getRelativePositionLocation());
                output.add(portalPosition);
            }
        }
        return output;
    }

    @Override
    public List<BlockLocation> getLocations(GateStructureType structureType) {
        List<BlockLocation> output = new ArrayList<>();
        for (BlockVector structurePositionVector : getFormat().getStructure(structureType.getGateFormatEquivalent()).getStructureTypePositions()) {
            Location structureLocation = getLocation(structurePositionVector);
            output.add(new BlockLocation(structureLocation));
        }
        return output;
    }

    @Override
    public void open() {
        changeOpenState(true);
    }

    @Override
    public void close() {
        changeOpenState(false);
    }

    @Override
    public Location getExit() {
        BlockVector formatExit = getFormat().getExit();
        return getLocation(formatExit);
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Sets whether this gate is currently open
     *
     * @param isOpen <p>Whether this gate is currently open</p>
     */
    private void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Override
    public @NotNull GateFormat getFormat() {
        return format;
    }

    @Override
    public BlockFace getFacing() {
        return converter.getFacing();
    }

    @Override
    public boolean getFlipZ() {
        return this.flipped;
    }

    @Override
    public Vector getRelativeVector(Location location) {
        Vector vector = location.clone().subtract(topLeft).toVector();
        return converter.performToAbstractSpaceOperation(vector);
    }

    /**
     * Changes the iris blocks to the given material
     *
     * <p>Note that nether portals have to be oriented in the right axis. End gateways need to be forced to a location
     * to prevent exit gateway generation.</p>
     *
     * @param material <p>The new material to use for the iris</p>
     */
    private void setIrisMaterial(Material material) {
        List<BlockLocation> locations = getLocations(GateStructureType.IRIS);
        BlockData blockData = Bukkit.createBlockData(material);

        if (blockData instanceof Orientable orientation) {
            orientation.setAxis(converter.getIrisNormal());
        }

        for (BlockLocation blockLocation : locations) {
            Block block = blockLocation.getLocation().getBlock();
            block.setBlockData(blockData);
            if (material == Material.END_GATEWAY) {// force a location to prevent exit gateway generation
                EndGateway gateway = (EndGateway) block.getState();
                // https://github.com/stargate-bukkit/Stargate-Bukkit/issues/36
                gateway.setAge(-9223372036854775808L);
                if (block.getWorld().getEnvironment() == World.Environment.THE_END) {
                    gateway.setExitLocation(block.getLocation());
                    gateway.setExactTeleport(true);
                }
                gateway.update(false, false);
            }
        }
    }

    @Override
    public Location getLocation(@NotNull Vector vector) {
        return topLeft.clone().add(converter.performToRealSpaceOperation(vector));
    }

    /**
     * Checks if the built stargate matches this gate's format
     *
     * <p>This will try to match the format regardless of which control block the sign was placed on
     * TODO: symmetric formats will be checked twice, make a way to determine if a format is symmetric to avoid this
     * </p>
     *
     * @param location <p>The location of a control-block</p>
     * @param alwaysOn <p>Whether the new portal is set as always-on</p>
     * @return <p>True if the built stargate matches this format</p>
     * @throws GateConflictException <p>If the built stargate conflicts with another gate</p>
     */
    public boolean matchesFormat(@NotNull Location location, boolean alwaysOn) throws GateConflictException {
        List<BlockVector> controlBlocks = getFormat().getControlBlocks();
        for (BlockVector controlBlock : controlBlocks) {
            /*
             * Top-left is origin for the format, everything becomes easier if you calculate this position in the world;
             * this is a hypothetical position, calculated from the position of the sign minus a vector of a
             * hypothetical sign position in format space.
             */
            topLeft = location.clone().subtract(converter.performToRealSpaceOperation(controlBlock));
            // Clear all portal positions
            portalPositions.clear();
            //Calculate all relevant portal positions
            calculatePortalPositions(alwaysOn);
            if (isValid(alwaysOn)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this gate with the current settings is valid
     *
     * @param alwaysOn <p>Whether this gate is always on</p>
     * @return <p>True if this gate is valid</p>
     * @throws GateConflictException <p>If this gate conflicts with another gate</p>
     */
    public boolean isValid(boolean alwaysOn) throws GateConflictException {
        if (getFormat().matches(converter, topLeft)) {
            if (hasGateFrameConflict(registry)) {
                throw new GateConflictException();
            }

            //Make sure no controls conflict with existing controls
            if (hasGateControlConflict()) {
                throw new GateConflictException();
            }
            return true;
        }
        return false;
    }

    /**
     * Calculates all portal positions for this gate
     *
     * @param alwaysOn <p>Whether this gate is always on</p>
     */
    private void calculatePortalPositions(boolean alwaysOn) {
        //First find buttons and signs on the Stargate
        List<BlockVector> registeredControls = getExistingControlPositions(alwaysOn);

        //Return if no button is necessary
        if (alwaysOn) {
            return;
        }
        //Return if a button has already been registered
        for (PortalPosition portalPosition : portalPositions) {
            if (portalPosition.getPositionType() == PositionType.BUTTON) {
                Stargate.log(Level.FINEST,"A portal position of type BUTTON has already been registered; no generation of a button is necessary");
                return;
            }
        }

        //Add a button to the first available control block
        for (BlockVector buttonVector : getFormat().getControlBlocks()) {
            if (registeredControls.contains(buttonVector)) {
                continue;
            }
            portalPositions.add(new PortalPosition(PositionType.BUTTON, buttonVector, "Stargate"));
            break;
        }

        //GATE_CONTROLS_FAULT
        //TODO: What to do if no available control block?
    }

    /**
     * Gets positions for any controls built on the Stargate
     *
     * @return <p>The vectors found containing controls</p>
     */
    private List<BlockVector> getExistingControlPositions(boolean alwaysOn) {
        List<BlockVector> foundVectors = new ArrayList<>();
        for (BlockVector blockVector : getFormat().getControlBlocks()) {
            Material material = getLocation(blockVector).getBlock().getType();
            if (!isControl(material)) {
                continue;
            }

            if (Tag.WALL_SIGNS.isTagged(material)) {
                portalPositions.add(new PortalPosition(PositionType.SIGN, blockVector, "Stargate"));
                Stargate.log(Level.FINEST,"Adding a SIGN at " + blockVector);
            } else if (!alwaysOn && ButtonHelper.isButton(material)) {
                portalPositions.add(new PortalPosition(PositionType.BUTTON, blockVector, "Stargate"));
                Stargate.log(Level.FINEST,"Adding a BUTTON at " + blockVector);
            }
            foundVectors.add(blockVector);
        }
        return foundVectors;
    }

    /**
     * Checks whether the given material corresponds to a control
     *
     * @param material <p>The material to check</p>
     * @return <p>True if the material corresponds to a control</p>
     */
    private boolean isControl(Material material) {
        return Tag.WALL_SIGNS.isTagged(material) || ButtonHelper.isButton(material);
    }

    /**
     * Checks if this gate is in conflict with another
     *
     * @return <p>True if there is a conflict</p>
     */
    private boolean hasGateFrameConflict(RegistryAPI registry) {
        List<BlockLocation> frameLocations = this.getLocations(GateStructureType.FRAME);
        for (BlockLocation blockLocation : frameLocations) {
            if (registry.getPortal(blockLocation, GateStructureType.values()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this gate's control blocks are in conflict with another gate's control blocks
     *
     * @return <p>True if there is a conflict</p>
     */
    private boolean hasGateControlConflict() {
        List<PortalPosition> portalPositions = this.getPortalPositions();
        for (PortalPosition portalPosition : portalPositions) {
            Location location = getLocation(portalPosition.getRelativePositionLocation());
            PortalPosition conflictingPortalPosition = registry.getPortalPosition(location);
            if (conflictingPortalPosition != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Changes the open-state of this gate
     *
     * @param open <p>Whether to open this gate, as opposed to closing this gate</p>
     */
    private void changeOpenState(boolean open) {
        Material newMaterial = getFormat().getIrisMaterial(open);
        setIrisMaterial(newMaterial);
        setOpen(open);
    }

    @Override
    public Location getTopLeft() {
        return this.topLeft;
    }

    @Override
    public PortalPosition addPortalPosition(Location location, PositionType type, String pluginName) {
        BlockVector relativeBlockVector = this.getRelativeVector(location).toBlockVector();
        Stargate.log(Level.FINEST, String.format("Adding portal position %s with relative position %s", type.toString(), relativeBlockVector));
        return this.addPortalPosition(relativeBlockVector, type, pluginName);
    }

    @Override
    public void addPortalPosition(PortalPosition portalPosition) {
        Stargate.log(Level.FINEST, String.format("Adding portal position %s with relative position %s", portalPosition.getPositionType().toString(), portalPosition.getRelativePositionLocation()));
        this.portalPositions.add(portalPosition);
    }

    @Override
    public @Nullable PortalPosition removePortalPosition(Location location) {
        BlockVector relativeBlockVector = this.getRelativeVector(location).toBlockVector();
        for(PortalPosition portalPosition : this.portalPositions){
            if(portalPosition.getRelativePositionLocation().equals(relativeBlockVector)){
                this.portalPositions.remove(portalPosition);
                return portalPosition;
            }
        }
        return null;
    }

    @Override
    public void removePortalPosition(PortalPosition portalPosition) {
        this.portalPositions.remove(portalPosition);
    }

    /**
     * Add a position specific for this Gate
     *
     * @param relativeBlockVector <p> The relative position in format space</p>
     * @param type                <p> The type of position </p>
     */
    public PortalPosition addPortalPosition(BlockVector relativeBlockVector, PositionType type, String pluginName) {
        PortalPosition pos = new PortalPosition(type, relativeBlockVector, pluginName);
        this.portalPositions.add(pos);
        return pos;
    }

    /**
     * Add portal positions specific for this Gate
     *
     * @param portalPositions <p> A list of portalPositions </p>
     */
    public void addPortalPositions(List<PortalPosition> portalPositions) {
        this.portalPositions.addAll(portalPositions);
    }
}
