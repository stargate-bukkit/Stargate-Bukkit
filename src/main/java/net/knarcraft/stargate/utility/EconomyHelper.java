package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.EconomyHandler;
import net.knarcraft.stargate.Portal;
import net.knarcraft.stargate.Stargate;
import org.bukkit.entity.Player;

public class EconomyHelper {

    /**
     * Tries to make the given user pay the teleport fee
     * @param entrancePortal <p>The portal the player is entering</p>
     * @param player <p>The player wishing to teleport</p>
     * @param cost <p>The cost of teleportation</p>
     * @return <p>True if payment was successful</p>
     */
    public static boolean payTeleportFee(Portal entrancePortal, Player player, int cost) {
        boolean success;

        //Try to charge the player
        if (entrancePortal.getGate().getToOwner()) {
            success = entrancePortal.getOwnerUUID() != null && Stargate.chargePlayer(player, entrancePortal.getOwnerUUID(), cost);
        } else {
            success = Stargate.chargePlayer(player, cost);
        }

        // Insufficient Funds
        if (!success) {
            sendInsufficientFundsMessage(entrancePortal.getName(), player, cost);
            entrancePortal.close(false);
            return false;
        }

        //Send the deduct message to the player
        sendDeductMessage(entrancePortal.getName(), player, cost);

        if (entrancePortal.getGate().getToOwner()) {
            Player gateOwner;
            if (entrancePortal.getOwnerUUID() != null) {
                gateOwner = Stargate.server.getPlayer(entrancePortal.getOwnerUUID());
            } else {
                gateOwner = Stargate.server.getPlayer(entrancePortal.getOwnerName());
            }

            //Notify the gate owner of received payment
            if (gateOwner != null) {
                sendObtainMessage(entrancePortal.getName(), gateOwner, cost);
            }
        }
        return true;
    }

    /**
     * Sends a message to the gate owner telling him/her how much he/she earned from a player using his/her gate
     * @param portalName <p>The name of the used portal</p>
     * @param portalOwner <p>The owner of the portal</p>
     * @param earnings <p>The amount the owner earned</p>
     */
    public static void sendObtainMessage(String portalName, Player portalOwner, int earnings) {
        String obtainedMsg = Stargate.getString("ecoObtain");
        obtainedMsg = replaceVars(obtainedMsg, portalName, earnings);
        Stargate.sendMessage(portalOwner, obtainedMsg, false);
    }

    /**
     * Sends a message telling the user how much they paid for interacting with a portal
     * @param portalName <p>The name of the portal interacted with</p>
     * @param player <p>The interacting player</p>
     * @param cost <p>The cost of the interaction</p>
     */
    public static void sendDeductMessage(String portalName, Player player, int cost) {
        String deductMsg = Stargate.getString("ecoDeduct");
        deductMsg = replaceVars(deductMsg, portalName, cost);
        Stargate.sendMessage(player, deductMsg, false);
    }

    /**
     * Sends a message telling the user they don't have enough funds to do a portal interaction
     * @param portalName <p>The name of the portal interacted with</p>
     * @param player <p>The interacting player</p>
     * @param cost <p>The cost of the interaction</p>
     */
    public static void sendInsufficientFundsMessage(String portalName, Player player, int cost) {
        String inFundMsg = Stargate.getString("ecoInFunds");
        inFundMsg = replaceVars(inFundMsg, portalName, cost);
        Stargate.sendMessage(player, inFundMsg);
    }

    /**
     * Sends a message telling the user how much they are refunded for breaking their portal
     * @param portalName <p>The name of the broken portal</p>
     * @param player <p>The player breaking the portal</p>
     * @param cost <p>The amount the user has to pay for destroying the portal. (expects a negative value)</p>
     */
    public static void sendRefundMessage(String portalName, Player player, int cost) {
        String refundMsg = Stargate.getString("ecoRefund");
        refundMsg = replaceVars(refundMsg, portalName, -cost);
        Stargate.sendMessage(player, refundMsg, false);
    }

    /**
     * Replaces the cost and portal variables in a string
     * @param message <p>The message to replace variables in</p>
     * @param portalName <p>The name of the relevant portal</p>
     * @param cost <p>The cost for a given interaction</p>
     * @return <p>The same string with cost and portal variables replaced</p>
     */
    private static String replaceVars(String message, String portalName, int cost) {
        return Stargate.replaceVars(message, new String[]{"%cost%", "%portal%"},
                new String[]{EconomyHandler.format(cost), portalName});
    }

}
