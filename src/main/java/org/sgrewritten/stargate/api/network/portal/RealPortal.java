package org.sgrewritten.stargate.api.network.portal;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.network.portal.PositionType;

import java.util.List;

/**
 * A real portal with a physical sign that is located on this server
 */
@SuppressWarnings("unused")
public interface RealPortal extends Portal {

    /**
     * Draws any control mechanisms belonging to this portal
     *
     * <p>This basically just re-draws the portal's sign or whichever other mechanisms the portal may use.</p>
     */
    void drawControlMechanisms();

    /**
     * Updates the color of this portal's sign
     *
     * <p>Replacement function for {@link Sign#setColor(org.bukkit.DyeColor)}, as the portal sign is an interface that
     * is using a combination of various colors; more has to be processed</p>
     *
     * @param color <p>Color to change the sign text to. If null, then the default color will be used</p>
     */
    void setSignColor(DyeColor color);

    /**
     * The action to be run when this portal's button is clicked
     *
     * @param event <p>The player interact event that triggered the button click</p>
     */
    void onButtonClick(PlayerInteractEvent event);

    /**
     * The action to be triggered if this portal sign is interacted with
     *
     * @param event <p>The triggered player interact event</p>
     */
    void onSignClick(PlayerInteractEvent event);


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
     * Set metadata for this portal
     *
     * @param data <p> The meta data to set </p>
     */
    void setMetaData(String data);

    /**
     * Get metadata for this portal
     *
     * @return <p> The meta data of this portal </p>
     */
    String getMetaData();

    /**
     * Get the facing entities exit from this portal.
     *
     * @return <p> The facing entities exit from this portal. </p>
     */
    BlockFace getExitFacing();


}
