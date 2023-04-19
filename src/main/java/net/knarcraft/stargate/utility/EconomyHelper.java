package net.knarcraft.stargate.utility;

import net.knarcraft.knarlib.formatting.StringFormatter;
import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.EconomyConfig;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.property.PortalOwner;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The economy helper class has helper functions for player payment
 */
public final class EconomyHelper {

    private EconomyHelper() {

    }

    /**
     * Tries to make the given user pay the teleport fee
     *
     * @param entrancePortal <p>The portal the player is entering</p>
     * @param player         <p>The player wishing to teleport</p>
     * @param cost           <p>The cost of teleportation</p>
     * @return <p>False if payment was successful. True if the payment was unsuccessful</p>
     */
    public static boolean cannotPayTeleportFee(Portal entrancePortal, Player player, int cost) {
        boolean success;

        //Try to charge the player. Paying the portal owner is only possible if a UUID is available
        UUID ownerUUID = entrancePortal.getOwner().getUUID();
        if (ownerUUID == null) {
            Stargate.logWarning(String.format("The owner of the portal %s does not have a UUID and payment to owner " +
                    "was therefore not possible. Make the owner re-create the portal to fix this.", entrancePortal));
        }
        if (entrancePortal.getGate().getToOwner() && ownerUUID != null) {
            success = chargePlayerIfNecessary(player, ownerUUID, cost);
        } else {
            success = chargePlayerIfNecessary(player, cost);
        }

        //Send the insufficient funds message
        if (!success) {
            sendInsufficientFundsMessage(entrancePortal.getName(), player, cost);
            entrancePortal.getPortalOpener().closePortal(false);
            return true;
        }

        //Send the deduct-message to the player
        sendDeductMessage(entrancePortal.getName(), player, cost);

        if (entrancePortal.getGate().getToOwner()) {
            PortalOwner owner = entrancePortal.getOwner();
            Player portalOwner;
            if (owner.getUUID() != null) {
                portalOwner = Stargate.getInstance().getServer().getPlayer(owner.getUUID());
            } else {
                portalOwner = Stargate.getInstance().getServer().getPlayer(owner.getName());
            }

            //Notify the gate owner of received payment
            if (portalOwner != null) {
                sendObtainMessage(entrancePortal.getName(), portalOwner, cost);
            }
        }
        return false;
    }

    /**
     * Sends a message to the gate owner telling him/her how much he/she earned from a player using his/her gate
     *
     * @param portalName  <p>The name of the used portal</p>
     * @param portalOwner <p>The owner of the portal</p>
     * @param earnings    <p>The amount the owner earned</p>
     */
    public static void sendObtainMessage(String portalName, Player portalOwner, int earnings) {
        String obtainedMsg = Stargate.getString("ecoObtain");
        obtainedMsg = replacePlaceholders(obtainedMsg, portalName, earnings);
        Stargate.getMessageSender().sendSuccessMessage(portalOwner, obtainedMsg);
    }

    /**
     * Sends a message telling the user how much they paid for interacting with a portal
     *
     * @param portalName <p>The name of the portal interacted with</p>
     * @param player     <p>The interacting player</p>
     * @param cost       <p>The cost of the interaction</p>
     */
    public static void sendDeductMessage(String portalName, Player player, int cost) {
        String deductMsg = Stargate.getString("ecoDeduct");
        deductMsg = replacePlaceholders(deductMsg, portalName, cost);
        Stargate.getMessageSender().sendSuccessMessage(player, deductMsg);
    }

    /**
     * Sends a message telling the user they don't have enough funds to do a portal interaction
     *
     * @param portalName <p>The name of the portal interacted with</p>
     * @param player     <p>The interacting player</p>
     * @param cost       <p>The cost of the interaction</p>
     */
    public static void sendInsufficientFundsMessage(String portalName, Player player, int cost) {
        String inFundMsg = Stargate.getString("ecoInFunds");
        inFundMsg = replacePlaceholders(inFundMsg, portalName, cost);
        Stargate.getMessageSender().sendErrorMessage(player, inFundMsg);
    }

    /**
     * Sends a message telling the user how much they are refunded for breaking their portal
     *
     * @param portalName <p>The name of the broken portal</p>
     * @param player     <p>The player breaking the portal</p>
     * @param cost       <p>The amount the user has to pay for destroying the portal. (expects a negative value)</p>
     */
    public static void sendRefundMessage(String portalName, Player player, int cost) {
        String refundMsg = Stargate.getString("ecoRefund");
        refundMsg = replacePlaceholders(refundMsg, portalName, -cost);
        Stargate.getMessageSender().sendSuccessMessage(player, refundMsg);
    }

    /**
     * Determines the cost of using a gate
     *
     * @param player      <p>The player trying to use the gate</p>
     * @param source      <p>The source/entry portal</p>
     * @param destination <p>The destination portal</p>
     * @return <p>The cost of using the portal</p>
     */
    public static int getUseCost(Player player, Portal source, Portal destination) {
        EconomyConfig config = Stargate.getEconomyConfig();
        //No payment required
        if (!config.useEconomy() || source.getOptions().isFree()) {
            return 0;
        }
        //Not charging for free destinations
        if (destination != null && config.freeIfFreeDestination() && destination.getOptions().isFree()) {
            return 0;
        }
        //Cost is 0 if the player owns this gate and funds go to the owner
        if (source.getGate().getToOwner() && source.isOwner(player)) {
            return 0;
        }
        //Player gets free gate use
        if (PermissionHelper.hasPermission(player, "stargate.free.use")) {
            return 0;
        }

        return source.getGate().getUseCost();
    }

    /**
     * Charges the player for an action, if required
     *
     * @param player <p>The player to take money from</p>
     * @param target <p>The target to pay</p>
     * @param cost   <p>The cost of the transaction</p>
     * @return <p>True if the player was charged successfully</p>
     */
    public static boolean chargePlayerIfNecessary(Player player, UUID target, int cost) {
        if (skipPayment(cost)) {
            return true;
        }
        //Charge player
        return chargePlayer(player, target, cost);
    }

    /**
     * Charges a player
     *
     * @param player <p>The player to charge</p>
     * @param amount <p>The amount to charge</p>
     * @return <p>True if the payment succeeded, or if no payment was necessary</p>
     */
    private static boolean chargePlayer(Player player, double amount) {
        Economy economy = Stargate.getEconomyConfig().getEconomy();
        if (Stargate.getEconomyConfig().isEconomyEnabled() && economy != null) {
            if (!economy.has(player, amount)) {
                return false;
            }
            economy.withdrawPlayer(player, amount);
        }
        return true;
    }

    /**
     * Transfers the given fees to the tax account
     *
     * @param economy <p>The economy to use</p>
     * @param cost    <p>The cost to transfer</p>
     */
    @SuppressWarnings("deprecation")
    private static void transferFees(Economy economy, int cost) {
        String accountName = Stargate.getEconomyConfig().getTaxAccount();
        if (accountName == null || accountName.isEmpty()) {
            return;
        }

        try {
            UUID accountId = UUID.fromString(accountName);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(accountId);
            economy.depositPlayer(offlinePlayer, cost);
        } catch (IllegalArgumentException exception) {
            economy.depositPlayer(accountName, cost);
        }
    }

    /**
     * Charges the player for an action, if required
     *
     * @param player <p>The player to take money from</p>
     * @param cost   <p>The cost of the transaction</p>
     * @return <p>True if the player was charged successfully</p>
     */
    public static boolean chargePlayerIfNecessary(Player player, int cost) {
        if (skipPayment(cost)) {
            return true;
        }
        //Charge player
        boolean charged = chargePlayer(player, cost);

        // Transfer the charged amount to the tax account
        if (charged) {
            transferFees(Stargate.getEconomyConfig().getEconomy(), cost);
        }

        return charged;
    }

    /**
     * Checks whether a payment transaction should be skipped
     *
     * @param cost <p>The cost of the transaction</p>
     * @return <p>True if the transaction should be skipped</p>
     */
    private static boolean skipPayment(int cost) {
        return cost == 0 || !Stargate.getEconomyConfig().useEconomy();
    }

    /**
     * Charges a player, giving the charge to a target
     *
     * @param player <p>The player to charge</p>
     * @param target <p>The UUID of the player to pay</p>
     * @param amount <p>The amount to charge</p>
     * @return <p>True if the payment succeeded, or if no payment was necessary</p>
     */
    private static boolean chargePlayer(Player player, UUID target, double amount) {
        Economy economy = Stargate.getEconomyConfig().getEconomy();
        if (Stargate.getEconomyConfig().isEconomyEnabled() && player.getUniqueId().compareTo(target) != 0 && economy != null) {
            if (!economy.has(player, amount)) {
                return false;
            }
            //Take money from the user and give to the owner
            economy.withdrawPlayer(player, amount);
            economy.depositPlayer(Bukkit.getOfflinePlayer(target), amount);
        }
        return true;
    }

    /**
     * Replaces the cost and portal variables in a string
     *
     * @param message    <p>The message to replace variables in</p>
     * @param portalName <p>The name of the relevant portal</p>
     * @param cost       <p>The cost for a given interaction</p>
     * @return <p>The same string with cost and portal variables replaced</p>
     */
    private static String replacePlaceholders(String message, String portalName, int cost) {
        return StringFormatter.replacePlaceholders(message, new String[]{"%cost%", "%portal%"},
                new String[]{Stargate.getEconomyConfig().format(cost), portalName});
    }

}
