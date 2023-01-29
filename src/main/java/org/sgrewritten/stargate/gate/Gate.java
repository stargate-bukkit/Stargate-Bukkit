package org.sgrewritten.stargate.gate;

import com.google.common.base.Preconditions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.api.formatting.LanguageManager;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GatePosition;
import org.sgrewritten.stargate.api.gate.control.ControlMechanism;
import org.sgrewritten.stargate.api.gate.control.GateActivationHandler;
import org.sgrewritten.stargate.api.gate.control.GateTextDisplayHandler;
import org.sgrewritten.stargate.api.gate.control.MechanismType;
import org.sgrewritten.stargate.api.gate.structure.GateStructureType;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.gate.control.AlwaysOnControlMechanism;
import org.sgrewritten.stargate.gate.control.ButtonControlMechanism;
import org.sgrewritten.stargate.gate.control.SignControlMechanism;
import org.sgrewritten.stargate.network.portal.PortalData;
import org.sgrewritten.stargate.util.ButtonHelper;
import org.sgrewritten.stargate.util.ClassConditionsHelper;
import org.sgrewritten.stargate.vectorlogic.MatrixVectorOperation;
import org.sgrewritten.stargate.vectorlogic.VectorOperation;

import java.util.*;
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
    private final List<GatePosition> portalPositions = new ArrayList<>();
    private final Map<MechanismType, ControlMechanism> controlMechanisms = new HashMap<>();
    private final BlockFace facing;
    private boolean isOpen = false;
    private boolean flipped;
    private final @NotNull RegistryAPI registry;
    private final @NotNull LanguageManager languageManager;


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
    public Gate(@NotNull GateFormat format, @NotNull Location signLocation, @NotNull BlockFace signFace, boolean alwaysOn, @NotNull RegistryAPI registry, @NotNull LanguageManager languageManager) throws InvalidStructureException, GateConflictException {
        Objects.requireNonNull(signLocation);
        this.format = Objects.requireNonNull(format);
        this.registry = Objects.requireNonNull(registry);
        this.languageManager = Objects.requireNonNull(languageManager);
        facing = Objects.requireNonNull(signFace);
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
     * @param portalData <p> Data of the portal </p>
     * @throws InvalidStructureException <p>If the facing is invalid or if no format could be found</p>
     */
    public Gate(@NotNull PortalData portalData, @NotNull RegistryAPI registry, @NotNull LanguageManager languageManager) throws InvalidStructureException {
        GateFormat format = GateFormatHandler.getFormat(portalData.gateFileName);
        this.languageManager = Objects.requireNonNull(languageManager);
        if (format == null) {
            Stargate.log(Level.WARNING, String.format("Could not find the format ''%s''. Check the full startup " + "log for more information", portalData.gateFileName));
            throw new InvalidStructureException("Could not find a matching gate-format");
        }
        this.topLeft = portalData.topLeft;
        this.converter = new MatrixVectorOperation(portalData.facing);
        this.converter.setFlipZAxis(portalData.flipZ);
        this.format = format;
        this.facing = portalData.facing;
        this.flipped = portalData.flipZ;
        this.registry = Preconditions.checkNotNull(registry);
    }

    @Override
    public List<GatePosition> getPortalPositions() {
        return new ArrayList<>(this.portalPositions);
    }

    @Override
    public List<BlockLocation> getLocations(GateStructureType structureType) {
        List<BlockLocation> output = new ArrayList<>();

        if (structureType == GateStructureType.CONTROL_BLOCK) {
            //Only give the locations of control-blocks in use
            for (GatePosition position : portalPositions) {
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
        GateStructureType targetType = GateStructureType.IRIS;
        List<BlockLocation> locations = getLocations(targetType);
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

        List<MechanismType> assignedMechanismTypes = new ArrayList<>();

        for (BlockVector blockVector : getFormat().getControlBlocks()) {
            Material material = getLocation(blockVector).getBlock().getType();
            if (!isControl(material)) {
                continue;
            }

            if (Tag.WALL_SIGNS.isTagged(material) && !assignedMechanismTypes.contains(MechanismType.SIGN)) {
                SignControlMechanism signMechanism = new SignControlMechanism(blockVector, this, languageManager);
                portalPositions.add(signMechanism);
                this.setPortalControlMechanism(signMechanism);
                assignedMechanismTypes.add(signMechanism.getType());
                continue;
            }
            if (!alwaysOn && !assignedMechanismTypes.contains(MechanismType.BUTTON)) {
                ButtonControlMechanism buttonMechanism = new ButtonControlMechanism(blockVector, this);
                portalPositions.add(buttonMechanism);
                this.setPortalControlMechanism(buttonMechanism);
                assignedMechanismTypes.add(buttonMechanism.getType());
            }
        }

        if (alwaysOn) {
            this.setPortalControlMechanism(new AlwaysOnControlMechanism());
        }
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
        List<GatePosition> portalPositions = this.getPortalPositions();
        for (GatePosition portalPosition : portalPositions) {
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
    public void addPortalPosition(GatePosition pos) {
        this.portalPositions.add(pos);
    }

    /**
     * Add portal positions specific for this Gate. Also assigns them as control mechanisms, if they are an instance
     * of {@link ControlMechanism}
     *
     * @param portalPositions <p> A list of portalPositions </p>
     */
    public void addPortalPositions(List<GatePosition> portalPositions) {
        this.portalPositions.addAll(portalPositions);
        for (GatePosition portalPosition : portalPositions) {
            if (portalPosition instanceof ControlMechanism controlMechanism) {
                this.setPortalControlMechanism(controlMechanism);
            }
        }
    }

    @Override
    public void setPortalControlMechanism(@NotNull ControlMechanism mechanism) {
        if (mechanism.getType() == MechanismType.SIGN) {
            ClassConditionsHelper.assertInstanceOf(GateTextDisplayHandler.class, mechanism);
        }
        if (mechanism.getType() == MechanismType.BUTTON) {
            ClassConditionsHelper.assertInstanceOf(GateActivationHandler.class, mechanism);
        }
        controlMechanisms.put(mechanism.getType(), mechanism);
    }

    @Override
    public @Nullable ControlMechanism getPortalControlMechanism(@NotNull MechanismType type) {
        return controlMechanisms.get(type);
    }

    @Override
    public GatePosition getPortalPosition(@NotNull Location location) {
        // TODO Auto-generated method stub
        return null;
    }
}
