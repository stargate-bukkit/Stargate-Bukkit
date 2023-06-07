package net.knarcraft.stargate.portal;

import net.knarcraft.knarlib.property.ColorConversion;
import net.knarcraft.knarlib.util.ColorHelper;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.container.SignData;
import net.knarcraft.stargate.portal.property.PortalLocation;
import net.knarcraft.stargate.utility.PermissionHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.Map;

/**
 * The portal sign drawer draws the sing of a given portal
 */
public class PortalSignDrawer {

    private final Portal portal;
    private final static ChatColor errorColor = ChatColor.DARK_RED;
    private static ChatColor freeColor;
    private static ChatColor mainColor;
    private static ChatColor highlightColor;
    private static Map<Material, ChatColor> perSignMainColors;
    private static Map<Material, ChatColor> perSignHighlightColors;

    /**
     * Instantiates a new portal sign drawer
     *
     * @param portal <p>The portal whose sign this portal sign drawer is responsible for drawing</p>
     */
    public PortalSignDrawer(Portal portal) {
        this.portal = portal;
    }

    /**
     * Sets the highlighting sign color
     *
     * <p>The highlighting color is used for the markings around portal names and network names ('>','<','-',')','(').</p>
     *
     * @param newHighlightColor <p>The new highlight color</p>
     */
    public static void setHighlightColor(ChatColor newHighlightColor) {
        highlightColor = newHighlightColor;
    }

    /**
     * Sets the main sign color
     *
     * <p>The main sign color is used for most text on the sign.</p>
     *
     * @param newMainColor <p>The new main sign color</p>
     */
    public static void setMainColor(ChatColor newMainColor) {
        mainColor = newMainColor;
    }

    /**
     * Sets the color to use for marking free stargates
     *
     * @param freeColor <p>The new color to use for marking free stargates</p>
     */
    public static void setFreeColor(ChatColor freeColor) {
        PortalSignDrawer.freeColor = freeColor;
    }

    /**
     * Sets the per-sign main colors
     *
     * @param signMainColors <p>The per-sign main colors</p>
     */
    public static void setPerSignMainColors(Map<Material, ChatColor> signMainColors) {
        PortalSignDrawer.perSignMainColors = signMainColors;
    }

    /**
     * Sets the per-sign highlight colors
     *
     * @param signHighlightColors <p>The per-sign highlight colors</p>
     */
    public static void setPerSignHighlightColors(Map<Material, ChatColor> signHighlightColors) {
        PortalSignDrawer.perSignHighlightColors = signHighlightColors;
    }

    /**
     * Gets the currently used main sign color
     *
     * @return <p>The currently used main sign color</p>
     */
    public static ChatColor getMainColor() {
        return mainColor;
    }

    /**
     * Gets the currently used highlighting sign color
     *
     * @return <p>The currently used highlighting sign color</p>
     */
    public static ChatColor getHighlightColor() {
        return highlightColor;
    }

    /**
     * Draws the sign of the portal this sign drawer is responsible for
     */
    public void drawSign() {
        Sign sign = getSign();
        if (sign == null) {
            return;
        }

        SignData signData = new SignData(sign, getMainColor(sign.getType()), getHighlightColor(sign.getType()));
        drawSign(signData);
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
     * @param signData <p>All necessary sign information</p>
     */
    private void drawSign(SignData signData) {
        Sign sign = signData.getSign();
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        //Clear sign
        clearSign(sign);
        setLine(signData, 0, highlightColor + "-" + mainColor + translateAllColorCodes(portal.getName()) +
                highlightColor + "-");

        if (!portal.getPortalActivator().isActive()) {
            //Default sign text
            drawInactiveSign(signData);
        } else {
            if (portal.getOptions().isBungee()) {
                //Bungee sign
                drawBungeeSign(signData);
            } else if (portal.getOptions().isFixed()) {
                //Sign pointing at one other portal
                drawFixedSign(signData);
            } else {
                //Networking stuff
                drawNetworkSign(signData);
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
            sign.getSide(Side.FRONT).setLine(index, "");
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
        sign.getSide(Side.FRONT).setLine(0, translateAllColorCodes(portal.getName()));
        sign.update();
    }

    /**
     * Draws a sign with choose-able network locations
     *
     * @param signData <p>All necessary sign information</p>
     */
    private void drawNetworkSign(SignData signData) {
        PortalActivator destinations = portal.getPortalActivator();
        int maxIndex = destinations.getDestinations().size() - 1;
        int signLineIndex = 0;
        int destinationIndex = destinations.getDestinations().indexOf(portal.getDestinationName());
        boolean freeGatesColored = Stargate.getEconomyConfig().useEconomy() &&
                Stargate.getEconomyConfig().drawFreePortalsColored();

        //Last, and not only entry. Draw the entry two back
        if ((destinationIndex == maxIndex) && (maxIndex > 1)) {
            drawNetworkSignLine(signData, freeGatesColored, ++signLineIndex, destinationIndex - 2);
        }
        //Not first entry. Draw the previous entry
        if (destinationIndex > 0) {
            drawNetworkSignLine(signData, freeGatesColored, ++signLineIndex, destinationIndex - 1);
        }
        //Draw the chosen entry (line 2 or 3)
        drawNetworkSignChosenLine(signData, freeGatesColored, ++signLineIndex);
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 1)) {
            drawNetworkSignLine(signData, freeGatesColored, ++signLineIndex, destinationIndex + 1);
        }
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 2) && (++signLineIndex <= 3)) {
            drawNetworkSignLine(signData, freeGatesColored, signLineIndex, destinationIndex + 2);
        }
    }

    /**
     * Draws the chosen destination on one sign line
     *
     * @param signData         <p>All necessary sign information</p>
     * @param freeGatesColored <p>Whether to display free gates in a different color</p>
     * @param signLineIndex    <p>The line to draw on</p>
     */
    private void drawNetworkSignChosenLine(SignData signData, boolean freeGatesColored, int signLineIndex) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        if (freeGatesColored) {
            Portal destination = PortalHandler.getByName(portal.getDestinationName(), portal.getNetwork());
            boolean free = PermissionHelper.isFree(portal.getActivePlayer(), portal, destination);
            ChatColor nameColor = (free ? freeColor : highlightColor);
            setLine(signData, signLineIndex, nameColor + ">" + (free ? freeColor : mainColor) +
                    translateAllColorCodes(portal.getDestinationName()) + nameColor + "<");
        } else {
            setLine(signData, signLineIndex, highlightColor + ">" + mainColor +
                    translateAllColorCodes(portal.getDestinationName()) + highlightColor + "<");
        }
    }

    /**
     * Sets a line on a sign, adding the chosen sign color
     *
     * @param signData <p>All necessary sign information</p>
     * @param index    <p>The index of the sign line to change</p>
     * @param text     <p>The new text on the sign</p>
     */
    public void setLine(SignData signData, int index, String text) {
        ChatColor mainColor = signData.getMainSignColor();
        signData.getSign().getSide(Side.FRONT).setLine(index, mainColor + text);
    }

    /**
     * Draws one network destination on one sign line
     *
     * @param signData         <p>All necessary sign information</p>
     * @param freeGatesColored <p>Whether to display free gates in a different color</p>
     * @param signLineIndex    <p>The line to draw on</p>
     * @param destinationIndex <p>The index of the destination to draw</p>
     */
    private void drawNetworkSignLine(SignData signData, boolean freeGatesColored, int signLineIndex, int destinationIndex) {
        ChatColor mainColor = signData.getMainSignColor();
        PortalActivator destinations = portal.getPortalActivator();
        String destinationName = destinations.getDestinations().get(destinationIndex);
        if (freeGatesColored) {
            Portal destination = PortalHandler.getByName(destinationName, portal.getNetwork());
            boolean free = PermissionHelper.isFree(portal.getActivePlayer(), portal, destination);
            setLine(signData, signLineIndex, (free ? freeColor : mainColor) + translateAllColorCodes(destinationName));
        } else {
            setLine(signData, signLineIndex, mainColor + translateAllColorCodes(destinationName));
        }
    }

    /**
     * Draws the sign of a BungeeCord portal
     *
     * @param signData <p>All necessary sign information</p>
     */
    private void drawBungeeSign(SignData signData) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        setLine(signData, 1, Stargate.getString("bungeeSign"));
        setLine(signData, 2, highlightColor + ">" + mainColor + translateAllColorCodes(portal.getDestinationName()) +
                highlightColor + "<");
        setLine(signData, 3, highlightColor + "[" + mainColor + translateAllColorCodes(portal.getNetwork()) +
                highlightColor + "]");
    }

    /**
     * Draws the sign of an in-active portal
     *
     * <p>The sign for an in-active portal should display the right-click prompt and the network.</p>
     *
     * @param signData <p>All necessary sign information</p>
     */
    private void drawInactiveSign(SignData signData) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        setLine(signData, 1, Stargate.getString("signRightClick"));
        setLine(signData, 2, Stargate.getString("signToUse"));
        if (!portal.getOptions().isNoNetwork()) {
            setLine(signData, 3, highlightColor + "(" + mainColor + translateAllColorCodes(portal.getNetwork()) +
                    highlightColor + ")");
        } else {
            setLine(signData, 3, "");
        }
    }

    /**
     * Draws a sign pointing to a fixed location
     *
     * @param signData <p>All necessary sign information</p>
     */
    private void drawFixedSign(SignData signData) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        Portal destinationPortal = PortalHandler.getByName(Portal.cleanString(portal.getDestinationName()),
                portal.getCleanNetwork());
        String destinationName = portal.getOptions().isRandom() ? Stargate.getString("signRandom") :
                (destinationPortal != null ? destinationPortal.getName() : portal.getDestinationName());
        setLine(signData, 1, highlightColor + ">" + mainColor + translateAllColorCodes(destinationName) +
                highlightColor + "<");

        if (portal.getOptions().isNoNetwork()) {
            setLine(signData, 2, "");
        } else {
            setLine(signData, 2, highlightColor + "(" + mainColor +
                    translateAllColorCodes(portal.getNetwork()) + highlightColor + ")");
        }
        Portal destination = PortalHandler.getByName(Portal.cleanString(portal.getDestinationName()),
                portal.getNetwork());
        if (destination == null && !portal.getOptions().isRandom()) {
            setLine(signData, 3, errorColor + Stargate.getString("signDisconnected"));
        } else {
            setLine(signData, 3, "");
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
        BlockState blockState = portalLocation.getSignLocation().getBlock().getState();
        if (!(blockState instanceof Sign sign)) {
            return;
        }
        sign.getSide(Side.FRONT).setLine(3, errorColor + Stargate.getString("signInvalidGate"));
        sign.update();

        Stargate.logInfo(String.format("Gate layout on line %d does not exist [%s]", lineIndex, gateName));
    }

    /**
     * Gets the main color to use for the given sign type
     *
     * @param signType <p>The sign type to get the main color for</p>
     * @return <p>The main color for the given sign type</p>
     */
    private ChatColor getMainColor(Material signType) {
        ChatColor signColor = perSignMainColors.get(signType);
        if (signColor == null) {
            return mainColor;
        } else {
            return signColor;
        }
    }

    /**
     * Gets the highlight color to use for the given sign type
     *
     * @param signType <p>The sign type to get the highlight color for</p>
     * @return <p>The highlight color for the given sign type</p>
     */
    private ChatColor getHighlightColor(Material signType) {
        ChatColor signColor = perSignHighlightColors.get(signType);
        if (signColor == null) {
            return highlightColor;
        } else {
            return signColor;
        }
    }

    /**
     * Translates all normal and RGB color codes in the given input
     *
     * @param input <p>The input to translate color codes for</p>
     * @return <p>The input with color codes converted translated from & to §</p>
     */
    private String translateAllColorCodes(String input) {
        return ColorHelper.translateColorCodes(input, ColorConversion.RGB);
    }

}
