package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.PortalHandler;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

/**
 * This class helps to draw the sign on a portal as it's a bit too complicated to be contained within the portal class
 */
public final class SignHelper {

    /**
     * Draws this portal's sign
     */
    public static void drawSign(Portal portal) {
        Block signBlock = portal.getSignLocation().getBlock();
        BlockState state = signBlock.getState();
        if (!(state instanceof Sign sign)) {
            Stargate.logger.warning(Stargate.getString("prefix") + "Sign block is not a Sign object");
            Stargate.debug("Portal::drawSign", "Block: " + signBlock.getType() + " @ " +
                    signBlock.getLocation());
            return;
        }

        SignHelper.drawSign(sign, portal);
    }

    /**
     * Draws the sign on this portal
     */
    public static void drawSign(Sign sign, Portal portal) {
        //Clear sign
        for (int index = 0; index <= 3; index++) {
            sign.setLine(index, "");
        }
        Stargate.setLine(sign, 0, ChatColor.WHITE + "-" + ChatColor.BLACK + portal.getName() +
                ChatColor.WHITE + "-");

        if (!portal.isActive()) {
            //Default sign text
            drawInactiveSign(sign, portal);
        } else {
            if (portal.getOptions().isBungee()) {
                //Bungee sign
                drawBungeeSign(sign, portal);
            } else if (portal.getOptions().isFixed()) {
                //Sign pointing at one other portal
                drawFixedSign(sign, portal);
            } else {
                //Networking stuff
                drawNetworkSign(sign, portal);
            }
        }

        sign.update();
    }

    /**
     * Draws a sign with choose-able network locations
     *
     * @param sign <p>The sign to draw on</p>
     */
    private static void drawNetworkSign(Sign sign, Portal portal) {
        int maxIndex = portal.getDestinations().size() - 1;
        int signLineIndex = 0;
        int destinationIndex = portal.getDestinations().indexOf(portal.getDestinationName());
        boolean freeGatesGreen = EconomyHandler.useEconomy() && EconomyHandler.freeGatesGreen;

        //Last, and not only entry. Draw the entry two back
        if ((destinationIndex == maxIndex) && (maxIndex > 1)) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex - 2, portal);
        }
        //Not first entry. Draw the previous entry
        if (destinationIndex > 0) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex - 1, portal);
        }
        //Draw the chosen entry (line 2 or 3)
        drawNetworkSignChosenLine(freeGatesGreen, sign, ++signLineIndex, portal);
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 1)) {
            drawNetworkSignLine(freeGatesGreen, sign, ++signLineIndex, destinationIndex + 1, portal);
        }
        //Has another entry and space on the sign
        if ((maxIndex >= destinationIndex + 2) && (++signLineIndex <= 3)) {
            drawNetworkSignLine(freeGatesGreen, sign, signLineIndex, destinationIndex + 2, portal);
        }
    }

    /**
     * Draws the chosen destination on one sign line
     *
     * @param freeGatesGreen <p>Whether to display free gates in a green color</p>
     * @param sign           <p>The sign to draw on</p>
     * @param signLineIndex  <p>The line to draw on</p>
     */
    private static void drawNetworkSignChosenLine(boolean freeGatesGreen, Sign sign, int signLineIndex, Portal portal) {
        if (freeGatesGreen) {
            Portal destination = PortalHandler.getByName(portal.getDestinationName(), portal.getNetwork());
            boolean green = PermissionHelper.isFree(portal.getActivePlayer(), portal, destination);
            Stargate.setLine(sign, signLineIndex, (green ? ChatColor.DARK_GREEN : "") + ">" +
                    portal.getDestinationName() + (green ? ChatColor.DARK_GREEN : "") + "<");
        } else {
            Stargate.setLine(sign, signLineIndex, ChatColor.BLACK + " >" + portal.getDestinationName() +
                    ChatColor.BLACK + "< ");
        }
    }

    /**
     * Draws one network destination on one sign line
     *
     * @param freeGatesGreen   <p>Whether to display free gates in a green color</p>
     * @param sign             <p>The sign to draw on</p>
     * @param signLineIndex    <p>The line to draw on</p>
     * @param destinationIndex <p>The index of the destination to draw</p>
     */
    private static void drawNetworkSignLine(boolean freeGatesGreen, Sign sign, int signLineIndex, int destinationIndex,
                                            Portal portal) {
        if (freeGatesGreen) {
            Portal destination = PortalHandler.getByName(portal.getDestinations().get(destinationIndex), portal.getNetwork());
            boolean green = PermissionHelper.isFree(portal.getActivePlayer(), portal, destination);
            Stargate.setLine(sign, signLineIndex, (green ? ChatColor.DARK_GREEN : "") + portal.getDestinations().get(destinationIndex));
        } else {
            Stargate.setLine(sign, signLineIndex, portal.getDestinations().get(destinationIndex));
        }
    }

    /**
     * Draws a bungee sign
     *
     * @param sign <p>The sign to draw on</p>
     */
    private static void drawBungeeSign(Sign sign, Portal portal) {
        Stargate.setLine(sign, 1, Stargate.getString("bungeeSign"));
        Stargate.setLine(sign, 2, ">" + portal.getDestinationName() + "<");
        Stargate.setLine(sign, 3, "[" + portal.getNetwork() + "]");
    }

    /**
     * Draws an inactive sign
     *
     * @param sign <p>The sign to draw on</p>
     */
    private static void drawInactiveSign(Sign sign, Portal portal) {
        Stargate.setLine(sign, 1, Stargate.getString("signRightClick"));
        Stargate.setLine(sign, 2, Stargate.getString("signToUse"));
        if (!portal.getOptions().isNoNetwork()) {
            Stargate.setLine(sign, 3, "(" + portal.getNetwork() + ")");
        } else {
            Stargate.setLine(sign, 3, "");
        }
    }

    /**
     * Draws a sign pointing to a fixed location
     *
     * @param sign <p>The sign to draw on</p>
     */
    private static void drawFixedSign(Sign sign, Portal portal) {
        if (portal.getOptions().isRandom()) {
            Stargate.setLine(sign, 1, "> " + Stargate.getString("signRandom") + " <");
        } else {
            Stargate.setLine(sign, 1, ">" + portal.getDestinationName() + "<");
        }
        if (portal.getOptions().isNoNetwork()) {
            Stargate.setLine(sign, 2, "");
        } else {
            Stargate.setLine(sign, 2, "(" + portal.getNetwork() + ")");
        }
        Portal destination = PortalHandler.getByName(portal.getDestinationName(), portal.getNetwork());
        if (destination == null && !portal.getOptions().isRandom()) {
            Stargate.setLine(sign, 3, Stargate.getString("signDisconnected"));
        } else {
            Stargate.setLine(sign, 3, "");
        }
    }

}
