package net.TheDgtl.Stargate.manager;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.logging.Level;

/**
 * A class for managing economy transactions
 */
public class EconomyManager {

    private final LanguageManager languageManager;
    private Economy economy;
    private boolean hasVault;

    /**
     * Instantiates a new economy manager
     *
     * @param languageManager <p>The language manager to use for any messages</p>
     */
    public EconomyManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
        if (!ConfigurationHelper.getBoolean(ConfigurationOption.USE_ECONOMY)) {
            hasVault = false;
            return;
        }
        hasVault = setupEconomyService();
        if (!hasVault) {
            Stargate.log(Level.WARNING, "Dependency ''Vault'' is unavailable; economy features are disabled");
        }
    }

    /**
     * Charges the given player
     *
     * @param player <p>The player to charge</p>
     * @param origin <p>The portal the player entered from</p>
     * @param amount <p>The amount to charge the player</p>
     * @return <p>True if there were no problems in processing the payment</p>
     */
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
            if (transactionTarget.getPlayer() != null) {
                String unFormattedMessage = languageManager.getMessage(TranslatableMessage.ECO_OBTAIN);
                String portalNameCompiledMessage = TranslatableMessageFormatter.formatPortal(unFormattedMessage, origin.getName());
                String message = TranslatableMessageFormatter.formatCost(portalNameCompiledMessage, amount);
                transactionTarget.getPlayer().sendMessage(message);
            }
            return true;
        } else {
            //Pay to the server
            return chargeAndTax(player, amount);
        }
    }

    /**
     * Money for the tax gods. May you live in wealth
     *
     * @param player <p>The player to charge and tax</p>
     * @param amount <p>The amount the player should be charged</p>
     * @return <p>True if the player was charged and/or taxed</p>
     */
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
     * Gets a Vault instance
     *
     * @return <p>A Vault instance</p>
     */
    public Plugin getEconomyPlugin() {
        return Bukkit.getPluginManager().getPlugin("Vault");
    }

    /**
     * Checks whether the given plugin is an instance of Vault
     *
     * @param plugin <p>The plugin to check</p>
     * @return <p>True if the plugin is an instance of Vault</p>
     */
    public boolean isValidEconomyPlugin(Plugin plugin) {
        Plugin vault = getEconomyPlugin();
        return vault != null && vault.equals(plugin);
    }

    /**
     * Sets up economy to make it ready for transactions
     */
    public void setupEconomy() {
        hasVault = setupEconomyService();
    }

    /**
     * Check if player has enough money
     *
     * @param target <p>The player to check</p>
     * @param amount <p>The amount required</p>
     * @return <p>True if the player has the required amount</p>
     */
    public boolean has(OfflinePlayer target, int amount) {
        if (!this.hasVault) {
            return true;
        }
        return this.economy.has(target, amount);
    }

    /**
     * Hooks into Vault to prepare for Economy
     *
     * @return <p>True if economy is ready to be used</p>
     */
    private boolean setupEconomyService() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            return false;
        }
        economy = economyProvider.getProvider();
        return true;
    }

    /**
     * Charges the given player and deposits it into the other given player's account
     *
     * @param player            <p>The player to charge</p>
     * @param transactionTarget <p>The player to receive the payment</p>
     * @param amount            <p>The amount the player should be charged</p>
     * @return <p>True if there was no problems with the payment</p>
     */
    private boolean chargeAndDepositPlayer(OfflinePlayer player, OfflinePlayer transactionTarget, int amount) {
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
     * Charges a player if possible
     *
     * @param offlinePlayer <p>The player to charge</p>
     * @param amount        <p>The amount the player should be charged</p>
     * @return <p>True if the payment was fulfilled</p>
     */
    private boolean chargePlayer(OfflinePlayer offlinePlayer, int amount) {
        //Skip if no payment is necessary
        if (amount == 0) {
            return true;
        }
        if (!hasVault) {
            Stargate.log(Level.FINE, "Skipping player payment");
            return true;
        } else {
            Stargate.log(Level.FINE, "Charging player " + amount);
        }

        EconomyResponse response = economy.withdrawPlayer(offlinePlayer, amount);
        Player player = offlinePlayer.getPlayer();
        boolean successfullyCharged = response.transactionSuccess();
        if (player != null && successfullyCharged) {
            //Tell the player that they have been charged
            String unformattedMessage = languageManager.getMessage(TranslatableMessage.ECO_DEDUCT);
            String message = TranslatableMessageFormatter.formatCost(unformattedMessage, amount);
            player.sendMessage(message);
        }
        return successfullyCharged;
    }

    /**
     * Deposits money in a player's account if possible
     *
     * @param player <p>The player receiving the money</p>
     * @param amount <p>The amount to receive</p>
     * @return <p>True if the payment was fulfilled</p>
     */
    private boolean depositPlayer(OfflinePlayer player, int amount) {
        //Skip if no payment is necessary
        if (amount == 0) {
            return true;
        }
        if (!hasVault) {
            Stargate.log(Level.FINE, "Skipping player payment");
            return true;
        } else {
            Stargate.log(Level.FINE, "Depositing player " + amount);
        }

        EconomyResponse response = economy.depositPlayer(player, amount);
        if (!response.transactionSuccess() && !economy.hasAccount(player)) {
            /* If the transaction is unsuccessful, and the player is missing an account, create a new account for the 
            player and try again. */
            economy.createPlayerAccount(player);
            response = economy.depositPlayer(player, amount);
        }
        return response.transactionSuccess();
    }

}
