package org.sgrewritten.stargate.gate;

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
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.StargateLogger;
import org.sgrewritten.stargate.action.BlockSetAction;
import org.sgrewritten.stargate.action.SupplierAction;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.gate.structure.GateStructureType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.BlockLocation;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.network.portal.PositionType;
import org.sgrewritten.stargate.util.ButtonHelper;
import org.sgrewritten.stargate.vectorlogic.MatrixVectorOperation;
import org.sgrewritten.stargate.vectorlogic.VectorOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Acts as an interface for portals to modify worlds
 *
 * @author Thorin
 */
public class Gate implements GateAPI {

    private final GateFormat format;
    private final VectorOperation converter;
    private Location topLeft;
    private final List<PortalPosition> portalPositions = new ArrayList<>();
    private final BlockFace facing;
    private boolean isOpen = false;
    private boolean flipped;
    private RegistryAPI registry;


    /**
     * Instantiates a new gate
     *
     * @param format       <p>The gate format used by this gate</p>
     * @param signLocation <p>The location of this gate's sign</p>
     * @param signFace     <p>The direction this gate's sign is facing</p>
     * @param alwaysOn     <p>Whether this gate has been set as always-on</p>
     * @param logger       <p>A stargate logger object</p>
     * @throws InvalidStructureException <p>If the physical stargate at the given location does not match the given format</p>
     * @throws GateConflictException     <p>If this gate is in conflict with an existing one</p>
     */
    public Gate(GateFormat format, Location signLocation, BlockFace signFace, boolean alwaysOn, RegistryAPI registry)
            throws InvalidStructureException, GateConflictException {
        this.format = format;
        this.registry = registry;
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

        throw new InvalidStructureException("Format does not match with signlocatino in world");
    }

    /**
     * Instantiates a gate from already predetermined parameters, no checking is done to see if format matches
     *
     * @param portalData <p> Data of the portal </p>
     * @param logger     <p> A logger </p>
     * @throws InvalidStructureException <p>If the facing is invalid or if no format could be found</p>
     */
    public Gate(PortalData portalData) throws InvalidStructureException {

        GateFormat format = GateFormatHandler.getFormat(portalData.gateFileName);
        if (format == null) {
            Stargate.log(Level.WARNING, String.format("Could not find the format ''%s''. Check the full startup " +
                    "log for more information", portalData.gateFileName));
            throw new InvalidStructureException("Could not find a matching gateformat");
        }
        this.topLeft = portalData.topLeft;
        this.converter = new MatrixVectorOperation(portalData.facing);
        this.converter.setFlipZAxis(portalData.flipZ);
        this.format = format;
        this.facing = portalData.facing;
        this.flipped = portalData.flipZ;
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
        for (PortalPosition portalPosition : portalPositions) {
            if (portalPosition.getPositionType() != PositionType.SIGN) {
                continue;
            }
            Location signLocation = getLocation(portalPosition.getPositionLocation());
            BlockState signState = signLocation.getBlock().getState();
            if (!(signState instanceof Sign)) {
                Stargate.log(Level.FINE, "Could not find sign at position " + signLocation);
                return;
            }

            Sign sign = (Sign) signState;
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
        for (PortalPosition portalPosition : portalPositions) {
            if (portalPosition.getPositionType() != PositionType.BUTTON) {
                continue;
            }

            Location buttonLocation = getLocation(portalPosition.getPositionLocation());
            Material blockType = buttonLocation.getBlock().getType();
            if (ButtonHelper.isButton(blockType)) {
                continue;
            }
            Material buttonMaterial = ButtonHelper.getButtonMaterial(getFormat().getIrisMaterial(false));
            Directional buttonData = (Directional) Bukkit.createBlockData(buttonMaterial);
            buttonData.setFacing(facing);

            buttonLocation.getBlock().setBlockData(buttonData);
        }
    }

    @Override
    public List<BlockLocation> getLocations(GateStructureType structureType) {
        List<BlockLocation> output = new ArrayList<>();

        if (structureType == GateStructureType.CONTROL_BLOCK) {
            //Only give the locations of control-blocks in use
            for (PortalPosition position : portalPositions) {
                output.add(new BlockLocation(getLocation(position.getPositionLocation())));
            }
        } else {
            //Get all locations from the format
            for (BlockVector structurePositionVector : getFormat().getStructure(structureType).getStructureTypePositions()) {
                Location structureLocation = getLocation(structurePositionVector);
                output.add(new BlockLocation(structureLocation));
            }
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
    public GateFormat getFormat() {
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
        GateStructureType targetType = GateStructureType.IRIS;
        List<BlockLocation> locations = getLocations(targetType);
        BlockData blockData = Bukkit.createBlockData(material);

        if (blockData instanceof Orientable) {
            Orientable orientation = (Orientable) blockData;
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

            //Calculate all relevant portal positions
            calculatePortalPositions(alwaysOn);

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
                return;
            }
        }

        //Add a button to the first available control block
        for (BlockVector buttonVector : getFormat().getControlBlocks()) {
            if (registeredControls.contains(buttonVector)) {
                continue;
            }
            portalPositions.add(new PortalPosition(PositionType.BUTTON, buttonVector));
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
                portalPositions.add(new PortalPosition(PositionType.SIGN, blockVector));
            } else if (!alwaysOn && ButtonHelper.isButton(material)) {
                portalPositions.add(new PortalPosition(PositionType.BUTTON, blockVector));
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
        //TODO: If we allow add-ons to add new controls after creation, this should be expanded to all control blocks
        List<PortalPosition> portalPositions = this.getPortalPositions();
        for (PortalPosition portalPosition : portalPositions) {
            BlockLocation positionLocation = new BlockLocation(getLocation(portalPosition.getPositionLocation()));
            if (registry.getPortal(positionLocation, GateStructureType.CONTROL_BLOCK) != null) {
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
    public void addPortalPosition(Location location, PositionType type) {
        BlockVector relativeBlockVector = this.getRelativeVector(location).toBlockVector();
        Stargate.log(Level.FINEST, String.format("Adding portal position %s with relative position %s", type.toString(), relativeBlockVector));
        this.addPortalPosition(relativeBlockVector, type);
    }

    /**
     * Add a position specific for this Gate
     *
     * @param relativeBlockVector <p> The relative position in format space</p>
     * @param type                <p> The type of position </p>
     */
    public void addPortalPosition(BlockVector relativeBlockVector, PositionType type) {
        PortalPosition pos = new PortalPosition(type, relativeBlockVector);
        this.portalPositions.add(pos);
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
