package net.knarcraft.stargate.portal.property;

import net.knarcraft.stargate.container.BlockLocation;
import net.knarcraft.stargate.container.RelativeBlockVector;
import org.bukkit.Axis;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Keeps track of location related data for a portal
 */
@SuppressWarnings("UnusedReturnValue")
public class PortalLocation {

    private BlockLocation topLeft;
    private float yaw;
    private BlockLocation signLocation;
    private RelativeBlockVector buttonVector;
    private BlockFace buttonFacing;

    /**
     * Gets the top-left block of the portal
     *
     * @return <p>The top-left block of the portal</p>
     */
    @NotNull
    public BlockLocation getTopLeft() {
        return topLeft;
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
     *
     * @return <p>The location of the portal's sign</p>
     */
    @NotNull
    public BlockLocation getSignLocation() {
        return signLocation;
    }

    /**
     * The relative block vector pointing to the portal's button
     *
     * @return <p>The relative location of the portal's button</p>
     */
    @Nullable
    public RelativeBlockVector getButtonVector() {
        return buttonVector;
    }

    /**
     * Gets the block face determining the button's direction
     *
     * @return <p>The button's block face</p>
     */
    @NotNull
    public BlockFace getButtonFacing() {
        return buttonFacing;
    }

    /**
     * Gets the rotation axis, which is the axis along which the gate is placed
     *
     * <p>The portal's rotation axis is the cross axis of the button's axis</p>
     *
     * @return <p>The portal's rotation axis</p>
     */
    @NotNull
    public Axis getRotationAxis() {
        return getYaw() == 0.0F || getYaw() == 180.0F ? Axis.X : Axis.Z;
    }

    /**
     * Gets the world this portal resides in
     *
     * @return <p>The world this portal resides in</p>
     */
    @Nullable
    public World getWorld() {
        return topLeft.getWorld();
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
    @NotNull
    public PortalLocation setTopLeft(@NotNull BlockLocation topLeft) {
        this.topLeft = topLeft;
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
    @NotNull
    public PortalLocation setYaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    /**
     * Sets the location of the portal's sign
     *
     * @param signLocation <p>The new sign location</p>
     * @return <p>The portal location Object</p>
     */
    @NotNull
    public PortalLocation setSignLocation(@NotNull BlockLocation signLocation) {
        this.signLocation = signLocation;
        return this;
    }

    /**
     * Sets the relative location of the portal's button
     *
     * @param buttonVector <p>The new relative button location</p>
     * @return <p>The portal location Object</p>
     */
    @NotNull
    public PortalLocation setButtonVector(@Nullable RelativeBlockVector buttonVector) {
        this.buttonVector = buttonVector;
        return this;
    }

    /**
     * Sets the block face for the direction the portal button is facing
     *
     * @param buttonFacing <p>The new block face of the portal's button</p>
     * @return <p>The portal location Object</p>
     */
    @NotNull
    public PortalLocation setButtonFacing(@NotNull BlockFace buttonFacing) {
        this.buttonFacing = buttonFacing;
        return this;
    }

}
