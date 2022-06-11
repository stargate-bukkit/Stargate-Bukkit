package net.TheDgtl.Stargate.economy;

import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * An abstract economy manager containing the essential economy code
 */
public abstract class EconomyManager implements EconomyAPI, StargateEconomyAPI {

    private final LanguageManager languageManager;

    /**
     * Instantiates a new economy manager
     *
     * @param languageManager <p>The language manager to use for translations</p>
     */
    public EconomyManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    @Override
    public boolean chargePlayer(OfflinePlayer player, Portal origin, int amount) {
        //Skip if no payment is necessary
        if (amount == 0) {
            return true;
        }
        boolean isOwner = origin.getOwnerUUID() == player.getUniqueId();
        boolean ownerRevenue = ConfigurationHelper.getBoolean(ConfigurationOption.GATE_OWNER_REVENUE);
        //Skip payment if the player would pay to itself
        if (isOwner && ownerRevenue && origin.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
            return true;
        }
        if (ownerRevenue) {
            //Pay to the owner
            OfflinePlayer transactionTarget = Bukkit.getServer().getOfflinePlayer(origin.getOwnerUUID());
            //If the owner is the player, there is nothing to do
            if (transactionTarget.equals(player)) {
                return true;
            }
            //Failed payment
            if (!chargeAndDepositPlayer(player, transactionTarget, amount)) {
                return false;
            }
            //Inform the transaction target that they've received money
            sendObtainSuccessMessage(transactionTarget, amount, origin.getName());
            return true;
        } else {
            //Pay to the server
            return chargeAndTax(player, amount);
        }
    }

    @Override
    public boolean chargeAndTax(OfflinePlayer player, int amount) {
        //Skip if no payment is necessary
        if (amount == 0) {
            return true;
        }
        String bankUUIDString = ConfigurationHelper.getString(ConfigurationOption.TAX_DESTINATION);
        if (!bankUUIDString.isEmpty()) {
            //Charge the player and put the money on the tax account
            OfflinePlayer bankAccount = Bukkit.getOfflinePlayer(UUID.fromString(bankUUIDString));
            return chargeAndDepositPlayer(player, bankAccount, amount);
        } else {
            return chargePlayer(player, amount);
        }
    }

    /**
     * Charges the given player and deposits it into the other given player's account
     *
     * @param player            <p>The player to charge</p>
     * @param transactionTarget <p>The player to receive the payment</p>
     * @param amount            <p>The amount the player should be charged</p>
     * @return <p>True if there was no problems with the payment</p>
     */
    protected boolean chargeAndDepositPlayer(OfflinePlayer player, OfflinePlayer transactionTarget, int amount) {
        //Skip if there is no charge, or if the transaction wouldn't change anything
        if (amount == 0 || player.equals(transactionTarget)) {
            return true;
        }
        if (chargePlayer(player, amount)) {
            return depositPlayer(transactionTarget, amount);
        } else {
            return false;
        }
    }

    /**
     * Sends a message to a player telling them they've been successfully charged
     *
     * @param offlinePlayer <p>The player to send the message to</p>
     * @param amount        <p>The amount the player was charged</p>
     */
    protected void sendChargeSuccessMessage(OfflinePlayer offlinePlayer, int amount) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return;
        }
        //Tell the player that they have been charged
        String unformattedMessage = languageManager.getMessage(TranslatableMessage.ECO_DEDUCT);
        String message = TranslatableMessageFormatter.formatCost(unformattedMessage, amount);
        player.sendMessage(message);
    }

    /**
     * Sends a message to the player telling them they've successfully received funds
     *
     * @param offlinePlayer <p>The player to send the message to</p>
     * @param amount        <p>The amount the player received</p>
     * @param portalName    <p>The portal used by another player</p>
     */
    protected void sendObtainSuccessMessage(OfflinePlayer offlinePlayer, int amount, String portalName) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return;
        }
        //Inform the transaction target that they've received money
        String unFormattedMessage = languageManager.getMessage(TranslatableMessage.ECO_OBTAIN);
        String portalNameCompiledMessage = TranslatableMessageFormatter.formatPortal(unFormattedMessage, portalName);
        String message = TranslatableMessageFormatter.formatCost(portalNameCompiledMessage, amount);
        player.sendMessage(message);
    }

}
