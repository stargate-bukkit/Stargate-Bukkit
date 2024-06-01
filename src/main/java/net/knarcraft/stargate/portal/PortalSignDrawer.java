package net.knarcraft.stargate.portal;

import net.knarcraft.knarlib.property.ColorConversion;
import net.knarcraft.knarlib.util.ColorHelper;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.Message;
import net.knarcraft.stargate.container.SignData;
import net.knarcraft.stargate.portal.property.PortalLocation;
import net.knarcraft.stargate.utility.PermissionHelper;
import net.knarcraft.stargate.utility.SignHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

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
    public PortalSignDrawer(@NotNull Portal portal) {
        this.portal = portal;
    }

    /**
     * Sets the highlighting sign color
     *
     * <p>The highlighting color is used for the markings around portal names and network names ('>','<','-',')','(').</p>
     *
     * @param newHighlightColor <p>The new highlight color</p>
     */
    public static void setHighlightColor(@NotNull ChatColor newHighlightColor) {
        highlightColor = newHighlightColor;
    }

    /**
     * Sets the main sign color
     *
     * <p>The main sign color is used for most text on the sign.</p>
     *
     * @param newMainColor <p>The new main sign color</p>
     */
    public static void setMainColor(@NotNull ChatColor newMainColor) {
        mainColor = newMainColor;
    }

    /**
     * Sets the color to use for marking free stargates
     *
     * @param freeColor <p>The new color to use for marking free stargates</p>
     */
    public static void setFreeColor(@NotNull ChatColor freeColor) {
        PortalSignDrawer.freeColor = freeColor;
    }

    /**
     * Sets the per-sign main colors
     *
     * @param signMainColors <p>The per-sign main colors</p>
     */
    public static void setPerSignMainColors(@NotNull Map<Material, ChatColor> signMainColors) {
        PortalSignDrawer.perSignMainColors = signMainColors;
    }

    /**
     * Sets the per-sign highlight colors
     *
     * @param signHighlightColors <p>The per-sign highlight colors</p>
     */
    public static void setPerSignHighlightColors(@NotNull Map<Material, ChatColor> signHighlightColors) {
        PortalSignDrawer.perSignHighlightColors = signHighlightColors;
    }

    /**
     * Gets the currently used main sign color
     *
     * @return <p>The currently used main sign color</p>
     */
    @NotNull
    public static ChatColor getMainColor() {
        return mainColor;
    }

    /**
     * Gets the currently used highlighting sign color
     *
     * @return <p>The currently used highlighting sign color</p>
     */
    @NotNull
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

        drawSign(new SignData(sign, getMainColor(sign.getType()), getHighlightColor(sign.getType())));
    }

    /**
     * Gets the sign for this sign drawer's portal
     *
     * @return <p>The sign of this sign drawer's portal</p>
     */
    @Nullable
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
    private void drawSign(@NotNull SignData signData) {
        Sign sign = signData.getSign();
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();

        String[] lines = new String[4];
        setLine(signData, 0, highlightColor + "-" + mainColor + translateAllColorCodes(portal.getName()) +
                highlightColor + "-", lines);

        if (!portal.getPortalActivator().isActive()) {
            //Default sign text
            drawInactiveSign(signData, lines);
        } else {
            if (portal.getOptions().isBungee()) {
                //Bungee sign
                drawBungeeSign(signData, lines);
            } else if (portal.getOptions().isFixed()) {
                //Sign pointing at one other portal
                drawFixedSign(signData, lines);
            } else {
                //Networking stuff
                drawNetworkSign(signData, lines);
            }
        }

        updateSign(sign, lines);
    }

    /**
     * Updates a sign, if necessary
     *
     * @param sign  <p>The sign to update</p>
     * @param lines <p>The sign's new lines</p>
     */
    private void updateSign(@NotNull Sign sign, @NotNull String[] lines) {
        boolean updateNecessary = false;

        String[] oldLines = SignHelper.getLines(sign);
        for (int i = 0; i < 4; i++) {
            if (!oldLines[i].equals(lines[i])) {
                updateNecessary = true;
                break;
            }
        }

        if (updateNecessary) {
            for (int i = 0; i < 4; i++) {
                SignHelper.setSignLine(sign, i, lines[i]);
            }

            sign.update();
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

        for (int index = 0; index <= 3; index++) {
            SignHelper.setSignLine(sign, index, "");
        }

        SignHelper.setSignLine(sign, 0, translateAllColorCodes(portal.getName()));
        sign.update();
    }

    /**
     * Draws a sign with choose-able network locations
     *
     * @param signData <p>All necessary sign information</p>
     * @param output   <p>The output list to write to</p>
     */
    private void drawNetworkSign(@NotNull SignData signData, @NotNull String[] output) {
        PortalActivator destinations = portal.getPortalActivator();
        int maxIndex = destinations.getDestinations().size() - 1;
        int signLineIndex = 0;
        int destinationIndex = destinations.getDestinations().indexOf(portal.getDestinationName());
        boolean freeGatesColored = Stargate.getEconomyConfig().useEconomy() &&
                Stargate.getEconomyConfig().drawFreePortalsColored();

        //Last, and not only entry. Draw the entry two back
        if ((destinationIndex == maxIndex) && (maxIndex > 1)) {
            drawNetworkSignLine(signData, freeGatesColored, ++signLineIndex, destinationIndex - 2, output);
        }
        //Not first entry. Draw the previous entry
        if (destinationIndex > 0) {
            drawNetworkSignLine(signData, freeGatesColored, ++signLineIndex, destinationIndex - 1, output);
        }
        //Draw the chosen entry (line 2 or 3)
        drawNetworkSignChosenLine(signData, freeGatesColored, ++signLineIndex, output);
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 1)) {
            drawNetworkSignLine(signData, freeGatesColored, ++signLineIndex, destinationIndex + 1, output);
        }
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 2) && (++signLineIndex <= 3)) {
            drawNetworkSignLine(signData, freeGatesColored, signLineIndex, destinationIndex + 2, output);
        }
    }

    /**
     * Draws the chosen destination on one sign line
     *
     * @param signData         <p>All necessary sign information</p>
     * @param freeGatesColored <p>Whether to display free gates in a different color</p>
     * @param signLineIndex    <p>The line to draw on</p>
     * @param output           <p>The output list to write to</p>
     */
    private void drawNetworkSignChosenLine(@NotNull SignData signData, boolean freeGatesColored, int signLineIndex,
                                           @NotNull String[] output) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        if (freeGatesColored) {
            Portal destination = PortalHandler.getByName(portal.getDestinationName(), portal.getNetwork());
            boolean free = PermissionHelper.isFree(Objects.requireNonNull(portal.getActivePlayer()), portal, destination);
            ChatColor nameColor = (free ? freeColor : highlightColor);
            setLine(signData, signLineIndex, nameColor + ">" + (free ? freeColor : mainColor) +
                    translateAllColorCodes(portal.getDestinationName()) + nameColor + "<", output);
        } else {
            setLine(signData, signLineIndex, highlightColor + ">" + mainColor +
                    translateAllColorCodes(portal.getDestinationName()) + highlightColor + "<", output);
        }
    }

    /**
     * Sets a line on a sign, adding the chosen sign color
     *
     * @param signData <p>All necessary sign information</p>
     * @param index    <p>The index of the sign line to change</p>
     * @param text     <p>The new text on the sign</p>
     * @param output   <p>The output list to write to</p>
     */
    public void setLine(@NotNull SignData signData, int index, @NotNull String text, @NotNull String[] output) {
        ChatColor mainColor = signData.getMainSignColor();
        output[index] = mainColor + text;
    }

    /**
     * Draws one network destination on one sign line
     *
     * @param signData         <p>All necessary sign information</p>
     * @param freeGatesColored <p>Whether to display free gates in a different color</p>
     * @param signLineIndex    <p>The line to draw on</p>
     * @param destinationIndex <p>The index of the destination to draw</p>
     * @param output           <p>The output list to write to</p>
     */
    private void drawNetworkSignLine(@NotNull SignData signData, boolean freeGatesColored, int signLineIndex,
                                     int destinationIndex, @NotNull String[] output) {
        ChatColor mainColor = signData.getMainSignColor();
        PortalActivator destinations = portal.getPortalActivator();
        String destinationName = destinations.getDestinations().get(destinationIndex);
        if (freeGatesColored) {
            Portal destination = PortalHandler.getByName(destinationName, portal.getNetwork());
            boolean free = PermissionHelper.isFree(Objects.requireNonNull(portal.getActivePlayer()), portal, destination);
            setLine(signData, signLineIndex, (free ? freeColor : mainColor) + translateAllColorCodes(destinationName), output);
        } else {
            setLine(signData, signLineIndex, mainColor + translateAllColorCodes(destinationName), output);
        }
    }

    /**
     * Draws the sign of a BungeeCord portal
     *
     * @param signData <p>All necessary sign information</p>
     * @param output   <p>The output list to write to</p>
     */
    private void drawBungeeSign(@NotNull SignData signData, @NotNull String[] output) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        setLine(signData, 1, Stargate.getString(Message.BUNGEE_SIGN), output);
        setLine(signData, 2, highlightColor + ">" + mainColor +
                translateAllColorCodes(portal.getDestinationName()) + highlightColor + "<", output);
        setLine(signData, 3, highlightColor + "[" + mainColor + translateAllColorCodes(portal.getNetwork()) +
                highlightColor + "]", output);
    }

    /**
     * Draws the sign of an in-active portal
     *
     * <p>The sign for an in-active portal should display the right-click prompt and the network.</p>
     *
     * @param signData <p>All necessary sign information</p>
     * @param output   <p>The output list to write to</p>
     */
    private void drawInactiveSign(@NotNull SignData signData, @NotNull String[] output) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        setLine(signData, 1, Stargate.getString(Message.SIGN_RIGHT_CLICK), output);
        setLine(signData, 2, Stargate.getString(Message.SIGN_TO_USE), output);
        if (!portal.getOptions().isNoNetwork()) {
            setLine(signData, 3, highlightColor + "(" + mainColor + translateAllColorCodes(portal.getNetwork()) +
                    highlightColor + ")", output);
        } else {
            setLine(signData, 3, "", output);
        }
    }

    /**
     * Draws a sign pointing to a fixed location
     *
     * @param signData <p>All necessary sign information</p>
     * @param output   <p>The output list to write to</p>
     */
    private void drawFixedSign(@NotNull SignData signData, @NotNull String[] output) {
        ChatColor highlightColor = signData.getHighlightSignColor();
        ChatColor mainColor = signData.getMainSignColor();
        Portal destinationPortal = PortalHandler.getByName(Portal.cleanString(portal.getDestinationName()),
                portal.getCleanNetwork());
        String destinationName = portal.getOptions().isRandom() ? Stargate.getString(Message.SIGN_RANDOM) :
                (destinationPortal != null ? destinationPortal.getName() : portal.getDestinationName());
        setLine(signData, 1, highlightColor + ">" + mainColor + translateAllColorCodes(destinationName) +
                highlightColor + "<", output);

        if (portal.getOptions().isNoNetwork()) {
            setLine(signData, 2, "", output);
        } else {
            setLine(signData, 2, highlightColor + "(" + mainColor +
                    translateAllColorCodes(portal.getNetwork()) + highlightColor + ")", output);
        }
        Portal destination = PortalHandler.getByName(Portal.cleanString(portal.getDestinationName()),
                portal.getNetwork());
        if (destination == null && !portal.getOptions().isRandom()) {
            setLine(signData, 3, errorColor + Stargate.getString(Message.SIGN_DISCONNECTED), output);
        } else {
            setLine(signData, 3, "", output);
        }
    }

    /**
     * Marks a portal with an invalid gate by changing its sign and writing to the console
     *
     * @param portalLocation <p>The location of the portal with an invalid gate</p>
     * @param gateName       <p>The name of the invalid gate type</p>
     * @param lineIndex      <p>The index of the line the invalid portal was found at</p>
     */
    public static void markPortalWithInvalidGate(@NotNull PortalLocation portalLocation, @NotNull String gateName,
                                                 int lineIndex) {
        BlockState blockState = portalLocation.getSignLocation().getBlock().getState();
        if (!(blockState instanceof Sign sign)) {
            return;
        }
        SignHelper.setSignLine(sign, 3, errorColor + Stargate.getString(Message.SIGN_INVALID));
        sign.update();

        Stargate.logInfo(String.format("Gate layout on line %d does not exist [%s]", lineIndex, gateName));
    }

    /**
     * Gets the main color to use for the given sign type
     *
     * @param signType <p>The sign type to get the main color for</p>
     * @return <p>The main color for the given sign type</p>
     */
    @NotNull
    private ChatColor getMainColor(@NotNull Material signType) {
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
    @NotNull
    private ChatColor getHighlightColor(@NotNull Material signType) {
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
    @NotNull
    private String translateAllColorCodes(@NotNull String input) {
        return ColorHelper.translateColorCodes(input, ColorConversion.RGB);
    }

}
