package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.action.BlockSetAction;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.PortalPosition;
import net.TheDgtl.Stargate.network.portal.PositionType;
import net.TheDgtl.Stargate.vectorlogic.MatrixVectorOperation;
import net.TheDgtl.Stargate.vectorlogic.VectorOperation;
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
    private final StargateLogger logger;
    private boolean isOpen = false;
    private boolean flipped;

    private static final Material DEFAULT_BUTTON = Material.STONE_BUTTON;
    private static final Material DEFAULT_WATER_BUTTON = Material.DEAD_TUBE_CORAL_WALL_FAN;

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
    public Gate(GateFormat format, Location signLocation, BlockFace signFace, boolean alwaysOn, StargateLogger logger)
            throws InvalidStructureException, GateConflictException {
        this.format = format;
        this.logger = logger;
        facing = signFace;
        converter = new MatrixVectorOperation(signFace, Stargate.getInstance());

        //Allow mirroring for non-symmetrical gates
        if (matchesFormat(signLocation, alwaysOn)) {
            return;
        }
        converter.setFlipZAxis(true);
        flipped = true;
        if (matchesFormat(signLocation, alwaysOn)) {
            return;
        }

        throw new InvalidStructureException();
    }

    /**
     * Instantiates a gate from already predetermined parameters, no checking is done to see if format matches
     *
     * @param topLeft <p>The location of the origin of the gate</p>
     * @param facing  <p>The facing of the gate</p>
     * @param flipZ   <p>If the gateFormat is flipped in the z-axis</p>
     * @param format  <p>The gate format used by this gate</p>
     * @throws InvalidStructureException <p>If the facing is invalid</p>
     */
    public Gate(Location topLeft, BlockFace facing, boolean flipZ, GateFormat format, StargateLogger logger) throws InvalidStructureException {
        this.facing = facing;
        this.topLeft = topLeft;
        this.logger = logger;
        this.converter = new MatrixVectorOperation(facing, logger);
        this.converter.setFlipZAxis(flipZ);
        this.format = format;
        this.flipped = flipZ;
    }

    @Override
    public void drawControlMechanisms(String[] signLines, boolean drawButton) {
        drawSigns(signLines);
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
            Stargate.syncTickPopulator.addAction(new BlockSetAction(sign, true));
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
            if (Tag.BUTTONS.isTagged(blockType) || Tag.WALL_CORALS.isTagged(blockType)) {
                continue;
            }
            Material buttonMaterial = getButtonMaterial();
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
     * Gets the button material to use for this gate
     *
     * @return <p>The button material to use for this gate</p>
     */
    private Material getButtonMaterial() {
        Material portalClosedMaterial = getFormat().getIrisMaterial(false);
        //TODO: Add support for using solid blocks as the gate-closed material for underwater portals
        switch (portalClosedMaterial) {
            case AIR:
                return DEFAULT_BUTTON;
            case WATER:
                return DEFAULT_WATER_BUTTON;
            default:
                Stargate.log(Level.INFO, portalClosedMaterial.name() +
                        " is currently not supported as a portal closed material");
                return DEFAULT_BUTTON;
        }
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
                    gateway.setExitLocation(block.getWorld().getSpawnLocation());
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
     * @param location <p>The top-left location of a built stargate</p>
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
             * hypothetical sign position in format.
             */
            topLeft = location.clone().subtract(converter.performToRealSpaceOperation(controlBlock));

            if (getFormat().matches(converter, topLeft)) {
                if (hasGateFrameConflict()) {
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
            } else if (!alwaysOn && (Tag.BUTTONS.isTagged(material) || Tag.WALL_CORALS.isTagged(material))) {
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
        return Tag.WALL_SIGNS.isTagged(material) || Tag.BUTTONS.isTagged(material) ||
                Tag.WALL_CORALS.isTagged(material);
    }

    /**
     * Checks if this gate is in conflict with another
     *
     * @return <p>True if there is a conflict</p>
     */
    private boolean hasGateFrameConflict() {
        RegistryAPI registryAPI = Stargate.getRegistry();
        List<BlockLocation> frameLocations = this.getLocations(GateStructureType.FRAME);
        for (BlockLocation blockLocation : frameLocations) {
            if (registryAPI.getPortal(blockLocation, GateStructureType.values()) != null) {
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
        RegistryAPI registryAPI = Stargate.getRegistry();
        //TODO: If we allow add-ons to add new controls after creation, this should be expanded to all control blocks
        List<PortalPosition> portalPositions = this.getPortalPositions();
        for (PortalPosition portalPosition : portalPositions) {
            BlockLocation positionLocation = new BlockLocation(getLocation(portalPosition.getPositionLocation()));
            if (registryAPI.getPortal(positionLocation, GateStructureType.CONTROL_BLOCK) != null) {
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
        logger.logMessage(Level.FINEST, String.format("Addding portalposition %s with relative position %s", type.toString(), relativeBlockVector));
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
