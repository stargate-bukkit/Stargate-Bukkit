package net.TheDgtl.Stargate.network.portal;

import net.TheDgtl.Stargate.gate.Gate;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * A real portal with a physical sign that is located on this server
 */
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
     * Gets the gate belonging to this portal
     *
     * @return <p>The gate belonging to this portal</p>
     */
    Gate getGate();

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
     * @param type <p> The type of portalPosition </p>
     * @return <p>The location of this portal's signs</p>
     */
    List<Location> getPortalPosition(PositionType type);

}
