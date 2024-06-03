package org.sgrewritten.stargate.gate;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
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
import org.sgrewritten.stargate.api.event.portal.StargateSignFormatPortalEvent;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.gate.GateFormatAPI;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.gate.structure.GateFormatStructureType;
import org.sgrewritten.stargate.api.gate.structure.GateStructure;
import org.sgrewritten.stargate.api.network.RegistryAPI;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.formatting.LineFormatter;
import org.sgrewritten.stargate.api.network.portal.formatting.SignLine;
import org.sgrewritten.stargate.api.network.portal.formatting.StargateComponentDeserialiser;
import org.sgrewritten.stargate.api.network.portal.formatting.data.LineData;
import org.sgrewritten.stargate.api.vectorlogic.MatrixVectorOperation;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;
import org.sgrewritten.stargate.exception.GateConflictException;
import org.sgrewritten.stargate.exception.InvalidStructureException;
import org.sgrewritten.stargate.manager.BlockDropManager;
import org.sgrewritten.stargate.network.portal.portaldata.GateData;
import org.sgrewritten.stargate.property.NonLegacyClass;
import org.sgrewritten.stargate.property.StargateConstant;
import org.sgrewritten.stargate.thread.task.StargateRegionTask;
import org.sgrewritten.stargate.util.ButtonHelper;
import org.sgrewritten.stargate.util.VectorUtils;

import java.util.*;
import java.util.logging.Level;

/**
 * Acts as an interface for portals to modify worlds
 *
 * @author Thorin
 */
public class Gate implements GateAPI {

    private final @NotNull GateFormatAPI format;
    private final VectorOperation converter;
    private Location topLeft;
    private final Set<PortalPosition> portalPositions = new HashSet<>();
    private final BlockFace facing;
    private boolean isOpen = false;
    private boolean flipped;
    private final @NotNull RegistryAPI registry;
    private RealPortal portal;


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
    public Gate(@NotNull GateFormatAPI format, @NotNull Location signLocation, BlockFace signFace, boolean alwaysOn, @NotNull RegistryAPI registry)
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
        this.topLeft = gateData.topLeft();
        this.converter = new MatrixVectorOperation(gateData.facing());
        this.converter.setFlipZAxis(gateData.flipZ());
        this.format = Objects.requireNonNull(gateData.gateFormat());
        this.facing = gateData.facing();
        this.flipped = gateData.flipZ();
        this.registry = Preconditions.checkNotNull(registry);
    }

    @Override
    public void drawControlMechanisms(LineData[] lines) {
        portalPositions.forEach(portalPosition -> this.redrawPosition(portalPosition, lines));
    }

    @Override
    public void redrawPosition(PortalPosition portalPosition, @Nullable LineData[] lines) {
        if (portalPosition.getPositionType() == PositionType.SIGN) {
            drawSign(portalPosition, Objects.requireNonNull(lines));
        } else if (portalPosition.getPositionType() == PositionType.BUTTON) {
            drawButton(portalPosition);
        }
    }

    @Override
    public List<PortalPosition> getPortalPositions() {
        return new ArrayList<>(this.portalPositions);
    }

    /**
     * Draws this gate's signs
     *
     * @param lineData <p>The lines to draw on the sign</p>
     */
    private void drawSign(PortalPosition portalPosition, final LineData[] lineData) {
        if(!(portalPosition.getAttachment() instanceof LineFormatter lineFormatter)){
            throw new IllegalArgumentException("Expected attachment to be instance of line formatter");
        }
        SignLine[] signLines = lineFormatter.formatLineData(lineData);
        Location signLocation = getLocation(portalPosition.getRelativePositionLocation());
        new StargateRegionTask(signLocation) {
            @Override
            public void run() {
                Stargate.log(Level.FINER, "Drawing sign at location " + signLocation);
                BlockState signState = signLocation.getBlock().getState();
                if (!(signState instanceof Sign sign)) {
                    Stargate.log(Level.FINE, "Could not find sign at position " + signLocation);
                    return;
                }
                StargateSignFormatPortalEvent event = new StargateSignFormatPortalEvent(portal, signLines, portalPosition, signLocation);
                Bukkit.getPluginManager().callEvent(event);
                SignLine[] newSignLines = event.getLines();
                setSignLines(sign, newSignLines);
                sign.update();
            }
        }.runNow();
    }

    private void setSignLines(Sign sign, SignLine[] signLines) {
        for (int i = 0; i < 4; i++) {
            if (NonLegacyClass.COMPONENT.isImplemented()) {
                Component line = StargateComponentDeserialiser.getComponent(signLines[i]);
                sign.line(i, line);
            } else {
                String line = StargateComponentDeserialiser.getLegacyText(signLines[i]);
                sign.setLine(i, line);
            }
        }
    }

    /**
     * Draws this gate's button
     */
    private void drawButton(PortalPosition portalPosition) {
        Location buttonLocation = getLocation(portalPosition.getRelativePositionLocation());
        new StargateRegionTask(buttonLocation) {
            @Override
            public void run() {
                Material blockType = buttonLocation.getBlock().getType();
                if (ButtonHelper.isButton(blockType)) {
                    return;
                }
                Material buttonMaterial = ButtonHelper.getButtonMaterial(buttonLocation);
                Stargate.log(Level.FINEST, "buttonMaterial: " + buttonMaterial);
                Directional buttonData = (Directional) Bukkit.createBlockData(buttonMaterial);
                buttonData.setFacing(facing);

                buttonLocation.getBlock().setBlockData(buttonData);
                BlockDropManager.disableBlockDrops(buttonLocation.getBlock());
            }
        }.runNow();
    }

    /**
     * @param type <p> The type of portal position</p>
     * @return <p>Portal positions of specified type controlled by this plugin</p>
     */
    private List<PortalPosition> getActivePortalPositions(PositionType type) {
        List<PortalPosition> output = new ArrayList<>();
        Stargate.log(Level.FINEST, "Checking active portal positions");
        for (PortalPosition portalPosition : getPortalPositions()) {
            if (portalPosition.getPositionType() != type || !portalPosition.isActive()) {
                Stargate.log(Level.FINEST, type.name() + ":" + portalPosition.getPositionType() + ", " + portalPosition.isActive() + ", " + portalPosition.getRelativePositionLocation());
                continue;
            }
            if (portalPosition.getPluginName().equals(StargateConstant.STARGATE_NAME)) {
                Stargate.log(Level.FINEST, "Found, " + type.name() + " at " + portalPosition.getRelativePositionLocation());
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
    public @NotNull GateFormatAPI getFormat() {
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
            new StargateRegionTask(block.getLocation()) {
                @Override
                public void run() {
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
            }.runNow();
        }
    }

    @Override
    public Location getLocation(@NotNull Vector vector) {
        return VectorUtils.getLocation(topLeft, converter, vector);
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
            try {
                calculatePortalPositions(alwaysOn);
            } catch (InvalidStructureException e) {
                continue;
            }
            if (isValid()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid() throws GateConflictException {
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

    @Override
    public void calculatePortalPositions(boolean alwaysOn) throws InvalidStructureException {
        //First find buttons and signs on the Stargate
        List<BlockVector> registeredControls = findExistingPortalPositions(alwaysOn);

        //Return if no button is necessary
        if (alwaysOn) {
            return;
        }
        //Return if a button has already been registered
        for (PortalPosition portalPosition : portalPositions) {
            if (portalPosition.getPositionType() == PositionType.BUTTON) {
                Stargate.log(Level.FINEST, "A portal position of type BUTTON has already been registered; no generation of a button is necessary");
                return;
            }
        }
        //Add a button to the first available control block
        boolean hasRegisteredAButton = false;
        for (BlockVector buttonVector : getFormat().getControlBlocks()) {
            if (!registeredControls.contains(buttonVector)) {
                portalPositions.add(new PortalPosition(PositionType.BUTTON, buttonVector, StargateConstant.STARGATE_NAME));
                hasRegisteredAButton = true;
                break;
            }
        }

        if (!hasRegisteredAButton) {
            throw new InvalidStructureException("Could not find a button position");
        }
    }

    /**
     * Gets positions for any controls built on the Stargate
     *
     * @return <p>The vectors found containing controls</p>
     */
    private List<BlockVector> findExistingPortalPositions(boolean alwaysOn) {
        List<BlockVector> foundVectors = new ArrayList<>();
        for (BlockVector blockVector : getFormat().getControlBlocks()) {
            Material material = getLocation(blockVector).getBlock().getType();
            if (!isControl(material)) {
                continue;
            }

            if (Tag.WALL_SIGNS.isTagged(material)) {
                portalPositions.add(new PortalPosition(PositionType.SIGN, blockVector, StargateConstant.STARGATE_NAME));
                Stargate.log(Level.FINEST, "Adding a SIGN at " + blockVector);
            } else if (!alwaysOn && ButtonHelper.isButton(material)) {
                portalPositions.add(new PortalPosition(PositionType.BUTTON, blockVector, StargateConstant.STARGATE_NAME));
                Stargate.log(Level.FINEST, "Adding a BUTTON at " + blockVector);
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
        for (PortalPosition portalPosition : this.getPortalPositions()) {
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
        Stargate.log(Level.FINEST, String.format("Adding portal position %s with relative position %s", portalPosition.getPositionType(), portalPosition.getRelativePositionLocation()));
        this.portalPositions.add(portalPosition);
    }

    @Override
    public @Nullable PortalPosition removePortalPosition(Location location) {
        BlockVector relativeBlockVector = this.getRelativeVector(location).toBlockVector();
        for (PortalPosition portalPosition : this.portalPositions) {
            if (portalPosition.getRelativePositionLocation().equals(relativeBlockVector)) {
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

    @Override
    public void forceGenerateStructure() {
        for (GateFormatStructureType type : GateFormatStructureType.values()) {
            GateStructure structure = getFormat().getStructure(type);
            structure.generateStructure(converter, topLeft);
        }
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

    @Override
    public void assignPortal(@NotNull RealPortal realPortal) {
        if (this.portal != null) {
            throw new IllegalStateException("A gate can only be assigned to a portal once.");
        }
        this.portal = Objects.requireNonNull(realPortal);
        for (PortalPosition portalPosition : this.portalPositions) {
            portalPosition.assignPortal(realPortal);
        }
    }

    @Override
    public RealPortal getPortal() {
        return this.portal;
    }
}
