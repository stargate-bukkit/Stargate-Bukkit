package net.TheDgtl.Stargate.manager;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.util.TranslatableMessageFormatter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.logging.Level;

public class EconomyManager {

    private Economy econ;
    private boolean hasVault;

    public EconomyManager() {
        if (!ConfigurationHelper.getBoolean(ConfigurationOption.USE_ECONOMY)) {
            hasVault = false;
            return;
        }
        hasVault = setupEconomy();
        if (!hasVault) {
            Stargate.log(Level.WARNING, "Dependency ''Vault'' is unavailable; economy features are disabled");
        }
    }

    /**
     * Charges a player if possible
     *
     * @param player <p>The player to charge</p>
     * @param amount <p>The amount the player should be charged</p>
     * @return <p>True if the payment was fulfilled</p>
     */
    public boolean chargePlayer(OfflinePlayer player, int amount) {
        if (amount == 0) {
            return true;
        }
        Stargate.log(Level.FINE, "Charging player " + amount);
        if (!hasVault) {
            return true;
        }

        EconomyResponse response = econ.withdrawPlayer(player, amount);
        if (player.getPlayer() != null) {
            String unformattedMessage = Stargate.languageManager.getMessage(TranslatableMessage.ECO_DEDUCT);
            String message = TranslatableMessageFormatter.formatCost(unformattedMessage, amount);
            player.getPlayer().sendMessage(message);
        }
        return response.transactionSuccess();
    }

    public boolean depositPlayer(OfflinePlayer player, int amount) {
        if (amount == 0) {
            return true;
        }
        Stargate.log(Level.FINE, "Depositing player " + amount);
        if (!hasVault) {
            return true;
        }
        EconomyResponse response = econ.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            econ.createPlayerAccount(player);
            return response.transactionSuccess();
        }
        return true;
    }

    /**
     * Money for the tax gods. May you live in wealth
     *
     * @param player <p>The player to charge and tax</p>
     * @param amount <p>The amount the player should be charged</p>
     * @return <p>True if the player was charged and/or taxed</p>
     */
    public boolean chargeAndTax(OfflinePlayer player, int amount) {
        if (amount == 0) {
            return true;
        }
        String bankUUIDStr = ConfigurationHelper.getString(ConfigurationOption.TAX_DESTINATION);
        if (!bankUUIDStr.isEmpty()) {
            OfflinePlayer bankAccount = Bukkit.getOfflinePlayer(UUID.fromString(bankUUIDStr));
            return chargeAndDepositPlayer(player, bankAccount, amount);
        }
        return chargePlayer(player, amount);
    }

    public boolean chargePlayer(OfflinePlayer player, Portal origin, int amount) {
        if (amount == 0) {
            return true;
        }
        if (origin.getOwnerUUID() == player.getUniqueId()
                && ConfigurationHelper.getBoolean(ConfigurationOption.GATE_OWNER_REVENUE)
                && origin.hasFlag(PortalFlag.PERSONAL_NETWORK)) {
            return true;
        }
        if (ConfigurationHelper.getBoolean(ConfigurationOption.GATE_OWNER_REVENUE)) {
            if (chargeAndDepositPlayer(player, Bukkit.getServer().getOfflinePlayer(origin.getOwnerUUID()), amount)) {
                if (player.getPlayer() != null) {
                    String unFormattedMessage = Stargate.languageManager.getMessage(TranslatableMessage.ECO_OBTAIN);
                    String portalNameCompiledMessage = TranslatableMessageFormatter.formatPortal(unFormattedMessage, origin.getName());
                    String message = TranslatableMessageFormatter.formatCost(portalNameCompiledMessage, amount);
                    player.getPlayer().sendMessage(message);
                }
                return true;
            }
            return false;
        }
        return chargeAndTax(player, amount);
    }

    public Plugin getEconomyPlugin() {
        return Bukkit.getPluginManager().getPlugin("Vault");
    }

    public boolean isValidEconomyPlugin(Plugin plugin) {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        return vault != null && vault.equals(plugin);
    }

    public void setEconomy() {
        hasVault = setupEconomy();
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
            return false;
        }
        econ = economyProvider.getProvider();
        return true;
    }

    private boolean chargeAndDepositPlayer(OfflinePlayer player, OfflinePlayer transactionTarget, int amount) {
        if (amount == 0) {
            return true;
        }

        if (chargePlayer(player, amount)) {
            return depositPlayer(transactionTarget, amount);
        }
        return false;
    }

}
