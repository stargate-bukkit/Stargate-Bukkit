package org.sgrewritten.stargate.api.network.portal;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.MetadataHolder;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.portal.behavior.PortalBehavior;

import java.util.List;
import java.util.UUID;

/**
 * A real portal with a physical sign that is located on this server
 */
@SuppressWarnings("unused")
public interface RealPortal extends Portal, MetadataHolder {

    /**
     * Open this portal without any checks
     *
     * @param destination <p>The destination to open to</p>
     * @param actor       <p>The player which opened this portal</p>
     */
    void open(@Nullable Portal destination, @Nullable Player actor);

    /**
     * Updates the color of this portal's sign
     *
     * <p>Replacement function for {@link Sign#setColor(org.bukkit.DyeColor)}, as the portal sign is an interface that
     * is using a combination of various colors; more has to be processed</p>
     *
     * @param color <p>Color to change the sign text to. If null, then the default color will be used</p>
     */
    void setSignColor(DyeColor color, PortalPosition portalPosition);


    /**
     * Gets the gate belonging to this portal
     *
     * @return <p>The gate belonging to this portal</p>
     */
    GateAPI getGate();

    /**
     * Closes this portal
     *
     * <p>Everytime most of the portals opens, there is going to be a scheduled event to close it after a specific time.
     * If a player enters the portal before this, then it is going to close, but the scheduled close event is still
     * going to be there. And if the portal gets activated again, it is going to close prematurely, because of this
     * already scheduled event. Solution to avoid this is to assign an open-time for each scheduled close event and
     * only close if the related open time matches with the most recent time the portal was opened.</p>
     *
     * @param relatedOpenTime <p>The time this portal was opened</p>
     */
    void close(long relatedOpenTime);

    /**
     * Gets the exit location of this portal
     *
     * @return <p>The exit location of this portal</p>
     */
    Location getExit();


    /**
     * Gets the location of all positions of the specified portal position type
     *
     * @param type <p> The type of portalPosition </p>
     * @return <p>The location of this portal's signs</p>
     */
    List<Location> getPortalPosition(PositionType type);

    /**
     * @return <p>The uuid activator or null if portal is not active (or always on)</p>
     */
    @Nullable
    UUID getActivatorUUID();

    /**
     * Deactivate this portal
     */
    void deactivate();

    /**
     * Get the facing entities exit from this portal.
     *
     * @return <p> The facing entities exit from this portal. </p>
     */
    BlockFace getExitFacing();

    /**
     * @return <p>The behavior which defines this portal destination selection and sign text</p>
     */
    PortalBehavior getBehavior();

    /**
     * Modify the behavior this portal uses
     *
     * @param portalBehavior <p>New behavior this portal should follow</p>
     */
    void setBehavior(PortalBehavior portalBehavior);

    /**
     * Redraw all signs in this portal
     */
    void redrawSigns();

    /**
     * Activates this portal for the given player during internally specified time
     *
     * @param player <p>The player to activate this portal for</p>
     */
    void activate(Player player);


    /**
     * @return <p>True if this portal is active</p>
     */
    boolean isActive();

    /**
     * Teleports the given entity to given destination
     *
     * @param target <p>The entity to teleport</p>
     */
    void doTeleport(@NotNull Entity target, @Nullable Portal destination);

    /**
     * Teleports the given entity to stored destination
     *
     * @param target <p>The entity to teleport</p>
     */
    void doTeleport(@NotNull Entity target);
}
