package net.knarcraft.stargate.portal.property;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.property.gate.Gate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The portal structure is responsible for the physical properties of a portal
 *
 * <p>The portal structure knows which gate type is used, where the real locations of buttons, frames and entrances are
 * and whether the portal is verified.</p>
 */
public class PortalStructure {

    private final Portal portal;
    private final Gate gate;
    private BlockLocation button;
    private BlockLocation[] frame;
    private BlockLocation[] entrances;
    private boolean verified;

    /**
     * Instantiates a new portal structure
     *
     * @param portal <p>The portal whose structure to store</p>
     * @param gate   <p>The gate type used by this portal structure</p>
     * @param button <p>The real location of the portal's button</p>
     */
    public PortalStructure(@NotNull Portal portal, @NotNull Gate gate, @Nullable BlockLocation button) {
        this.portal = portal;
        this.gate = gate;
        this.verified = false;
        this.button = button;
    }

    /**
     * Gets the gate used by this portal structure
     *
     * @return <p>The gate used by this portal structure</p>
     */
    @NotNull
    public Gate getGate() {
        return gate;
    }

    /**
     * Gets the location of this portal's button
     *
     * @return <p>The location of this portal's button</p>
     */
    @Nullable
    public BlockLocation getButton() {
        return button;
    }

    /**
     * Sets the location of this portal's button
     *
     * @param button <p>The location of this portal's button</p>
     */
    public void setButton(@NotNull BlockLocation button) {
        this.button = button;
    }

    /**
     * Verifies that all control blocks in this portal follows its gate template
     *
     * @return <p>True if all control blocks were verified</p>
     */
    public boolean isVerified() {
        boolean verified = true;
        if (!Stargate.getGateConfig().verifyPortals()) {
            return true;
        }
        for (RelativeBlockVector control : gate.getLayout().getControls()) {
            verified = verified && gate.isValidControlBlock(portal.getBlockAt(control).getBlock().getType());
        }
        this.verified = verified;
        return verified;
    }

    /**
     * Gets the result of the last portal verification
     *
     * @return <p>True if this portal was verified</p>
     */
    public boolean wasVerified() {
        if (!Stargate.getGateConfig().verifyPortals()) {
            return true;
        }
        return verified;
    }

    /**
     * Checks if all blocks in a gate matches the gate template
     *
     * @return <p>True if all blocks match the gate template</p>
     */
    public boolean checkIntegrity() {
        if (Stargate.getGateConfig().verifyPortals()) {
            return gate.matches(portal.getTopLeft(), portal.getYaw());
        } else {
            return true;
        }
    }

    /**
     * Gets a list of block locations from a list of relative block vectors
     *
     * <p>The block locations will be calculated by using this portal's top-left block as the origin for the relative
     * vectors..</p>
     *
     * @param vectors <p>The relative block vectors to convert</p>
     * @return <p>A list of block locations</p>
     */
    @NotNull
    private BlockLocation[] relativeBlockVectorsToBlockLocations(@NotNull RelativeBlockVector[] vectors) {
        BlockLocation[] locations = new BlockLocation[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            locations[i] = portal.getBlockAt(vectors[i]);
        }
        return locations;
    }

    /**
     * Gets the locations of this portal's entrances
     *
     * @return <p>The locations of this portal's entrances</p>
     */
    @NotNull
    public BlockLocation[] getEntrances() {
        if (entrances == null) {
            //Get the locations of the entrances once, and only if necessary as it's an expensive operation
            entrances = relativeBlockVectorsToBlockLocations(gate.getLayout().getEntrances());
        }
        return entrances;
    }

    /**
     * Gets the locations of this portal's frame
     *
     * @return <p>The locations of this portal's frame</p>
     */
    @NotNull
    public BlockLocation[] getFrame() {
        if (frame == null) {
            //Get the locations of the frame blocks once, and only if necessary as it's an expensive operation
            frame = relativeBlockVectorsToBlockLocations(gate.getLayout().getBorder());
        }
        return frame;
    }

}
