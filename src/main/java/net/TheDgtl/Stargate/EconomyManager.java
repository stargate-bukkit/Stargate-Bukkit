package net.TheDgtl.Stargate;

import net.TheDgtl.Stargate.config.setting.Setting;
import net.TheDgtl.Stargate.config.setting.Settings;
import net.TheDgtl.Stargate.network.portal.Portal;
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
        if (!Settings.getBoolean(Setting.USE_ECONOMY)) {
            hasVault = false;
            return;
        }
        hasVault = setupEconomy();
        if (!hasVault)
            Stargate.log(Level.WARNING, "Dependency ''Vault'' is unavailable; economy features are disabled");
    }

    /**
     * @param player
     * @param amount
     * @return if player had enough money for transaction
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
            String message = TranslatableMessageFormatter.compileCost(unformattedMessage, amount);
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
     * @param player
     * @param amount
     * @return
     */
    public boolean chargeAndTax(OfflinePlayer player, int amount) {
        if (amount == 0)
            return true;
        String bankUUIDStr = Settings.getString(Setting.TAX_DESTINATION);
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
        if (Settings.getBoolean(Setting.GATE_OWNER_REVENUE)) {
            if (chargeAndDepositPlayer(player, Bukkit.getServer().getOfflinePlayer(origin.getOwnerUUID()), amount)) {
                if (player.getPlayer() != null) {
                    String unCompiledMessage = Stargate.languageManager.getMessage(TranslatableMessage.ECO_OBTAIN);
                    String portalNameCompiledMessage = TranslatableMessageFormatter.compilePortal(unCompiledMessage, origin.getName());
                    String message = TranslatableMessageFormatter.compileCost(portalNameCompiledMessage, amount);
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
            depositPlayer(transactionTarget, amount);
            return true;
        }
        return false;
    }
    
}
