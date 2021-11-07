package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.property.PortalLocation;
import net.knarcraft.stargate.utility.PermissionHelper;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

/**
 * The portal sign drawer draws the sing of a given portal
 */
public class PortalSignDrawer {

    private final Portal portal;
    private final static ChatColor errorColor = ChatColor.DARK_RED;
    private final static ChatColor freeColor = ChatColor.DARK_GREEN;
    private static ChatColor mainColor;
    private static ChatColor highlightColor;

    /**
     * Instantiates a new portal sign drawer
     *
     * @param portal <p>The portal whose sign this portal sign drawer is responsible for drawing</p>
     */
    public PortalSignDrawer(Portal portal) {
        this.portal = portal;
    }

    /**
     * Sets the main sign color
     *
     * <p>The main sign color is used for most text on the sign, while the highlighting color is used for the markings
     * around portal names and network names ('>','<','-',')','(')</p>
     *
     * @param newMainColor      <p>The new main sign color</p>
     * @param newHighlightColor <p>The new highlight color</p>
     */
    public static void setColors(ChatColor newMainColor, ChatColor newHighlightColor) {
        mainColor = newMainColor;
        highlightColor = newHighlightColor;
    }

    /**
     * Draws the sign of the portal this sign drawer is responsible for
     */
    public void drawSign() {
        Sign sign = getSign();
        if (sign == null) {
            return;
        }

        drawSign(sign);
    }

    /**
     * Gets the sign for this sign drawer's portal
     *
     * @return <p>The sign of this sign drawer's portal</p>
     */
    private Sign getSign() {
        Block signBlock = portal.getSignLocation().getBlock();
        BlockState state = signBlock.getState();
        if (!(state instanceof Sign sign)) {
            if (!portal.getOptions().hasNoSign()) {
                Stargate.logWarning("Sign block is not a Sign object");
                Stargate.debug("Portal::drawSign", String.format("Block: %s @ %s", signBlock.getType(),
                        signBlock.getLocation()));
            }
            return null;
        }
        return sign;
    }

    /**
     * Draws the sign of the portal this sign drawer is responsible for
     *
     * @param sign <p>The sign re-draw</p>
     */
    private void drawSign(Sign sign) {
        //Clear sign
        clearSign(sign);
        setLine(sign, 0, highlightColor + "-" + mainColor +
                portal.getName() + highlightColor + "-");

        if (!portal.getPortalActivator().isActive()) {
            //Default sign text
            drawInactiveSign(sign);
        } else {
            if (portal.getOptions().isBungee()) {
                //Bungee sign
                drawBungeeSign(sign);
            } else if (portal.getOptions().isFixed()) {
                //Sign pointing at one other portal
                drawFixedSign(sign);
            } else {
                //Networking stuff
                drawNetworkSign(sign);
            }
        }

        sign.update();
    }

    /**
     * Clears all lines of a sign, but does not update the sign
     *
     * @param sign <p>The sign to clear</p>
     */
    private void clearSign(Sign sign) {
        for (int index = 0; index <= 3; index++) {
            sign.setLine(index, "");
        }
    }

    /**
     * Marks this sign drawer's portal as unregistered
     */
    public void drawUnregisteredSign() {
        Sign sign = getSign();
        if (sign == null) {
            return;
        }
        clearSign(sign);
        sign.setLine(0, portal.getName());
        sign.update();
    }

    /**
     * Draws a sign with choose-able network locations
     *
     * @param sign <p>The sign to re-draw</p>
     */
    private void drawNetworkSign(Sign sign) {
        PortalActivator destinations = portal.getPortalActivator();
        int maxIndex = destinations.getDestinations().size() - 1;
        int signLineIndex = 0;
        int destinationIndex = destinations.getDestinations().indexOf(portal.getDestinationName());
        boolean freeGatesGreen = Stargate.getEconomyConfig().useEconomy() &&
                Stargate.getEconomyConfig().drawFreePortalsGreen();

        //Last, and not only entry. Draw the entry two back
        if ((destinationIndex == maxIndex) && (maxIndex > 1)) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex - 2);
        }
        //Not first entry. Draw the previous entry
        if (destinationIndex > 0) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex - 1);
        }
        //Draw the chosen entry (line 2 or 3)
        drawNetworkSignChosenLine(freeGatesGreen, sign, ++signLineIndex);
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 1)) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex + 1);
        }
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 2) && (++signLineIndex <= 3)) {
            drawNetworkSignLine(freeGatesGreen, sign, signLineIndex, destinationIndex + 2);
        }
    }

    /**
     * Draws the chosen destination on one sign line
     *
     * @param freeGatesGreen <p>Whether to display free gates in a green color</p>
     * @param sign           <p>The sign to draw on</p>
     * @param signLineIndex  <p>The line to draw on</p>
     */
    private void drawNetworkSignChosenLine(boolean freeGatesGreen, Sign sign, int signLineIndex) {
        if (freeGatesGreen) {
            Portal destination = PortalHandler.getByName(portal.getDestinationName(), portal.getNetwork());
            boolean green = PermissionHelper.isFree(portal.getActivePlayer(), portal, destination);
            ChatColor nameColor = (green ? freeColor : highlightColor);
            setLine(sign, signLineIndex, nameColor + ">" + (green ? freeColor : mainColor) +
                    portal.getDestinationName() + nameColor + "<");
        } else {
            setLine(sign, signLineIndex, highlightColor + ">" + mainColor + portal.getDestinationName() +
                    highlightColor + "<");
        }
    }

    /**
     * Sets a line on a sign, adding the chosen sign color
     *
     * @param sign  <p>The sign to update</p>
     * @param index <p>The index of the sign line to change</p>
     * @param text  <p>The new text on the sign</p>
     */
    public void setLine(Sign sign, int index, String text) {
        sign.setLine(index, mainColor + text);
    }

    /**
     * Draws one network destination on one sign line
     *
     * @param freeGatesGreen   <p>Whether to display free gates in a green color</p>
     * @param sign             <p>The sign to draw on</p>
     * @param signLineIndex    <p>The line to draw on</p>
     * @param destinationIndex <p>The index of the destination to draw</p>
     */
    private void drawNetworkSignLine(boolean freeGatesGreen, Sign sign, int signLineIndex, int destinationIndex) {
        PortalActivator destinations = portal.getPortalActivator();
        String destinationName = destinations.getDestinations().get(destinationIndex);
        if (freeGatesGreen) {
            Portal destination = PortalHandler.getByName(destinationName, portal.getNetwork());
            boolean green = PermissionHelper.isFree(portal.getActivePlayer(), portal, destination);
            setLine(sign, signLineIndex, (green ? freeColor : mainColor) + destinationName);
        } else {
            setLine(sign, signLineIndex, mainColor + destinationName);
        }
    }

    /**
     * Draws the sign of a BungeeCord portal
     *
     * @param sign <p>The sign to re-draw</p>
     */
    private void drawBungeeSign(Sign sign) {
        setLine(sign, 1, Stargate.getString("bungeeSign"));
        setLine(sign, 2, highlightColor + ">" + mainColor + portal.getDestinationName() + highlightColor + "<");
        setLine(sign, 3, highlightColor + "[" + mainColor + portal.getNetwork() + highlightColor + "]");
    }

    /**
     * Draws the sign of an in-active portal
     *
     * <p>The sign for an in-active portal should display the right-click prompt and the network.</p>
     *
     * @param sign <p>The sign to re-draw</p>
     */
    private void drawInactiveSign(Sign sign) {
        setLine(sign, 1, Stargate.getString("signRightClick"));
        setLine(sign, 2, Stargate.getString("signToUse"));
        if (!portal.getOptions().isNoNetwork()) {
            setLine(sign, 3, highlightColor + "(" + mainColor + portal.getNetwork() + highlightColor + ")");
        } else {
            setLine(sign, 3, "");
        }
    }

    /**
     * Draws a sign pointing to a fixed location
     *
     * @param sign <p>The sign to re-draw</p>
     */
    private void drawFixedSign(Sign sign) {
        String destinationName = portal.getOptions().isRandom() ? Stargate.getString("signRandom") :
                portal.getDestinationName();
        setLine(sign, 1, highlightColor + ">" + mainColor + destinationName + highlightColor + "<");

        if (portal.getOptions().isNoNetwork()) {
            setLine(sign, 2, "");
        } else {
            setLine(sign, 2, highlightColor + "(" + mainColor + portal.getNetwork() + highlightColor + ")");
        }
        Portal destination = PortalHandler.getByName(portal.getDestinationName(), portal.getNetwork());
        if (destination == null && !portal.getOptions().isRandom()) {
            setLine(sign, 3, errorColor + Stargate.getString("signDisconnected"));
        } else {
            setLine(sign, 3, "");
        }
    }

    /**
     * Marks a portal with an invalid gate by changing its sign and writing to the console
     *
     * @param portalLocation <p>The location of the portal with an invalid gate</p>
     * @param gateName       <p>The name of the invalid gate type</p>
     * @param lineIndex      <p>The index of the line the invalid portal was found at</p>
     */
    public static void markPortalWithInvalidGate(PortalLocation portalLocation, String gateName, int lineIndex) {
        Sign sign = (Sign) portalLocation.getSignLocation().getBlock().getState();
        sign.setLine(3, errorColor + Stargate.getString("signInvalidGate"));
        sign.update();

        Stargate.logInfo(String.format("Gate layout on line %d does not exist [%s]", lineIndex, gateName));
    }

}
