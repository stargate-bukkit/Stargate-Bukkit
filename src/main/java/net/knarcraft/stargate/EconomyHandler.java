package net.knarcraft.stargate;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/*
  stargate - A portal plugin for Bukkit
  Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
  Copyright (C) 2021 Kristian Knarvik
  <p>
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  <p>
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  <p>
  You should have received a copy of the GNU Lesser General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This handler handles economy actions such as payment for using a gate
 */
public class EconomyHandler {
    public static boolean economyEnabled = false;
    public static Economy economy = null;
    public static Plugin vault = null;

    public static int useCost = 0;
    public static int createCost = 0;
    public static int destroyCost = 0;
    public static boolean toOwner = false;
    public static boolean chargeFreeDestination = true;
    public static boolean freeGatesGreen = false;

    /**
     * Gets the balance (money) of the given player
     * @param player <p>The player to get balance for</p>
     * @return <p>The current balance of the player. Returns 0 if economy is disabled</p>
     */
    public static double getBalance(Player player) {
        if (economyEnabled) {
            return economy.getBalance(player);
        } else {
            return 0;
        }
    }

    /**
     * Charges a player, giving the charge to a target
     * @param player <p>The player to charge</p>
     * @param target <p>The UUID of the player to pay</p>
     * @param amount <p>The amount to charge</p>
     * @return <p>True if the payment succeeded, or if no payment was necessary</p>
     */
    public static boolean chargePlayer(Player player, UUID target, double amount) {
        if (economyEnabled && player.getUniqueId().compareTo(target) != 0 && economy != null) {
            if (!economy.has(player, amount)) {
                return false;
            }
            economy.withdrawPlayer(player, amount);
            economy.depositPlayer(Bukkit.getOfflinePlayer(target), amount);
        }
        return true;
    }

    /**
     * Charges a player
     * @param player <p>The player to charge</p>
     * @param amount <p>The amount to charge</p>
     * @return <p>True if the payment succeeded, or if no payment was necessary</p>
     */
    public static boolean chargePlayer(Player player, double amount) {
        if (economyEnabled && economy != null) {
            if (!economy.has(player, amount)) {
                return false;
            }
            economy.withdrawPlayer(player, amount);
        }
        return true;
    }

    /**
     * Gets a formatted string for an amount, adding the name of the currency
     * @param amount <p>The amount to display</p>
     * @return <p>A formatted text string describing the amount</p>
     */
    public static String format(int amount) {
        if (economyEnabled) {
            return economy.format(amount);
        } else {
            return "";
        }
    }

    /**
     * Sets up economy by initializing vault and the vault economy provider
     * @param pluginManager <p>The plugin manager to get plugins from</p>
     * @return <p>True if economy was enabled</p>
     */
    public static boolean setupEconomy(PluginManager pluginManager) {
        if (!economyEnabled) {
            return false;
        }
        // Check for Vault
        Plugin vault = pluginManager.getPlugin("Vault");
        if (vault != null && vault.isEnabled()) {
            RegisteredServiceProvider<Economy> economyProvider = Stargate.server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                EconomyHandler.vault = vault;
                return true;
            }
        }
        Stargate.log.info("[stargate] Economy is enabled but vault could not be loaded. Economy disabled");
        economyEnabled = false;
        return false;
    }

    /**
     * Gets whether to use economy
     * @return <p>True if the user has turned on economy and economy is available</p>
     */
    public static boolean useEconomy() {
        return economyEnabled && economy != null;
    }

}
