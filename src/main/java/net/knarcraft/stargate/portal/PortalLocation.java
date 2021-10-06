package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import org.bukkit.block.BlockFace;

/**
 * Keeps track of location related data for a portal
 */
public class PortalLocation {

    private BlockLocation topLeft;
    private int modX;
    private int modZ;
    private float yaw;
    private BlockLocation signLocation;
    private RelativeBlockVector buttonVector;
    private BlockFace buttonFacing;

    /**
     * Gets the top-left block of the portal
     *
     * @return <p>The top-left block of the portal</p>
     */
    public BlockLocation getTopLeft() {
        return topLeft;
    }

    /**
     * Gets the x-modifier for the portal
     *
     * @return <p>The x-modifier for the portal</p>
     */
    public int getModX() {
        return modX;
    }

    /**
     * Gets the z-modifier for the portal
     *
     * @return <p>The z-modifier for the portal</p>
     */
    public int getModZ() {
        return modZ;
    }

    /**
     * Gets the yaw for looking outwards from the portal
     *
     * @return <p>The portal's yaw</p>
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Gets the location of the portal's sign
     * @return <p>The location of the portal's sign</p>
     */
    public BlockLocation getSignLocation() {
        return signLocation;
    }

    /**
     * The relative block vector pointing to the portal's button
     * @return <p>The relative location of the portal's button</p>
     */
    public RelativeBlockVector getButtonVector() {
        return buttonVector;
    }

    /**
     * Gets the block face determining the button's direction
     * @return <p>The button's block face</p>
     */
    public BlockFace getButtonFacing() {
        return buttonFacing;
    }

    /**
     * Sets the portal's top-left location
     *
     * <p>Assuming the portal is a square, the top-left block is the top-left block when looking at the portal at the
     * side with the portal's sign.</p>
     *
     * @param topLeft <p>The new top-left block of the portal's square structure</p>
     * @return <p>The portal location Object</p>
     */
    public PortalLocation setTopLeft(BlockLocation topLeft) {
        this.topLeft = topLeft;
        return this;
    }

    /**
     * Sets the portal's x-modifier
     *
     * @param modX <p>The portal's new x-modifier</p>
     * @return <p>The portal location Object</p>
     */
    public PortalLocation setModX(int modX) {
        this.modX = modX;
        return this;
    }

    /**
     * Sets the portal's z-modifier
     *
     * @param modZ <p>The portal's new z-modifier</p>
     * @return <p>The portal location Object</p>
     */
    public PortalLocation setModZ(int modZ) {
        this.modZ = modZ;
        return this;
    }

    /**
     * Sets the portal's yaw
     *
     * <p>The portal's yaw is the yaw a player would get when looking directly out from the portal</p>
     *
     * @param yaw <p>The portal's new yaw</p>
     * @return <p>The portal location Object</p>
     */
    public PortalLocation setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    /**
     * Sets the location of the portal's sign
     * @param signLocation <p>The new sign location</p>
     * @return <p>The portal location Object</p>
     */
    public PortalLocation setSignLocation(BlockLocation signLocation) {
        this.signLocation = signLocation;
        return this;
    }

    /**
     * Sets the relative location of the portal's button
     * @param buttonVector <p>The new relative button location</p>
     * @return <p>The portal location Object</p>
     */
    public PortalLocation setButtonVector(RelativeBlockVector buttonVector) {
        this.buttonVector = buttonVector;
        return this;
    }

    /**
     * Sets the block face for the direction the portal button is facing
     * @param buttonFacing <p>The new block face of the portal's button</p>
     * @return <p>The portal location Object</p>
     */
    public PortalLocation setButtonFacing(BlockFace buttonFacing) {
        this.buttonFacing = buttonFacing;
        return this;
    }

}
