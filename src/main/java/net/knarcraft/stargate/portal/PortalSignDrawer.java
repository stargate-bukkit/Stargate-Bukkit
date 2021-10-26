package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.Stargate;
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

    /**
     * Instantiates a new portal sign drawer
     *
     * @param portal <p>The portal whose sign this portal sign drawer is responsible for drawing</p>
     */
    public PortalSignDrawer(Portal portal) {
        this.portal = portal;
    }

    /**
     * Draws the sign of the portal this sign drawer is responsible for
     */
    public void drawSign() {
        Block signBlock = portal.getSignLocation().getBlock();
        BlockState state = signBlock.getState();
        if (!(state instanceof Sign sign)) {
            Stargate.logWarning("Sign block is not a Sign object");
            Stargate.debug("Portal::drawSign", String.format("Block: %s @ %s", signBlock.getType(),
                    signBlock.getLocation()));
            return;
        }

        drawSign(sign);
    }

    /**
     * Draws the sign of the portal this sign drawer is responsible for
     *
     * @param sign <p>The sign re-draw</p>
     */
    public void drawSign(Sign sign) {
        //Clear sign
        for (int index = 0; index <= 3; index++) {
            sign.setLine(index, "");
        }
        setLine(sign, 0, ChatColor.WHITE + "-" + Stargate.getGateConfig().getSignColor() +
                portal.getName() + ChatColor.WHITE + "-");

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
            setLine(sign, signLineIndex, (green ? ChatColor.DARK_GREEN : "") + ">" +
                    portal.getDestinationName() + (green ? ChatColor.DARK_GREEN : "") + "<");
        } else {
            setLine(sign, signLineIndex, Stargate.getGateConfig().getSignColor() + " >" +
                    portal.getDestinationName() + Stargate.getGateConfig().getSignColor() + "< ");
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
        sign.setLine(index, Stargate.getGateConfig().getSignColor() + text);
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
        if (freeGatesGreen) {
            Portal destination = PortalHandler.getByName(destinations.getDestinations().get(destinationIndex),
                    portal.getNetwork());
            boolean green = PermissionHelper.isFree(portal.getActivePlayer(), portal, destination);
            setLine(sign, signLineIndex, (green ? ChatColor.DARK_GREEN : "") +
                    destinations.getDestinations().get(destinationIndex));
        } else {
            setLine(sign, signLineIndex, destinations.getDestinations().get(destinationIndex));
        }
    }

    /**
     * Draws a bungee sign
     *
     * @param sign <p>The sign to re-draw</p>
     */
    private void drawBungeeSign(Sign sign) {
        setLine(sign, 1, Stargate.getString("bungeeSign"));
        setLine(sign, 2, ">" + portal.getDestinationName() + "<");
        setLine(sign, 3, "[" + portal.getNetwork() + "]");
    }

    /**
     * Draws an inactive sign
     *
     * @param sign <p>The sign to re-draw</p>
     */
    private void drawInactiveSign(Sign sign) {
        setLine(sign, 1, Stargate.getString("signRightClick"));
        setLine(sign, 2, Stargate.getString("signToUse"));
        if (!portal.getOptions().isNoNetwork()) {
            setLine(sign, 3, "(" + portal.getNetwork() + ")");
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
        if (portal.getOptions().isRandom()) {
            setLine(sign, 1, "> " + Stargate.getString("signRandom") + " <");
        } else {
            setLine(sign, 1, ">" + portal.getDestinationName() + "<");
        }
        if (portal.getOptions().isNoNetwork()) {
            setLine(sign, 2, "");
        } else {
            setLine(sign, 2, "(" + portal.getNetwork() + ")");
        }
        Portal destination = PortalHandler.getByName(portal.getDestinationName(), portal.getNetwork());
        if (destination == null && !portal.getOptions().isRandom()) {
            setLine(sign, 3, Stargate.getString("signDisconnected"));
        } else {
            setLine(sign, 3, "");
        }
    }

}
