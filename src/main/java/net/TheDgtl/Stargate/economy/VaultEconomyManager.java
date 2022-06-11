package net.TheDgtl.Stargate.economy;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.LanguageManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * A class for managing economy transactions
 */
public class VaultEconomyManager extends EconomyManager {

    private Economy economy;
    private boolean hasVault;

    /**
     * Instantiates a new economy manager
     *
     * @param languageManager <p>The language manager to use for any messages</p>
     */
    public VaultEconomyManager(LanguageManager languageManager) {
        super(languageManager);
        setupEconomy();
    }

    @Override
    public boolean has(OfflinePlayer target, int amount) {
        if (!this.hasVault) {
            return true;
        }
        return this.economy.has(target, amount);
    }

    @Override
    public boolean chargePlayer(OfflinePlayer offlinePlayer, int amount) {
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
        boolean chargeSuccessful = response.transactionSuccess();
        if (chargeSuccessful) {
            //Tell the player that they have been charged
            sendChargeSuccessMessage(offlinePlayer, amount);
        }
        return chargeSuccessful;
    }

    @Override
    public boolean depositPlayer(OfflinePlayer player, int amount) {
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

    @Override
    public void setupEconomy() {
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

}
