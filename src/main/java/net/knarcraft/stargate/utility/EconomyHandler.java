package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/**
 * This handler handles economy actions such as payment for using a gate
 */
public final class EconomyHandler {

    public static boolean economyEnabled = false;
    public static Economy economy = null;
    public static Plugin vault = null;
    private static int useCost = 0;
    private static int createCost = 0;
    private static int destroyCost = 0;
    public static boolean toOwner = false;
    public static boolean chargeFreeDestination = true;
    public static boolean freeGatesGreen = false;

    /**
     * Gets the cost of using a gate without a specified cost
     *
     * @return <p>The gate use cost</p>
     */
    public static int getUseCost() {
        return useCost;
    }

    /**
     * Sets the cost of using a gate without a specified cost
     *
     * <p>The use cost cannot be negative.</p>
     *
     * @param useCost <p>The gate use cost</p>
     */
    public static void setUseCost(int useCost) {
        if (useCost < 0) {
            throw new IllegalArgumentException("Using a gate cannot cost a negative amount");
        }
        EconomyHandler.useCost = useCost;
    }

    /**
     * Gets the cost of creating a gate without a specified cost
     *
     * @return <p>The gate creation cost</p>
     */
    public static int getCreateCost() {
        return createCost;
    }

    /**
     * Sets the cost of creating a gate without a specified cost
     *
     * <p>The gate create cost cannot be negative</p>
     *
     * @param createCost <p>The gate creation cost</p>
     */
    public static void setCreateCost(int createCost) {
        EconomyHandler.createCost = createCost;
    }

    /**
     * Gets the cost of destroying a gate without a specified cost
     *
     * @return <p>The gate destruction cost</p>
     */
    public static int getDestroyCost() {
        return destroyCost;
    }

    /**
     * Sets the cost of destroying a gate without a specified cost
     *
     * @param destroyCost <p>The gate destruction cost</p>
     */
    public static void setDestroyCost(int destroyCost) {
        EconomyHandler.destroyCost = destroyCost;
    }

    /**
     * Gets the balance (money) of the given player
     *
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
     *
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
            //Take money from the user and give to the owner
            economy.withdrawPlayer(player, amount);
            economy.depositPlayer(Bukkit.getOfflinePlayer(target), amount);
        }
        return true;
    }

    /**
     * Charges a player
     *
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
     *
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
     *
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
            } else {
                Stargate.log.info(Stargate.getString("prefix") + Stargate.getString("ecoLoadError"));
            }
        } else {
            Stargate.log.info(Stargate.getString("prefix") + Stargate.getString("vaultLoadError"));
        }
        economyEnabled = false;
        return false;
    }

    /**
     * Gets whether to use economy
     *
     * @return <p>True if the user has turned on economy and economy is available</p>
     */
    public static boolean useEconomy() {
        return economyEnabled && economy != null;
    }

}
