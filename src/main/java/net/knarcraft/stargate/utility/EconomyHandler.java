package net.knarcraft.stargate.utility;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Gate;
import net.knarcraft.stargate.portal.Portal;
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
    public static int getDefaultUseCost() {
        return useCost;
    }

    /**
     * Sets the cost of using a gate without a specified cost
     *
     * <p>The use cost cannot be negative.</p>
     *
     * @param useCost <p>The gate use cost</p>
     */
    public static void setDefaultUseCost(int useCost) {
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
    public static int getDefaultCreateCost() {
        return createCost;
    }

    /**
     * Sets the cost of creating a gate without a specified cost
     *
     * <p>The gate create cost cannot be negative</p>
     *
     * @param createCost <p>The gate creation cost</p>
     */
    public static void setDefaultCreateCost(int createCost) {
        EconomyHandler.createCost = createCost;
    }

    /**
     * Gets the cost of destroying a gate without a specified cost
     *
     * @return <p>The gate destruction cost</p>
     */
    public static int getDefaultDestroyCost() {
        return destroyCost;
    }

    /**
     * Sets the cost of destroying a gate without a specified cost
     *
     * @param destroyCost <p>The gate destruction cost</p>
     */
    public static void setDefaultDestroyCost(int destroyCost) {
        EconomyHandler.destroyCost = destroyCost;
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
        return EconomyHandler.chargePlayer(player, cost);
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
        return EconomyHandler.chargePlayer(player, target, cost);
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
        //Check if vault is loaded
        Plugin vault = pluginManager.getPlugin("Vault");
        if (vault != null && vault.isEnabled()) {
            RegisteredServiceProvider<Economy> economyProvider = Stargate.server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                EconomyHandler.vault = vault;
                return true;
            } else {
                Stargate.logger.info(Stargate.getString("prefix") + Stargate.getString("ecoLoadError"));
            }
        } else {
            Stargate.logger.info(Stargate.getString("prefix") + Stargate.getString("vaultLoadError"));
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

    /**
     * Checks whether a payment transaction should be skipped
     *
     * @param cost <p>The cost of the transaction</p>
     * @return <p>True if the transaction should be skipped</p>
     */
    private static boolean skipPayment(int cost) {
        return cost == 0 || !EconomyHandler.useEconomy();
    }

    /**
     * Determines the cost of using a gate
     *
     * @param player      <p>The player trying to use the gate</p>
     * @param source      <p>The source/entry portal</p>
     * @param destination <p>The destination portal</p>
     * @return <p>The cost of using the portal</p>
     */
    public static int getDefaultUseCost(Player player, Portal source, Portal destination) {
        //No payment required
        if (!EconomyHandler.useEconomy() || source.getOptions().isFree()) {
            return 0;
        }
        //Not charging for free destinations
        if (destination != null && !EconomyHandler.chargeFreeDestination && destination.getOptions().isFree()) {
            return 0;
        }
        //Cost is 0 if the player owns this gate and funds go to the owner
        if (source.getGate().getToOwner() && source.isOwner(player)) {
            return 0;
        }
        //Player gets free gate use
        if (PermissionHelper.hasPermission(player, "stargate.free") ||
                PermissionHelper.hasPermission(player, "stargate.free.use")) {
            return 0;
        }

        return source.getGate().getUseCost();
    }

    /**
     * Gets the cost of creating the given gate
     *
     * @param player <p>The player creating the gate</p>
     * @param gate   <p>The gate type used</p>
     * @return <p>The cost of creating the gate</p>
     */
    public static int getCreateCost(Player player, Gate gate) {
        if (isFree(player, "create")) {
            return 0;
        } else {
            return gate.getCreateCost();
        }
    }

    /**
     * Gets the cost of destroying the given gate
     *
     * @param player <p>The player creating the gate</p>
     * @param gate   <p>The gate type used</p>
     * @return <p>The cost of destroying the gate</p>
     */
    public static int getDestroyCost(Player player, Gate gate) {
        if (isFree(player, "destroy")) {
            return 0;
        } else {
            return gate.getDestroyCost();
        }
    }

    /**
     * Determines if a player can do a gate action for free
     *
     * @param player         <p>The player to check</p>
     * @param permissionNode <p>The free.permissionNode necessary to allow free gate {action}</p>
     * @return <p></p>
     */
    private static boolean isFree(Player player, String permissionNode) {
        return !EconomyHandler.useEconomy() || PermissionHelper.hasPermission(player, "stargate.free") ||
                PermissionHelper.hasPermission(player, "stargate.free." + permissionNode);
    }

    /**
     * Charges a player
     *
     * @param player <p>The player to charge</p>
     * @param amount <p>The amount to charge</p>
     * @return <p>True if the payment succeeded, or if no payment was necessary</p>
     */
    private static boolean chargePlayer(Player player, double amount) {
        if (economyEnabled && economy != null) {
            if (!economy.has(player, amount)) {
                return false;
            }
            economy.withdrawPlayer(player, amount);
        }
        return true;
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

}
