package org.sgrewritten.stargate.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.sgrewritten.stargate.config.ConfigurationHelper;
import org.sgrewritten.stargate.config.ConfigurationOption;
import org.sgrewritten.stargate.formatting.LanguageManager;
import org.sgrewritten.stargate.formatting.TranslatableMessage;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.util.TranslatableMessageFormatter;

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
        UUID transactionReceiverId = getTransactionReceiver(origin);
        //Skip payments to self
        if (transactionReceiverId != null && transactionReceiverId.equals(player.getUniqueId())) {
            return true;
        }
        if (transactionReceiverId != null) {
            //Pay the correct receiver
            OfflinePlayer transactionReceiver = Bukkit.getOfflinePlayer(transactionReceiverId);
            if (chargeAndDepositPlayer(player, transactionReceiver, amount)) {
                //Inform the transaction target that they've received money
                if (origin != null) {
                    sendObtainSuccessMessage(transactionReceiver, amount, origin.getName());
                }
                return true;
            } else {
                //Failed payment
                return false;
            }
        } else {
            //Give the money to the void
            return chargePlayer(player, amount);
        }
    }

    @Override
    public boolean refundPlayer(OfflinePlayer player, Portal origin, int amount) {
        //Skip if no payment is necessary
        if (amount == 0) {
            return true;
        }
        UUID transactionPayerId = getTransactionReceiver(origin);
        //Skip payments to self
        if (transactionPayerId != null && transactionPayerId.equals(player.getUniqueId())) {
            return true;
        }
        if (transactionPayerId != null) {
            //Pay the correct receiver
            OfflinePlayer transactionPayer = Bukkit.getOfflinePlayer(transactionPayerId);
            return chargeAndDepositPlayer(transactionPayer, player, amount);
        } else {
            //Give the money to the void
            return depositPlayer(player, amount);
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

    /**
     * Gets the receiver of a transaction for using the given portal
     *
     * @param origin <p>The portal used</p>
     * @return <p>The target account's UUID, or null for no receiver</p>
     */
    private UUID getTransactionReceiver(Portal origin) {
        if (origin != null && ConfigurationHelper.getBoolean(ConfigurationOption.GATE_OWNER_REVENUE)) {
            //If owner revenue is enabled, pay the portal owner
            return origin.getOwnerUUID();
        } else {
            //Pay the tax account if set
            String bankUUIDString = ConfigurationHelper.getString(ConfigurationOption.TAX_DESTINATION);
            if (!bankUUIDString.isEmpty()) {
                return UUID.fromString(bankUUIDString);
            } else {
                //Pay to the void
                return null;
            }
        }
    }

}
