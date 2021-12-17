package net.TheDgtl.Stargate.gate;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.StargateLogger;
import net.TheDgtl.Stargate.actions.BlockSetAction;
import net.TheDgtl.Stargate.exception.GateConflictException;
import net.TheDgtl.Stargate.exception.InvalidStructureException;
import net.TheDgtl.Stargate.gate.structure.GateStructureType;
import net.TheDgtl.Stargate.network.portal.BlockLocation;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.vectorlogic.VectorOperation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.Set;
import java.util.logging.Level;

/**
 * Acts as an interface for portals to modify worlds
 *
 * @author Thorin
 */
public class Gate {

    private GateFormat format;
    private final VectorOperation converter;
    private Location topLeft;
    private BlockVector signPosition;
    private BlockVector buttonPosition;
    private final BlockFace facing;
    private boolean isOpen = false;
    private Set<PortalFlag> flags;

    private static final Material DEFAULT_BUTTON = Material.STONE_BUTTON;
    private static final Material DEFAULT_WATER_BUTTON = Material.DEAD_TUBE_CORAL_WALL_FAN;

    /**
     * Instantiates a new gate
     *
     * @param format       <p>The gate format used by this gate</p>
     * @param signLocation <p>The location of this gate's sign</p>
     * @param signFace     <p>The direction this gate's sign is facing</p>
     * @param portal       <p>The portal using this gate</p>
     * @throws InvalidStructureException <p>If the physical stargate at the given location does not match the given format</p>
     * @throws GateConflictException     <p>If this gate is in conflict with an existing one</p>
     */
    public Gate(GateFormat format, Location signLocation, BlockFace signFace, Set<PortalFlag> flags)
            throws InvalidStructureException, GateConflictException {
        this.setFormat(format);
        facing = signFace;
        converter = new VectorOperation(signFace, Stargate.getInstance());
        this.flags = flags;

        //Allow mirroring for non-symmetrical gates
        if (matchesFormat(signLocation)) {
            return;
        }
        converter.setFlipZAxis(true);
        if (matchesFormat(signLocation)) {
            return;
        }

        throw new InvalidStructureException();
    }

    /**
     * Instantiates a gate from already predetermined parameters, no checking is done to see if format matches
     * 
     * @param topLeft       <p>The location of the origin of the gate</p>
     * @param facing        <p>The facing of the gate</p>
     * @param zFlip         <p>If the gateFormat is fliped in the z-axis</p>
     * @param gateDesignName    <p>The filename of the design for this gate</p>
     * @param portal        <p>The portal using this gate</p>
     * @throws InvalidStructureException    <p>If the facing is invalid</p>
     */
    public Gate(Location topLeft, BlockFace facing, boolean zFlip, String gateDesignName, Set<PortalFlag> flags, StargateLogger logger)
            throws InvalidStructureException {
        this.facing = facing;
        this.topLeft = topLeft;
        this.converter = new VectorOperation(facing, logger);
        this.converter.setFlipZAxis(zFlip);
        GateFormat format = GateFormat.getFormat(gateDesignName);
        this.setFormat(format);
        this.flags = flags;
    }
    
    /**
     * Set button and draw sign
     *
     * @param signLines an array with 4 elements, representing each line of a sign
     */
    public void drawControlMechanisms(String[] signLines, boolean drawButton) {
        drawSign(signLines);
        if (drawButton) {
            drawButton();
        }
    }

    /**
     * Draws this gate's sign
     *
     * @param signLines <p>The lines to draw on the sign</p>
     */
    private void drawSign(String[] signLines) {
        Location signLocation = getLocation(signPosition);
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

    /**
     * Draws this gate's button
     */
    private void drawButton() {
        Location buttonLocation = getLocation(buttonPosition);
        Material buttonMaterial = getButtonMaterial();
        Directional buttonData = (Directional) Bukkit.createBlockData(buttonMaterial);
        buttonData.setFacing(facing);

        buttonLocation.getBlock().setBlockData(buttonData);
    }

    /**
     * Gets the block face of this gate's sign
     *
     * @return <p>The block face of this gate's sign</p>
     */
    public BlockFace getSignFace() {
        return this.facing;
    }

    /**
     * Gets the location of this gate's sign
     *
     * @return <p>The location of this gate's sign</p>
     */
    public Location getSignLocation() {
        return getLocation(signPosition);
    }

    /**
     * Gets all locations of this gate containing the given structure type
     *
     * @param structureType <p>The structure type to get locations of</p>
     * @return <p>All locations containing the given structure type</p>
     */
    public List<BlockLocation> getLocations(GateStructureType structureType) {
        List<BlockLocation> output = new ArrayList<>();

        for (BlockVector vec : getFormat().getStructure(structureType).getStructureTypePositions()) {
            Location loc = getLocation(vec);
            output.add(new BlockLocation(loc));
        }

        if (structureType == GateStructureType.CONTROL_BLOCK && flags.contains(PortalFlag.ALWAYS_ON)) {
            Location buttonLoc = getLocation(buttonPosition);
            output.remove(new BlockLocation(buttonLoc));
        }
        return output;
    }

    /**
     * Opens this gate
     */
    public void open() {
        changeOpenState(true);
    }

    /**
     * Closes this gate
     */
    public void close() {
        changeOpenState(false);
    }

    /**
     * Gets the exit location of this gate
     *
     * @return <p>The exit location of this gate</p>
     */
    public Location getExit() {
        BlockVector formatExit = getFormat().getExit();
        return getLocation(formatExit);
    }

    /**
     * Gets whether this gate is currently open
     *
     * @return <p>Whether this gate is currently open</p>
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Sets whether this gate is currently open
     *
     * @param isOpen <p>Whether this gate is currently open</p>
     */
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * Gets the gate format used by this gate
     *
     * @return <p>The gate format used by this gate</p>
     */
    public GateFormat getFormat() {
        return format;
    }

    /**
     * Sets the gate format used by this gate
     *
     * @param format <p>The gate format used by this gate</p>
     */
    public void setFormat(GateFormat format) {
        this.format = format;
    }

    /**
     * Gets the block face defining this gate's direction
     *
     * @return <p>The block face defining this gate's direction</p>
     */
    public BlockFace getFacing() {
        return converter.getFacing();
    }

    /**
     * Gets a vector relative to this gate's top-left location using the given location
     *
     * @param location <p>The location to turn into a relative location</p>
     * @return <p>A location relative to this gate's top-left location</p>
     */
    public Vector getRelativeVector(Location location) {
        Vector vector = topLeft.clone().subtract(location).toVector();
        return converter.performOperation(vector);
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

    /**
     * Gets a location from a relative vector
     *
     * @param vector <p>The vector defining a location</p>
     * @return <p>The location corresponding to the given vector</p>
     */
    private Location getLocation(@NotNull Vector vector) {
        return topLeft.clone().add(converter.performInverseOperation(vector));
    }

    /**
     * Checks if the built stargate matches this gate's format
     *
     * <p>This will try to match the format regardless of which control block the sign was placed on</p>
     * <p>
     * TODO: symmetric formats will be checked twice, make a way to determine if a format is symmetric to avoid this
     *
     * @param location <p>The top-left location of a built stargate</p>
     * @return <p>True if the built stargate matches this format</p>
     * @throws GateConflictException <p>If the built stargate conflicts with another gate</p>
     */
    private boolean matchesFormat(Location location) throws GateConflictException {
        List<BlockVector> controlBlocks = getFormat().getControlBlocks();
        for (BlockVector controlBlock : controlBlocks) {
            /*
             * Top-left is origin for the format, everything becomes easier if you calculate
             * this position in the world; this is a hypothetical position, calculated from
             * the position of the sign minus a vector of a hypothetical sign position in
             * format.
             */
            topLeft = location.clone().subtract(converter.performInverseOperation(controlBlock));

            if (getFormat().matches(converter, topLeft)) {
                if (isGateConflict()) {
                    throw new GateConflictException();
                }
                /*
                 * Just a cheat to exclude the sign location, and determine the position of the
                 * button. Note that this will have weird behaviour if there's more than 3
                 * control-blocks
                 */
                signPosition = controlBlock;
                for (BlockVector buttonVec : getFormat().getControlBlocks()) {
                    if (signPosition == buttonVec)
                        continue;
                    buttonPosition = buttonVec;
                    break;
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this gate is in conflict with another
     *
     * @return <p>True if there is a conflict</p>
     */
    private boolean isGateConflict() {
        List<BlockLocation> locations = this.getLocations(GateStructureType.FRAME);
        for (BlockLocation loc : locations) {
            if (Stargate.factory.getPortal(loc, GateStructureType.values()) != null) {
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

    public Location getTopLeft() {
        return this.topLeft;
    }

}
