package org.sgrewritten.stargate.api.network.portal;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.sgrewritten.stargate.api.gate.GateAPI;

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
     * Gets the exit location of this portal
     *
     * @return <p>The exit location of this portal</p>
     */
    Location getExit();

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
