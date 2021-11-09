package net.knarcraft.stargate.config;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.Portal;
import net.knarcraft.stargate.portal.property.gate.Gate;
import net.knarcraft.stargate.utility.PermissionHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import java.util.Map;
import java.util.UUID;

/**
 * The economy config keeps track of economy config values and performs economy actions such as payment for using a gate
 */
public final class EconomyConfig {

    private boolean economyEnabled = false;
    private Economy economy = null;
    private Plugin vault = null;
    private int useCost = 0;
    private int createCost = 0;
    private int destroyCost = 0;
    private boolean toOwner = false;
    private boolean chargeFreeDestination = true;
    private boolean freeGatesGreen = false;

    /**
     * Instantiates a new economy config
     *
     * @param configOptions <p>The loaded config options to read</p>
     */
    public EconomyConfig(Map<ConfigOption, Object> configOptions) {
        loadEconomyConfig(configOptions);
    }

    /**
     * Gets the cost of using a gate without a specified cost
     *
     * @return <p>The gate use cost</p>
     */
    public int getDefaultUseCost() {
        return useCost;
    }

    /**
     * Gets whether economy is enabled
     *
     * @return <p>Whether economy is enabled</p>
     */
    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    /**
     * Gets the economy object to use for transactions
     *
     * @return <p>An economy object, or null if economy is disabled or not initialized</p>
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Gets an instance of the Vault plugin
     *
     * @return <p>An instance of the Vault plugin, or null if Vault is not loaded</p>
     */
    public Plugin getVault() {
        return vault;
    }

    /**
     * Disables economy support by clearing relevant values
     */
    public void disableEconomy() {
        this.economy = null;
        this.vault = null;
    }

    /**
     * Gets whether free portals should be marked with green coloring
     *
     * @return <p>Whether free portals should be green</p>
     */
    public boolean drawFreePortalsGreen() {
        return freeGatesGreen;
    }

    /**
     * Whether a gate whose destination is a free gate is still charged
     *
     * <p>If teleporting from a free portal, it's free regardless of destination. If chargeFreeDestination is disabled,
     * it's also free to teleport back to the free portal. If chargeFreeDestination is enabled, it's only free to
     * teleport back if teleporting from another free portal.</p>
     *
     * @return <p>Whether to charge for free destinations</p>
     */
    public boolean chargeFreeDestination() {
        return chargeFreeDestination;
    }

    /**
     * Gets whether payments should be sent to the owner of the used portal
     *
     * @return <p>Whether to send payments to the portal owner</p>
     */
    public boolean sendPaymentToOwner() {
        return toOwner;
    }

    /**
     * Sets the cost of using a gate without a specified cost
     *
     * <p>The use cost cannot be negative.</p>
     *
     * @param useCost <p>The gate use cost</p>
     */
    public void setDefaultUseCost(int useCost) {
        if (useCost < 0) {
            throw new IllegalArgumentException("Using a gate cannot cost a negative amount");
        }
        this.useCost = useCost;
    }

    /**
     * Gets the cost of creating a gate without a specified cost
     *
     * @return <p>The gate creation cost</p>
     */
    public int getDefaultCreateCost() {
        return createCost;
    }

    /**
     * Sets the cost of creating a gate without a specified cost
     *
     * <p>The gate create cost cannot be negative</p>
     *
     * @param createCost <p>The gate creation cost</p>
     */
    public void setDefaultCreateCost(int createCost) {
        this.createCost = createCost;
    }

    /**
     * Gets the cost of destroying a gate without a specified cost
     *
     * @return <p>The gate destruction cost</p>
     */
    public int getDefaultDestroyCost() {
        return destroyCost;
    }

    /**
     * Sets the cost of destroying a gate without a specified cost
     *
     * @param destroyCost <p>The gate destruction cost</p>
     */
    public void setDefaultDestroyCost(int destroyCost) {
        this.destroyCost = destroyCost;
    }

    /**
     * Charges the player for an action, if required
     *
     * @param player <p>The player to take money from</p>
     * @param cost   <p>The cost of the transaction</p>
     * @return <p>True if the player was charged successfully</p>
     */
    public boolean chargePlayerIfNecessary(Player player, int cost) {
        if (skipPayment(cost)) {
            return true;
        }
        //Charge player
        return chargePlayer(player, cost);
    }

    /**
     * Checks whether the given player can afford the given fee
     *
     * @param player <p>The player to check</p>
     * @param cost   <p>The fee to pay</p>
     * @return <p>True if the player can afford to pay the fee</p>
     */
    public boolean canAffordFee(Player player, int cost) {
        return economy.getBalance(player) > cost;
    }

    /**
     * Charges the player for an action, if required
     *
     * @param player <p>The player to take money from</p>
     * @param target <p>The target to pay</p>
     * @param cost   <p>The cost of the transaction</p>
     * @return <p>True if the player was charged successfully</p>
     */
    public boolean chargePlayerIfNecessary(Player player, UUID target, int cost) {
        if (skipPayment(cost)) {
            return true;
        }
        //Charge player
        return chargePlayer(player, target, cost);
    }

    /**
     * Gets a formatted string for an amount, adding the name of the currency
     *
     * @param amount <p>The amount to display</p>
     * @return <p>A formatted text string describing the amount</p>
     */
    public String format(int amount) {
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
    public boolean setupEconomy(PluginManager pluginManager) {
        if (!economyEnabled) {
            return false;
        }
        //Check if vault is loaded
        Plugin vault = pluginManager.getPlugin("Vault");
        if (vault != null && vault.isEnabled()) {
            ServicesManager servicesManager = Stargate.getInstance().getServer().getServicesManager();
            RegisteredServiceProvider<Economy> economyProvider = servicesManager.getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                this.vault = vault;
                return true;
            } else {
                Stargate.logInfo(Stargate.getString("ecoLoadError"));
            }
        } else {
            Stargate.logInfo(Stargate.getString("vaultLoadError"));
        }
        economyEnabled = false;
        return false;
    }

    /**
     * Gets whether to use economy
     *
     * @return <p>True if the user has turned on economy and economy is available</p>
     */
    public boolean useEconomy() {
        return economyEnabled && economy != null;
    }

    /**
     * Checks whether a payment transaction should be skipped
     *
     * @param cost <p>The cost of the transaction</p>
     * @return <p>True if the transaction should be skipped</p>
     */
    private boolean skipPayment(int cost) {
        return cost == 0 || !useEconomy();
    }

    /**
     * Determines the cost of using a gate
     *
     * @param player      <p>The player trying to use the gate</p>
     * @param source      <p>The source/entry portal</p>
     * @param destination <p>The destination portal</p>
     * @return <p>The cost of using the portal</p>
     */
    public int getUseCost(Player player, Portal source, Portal destination) {
        //No payment required
        if (!useEconomy() || source.getOptions().isFree()) {
            return 0;
        }
        //Not charging for free destinations
        if (destination != null && !chargeFreeDestination && destination.getOptions().isFree()) {
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
     * Gets the cost of creating the given gate
     *
     * @param player <p>The player creating the gate</p>
     * @param gate   <p>The gate type used</p>
     * @return <p>The cost of creating the gate</p>
     */
    public int getCreateCost(Player player, Gate gate) {
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
    public int getDestroyCost(Player player, Gate gate) {
        if (isFree(player, "destroy")) {
            return 0;
        } else {
            return gate.getDestroyCost();
        }
    }

    /**
     * Loads all config values related to economy
     *
     * @param configOptions <p>The loaded config options to get values from</p>
     */
    private void loadEconomyConfig(Map<ConfigOption, Object> configOptions) {
        economyEnabled = (boolean) configOptions.get(ConfigOption.USE_ECONOMY);
        setDefaultCreateCost((Integer) configOptions.get(ConfigOption.CREATE_COST));
        setDefaultDestroyCost((Integer) configOptions.get(ConfigOption.DESTROY_COST));
        setDefaultUseCost((Integer) configOptions.get(ConfigOption.USE_COST));
        toOwner = (boolean) configOptions.get(ConfigOption.TO_OWNER);
        chargeFreeDestination = (boolean) configOptions.get(ConfigOption.CHARGE_FREE_DESTINATION);
        freeGatesGreen = (boolean) configOptions.get(ConfigOption.FREE_GATES_GREEN);
    }

    /**
     * Determines if a player can do a gate action for free
     *
     * @param player         <p>The player to check</p>
     * @param permissionNode <p>The free.permissionNode necessary to allow free gate {action}</p>
     * @return <p></p>
     */
    private boolean isFree(Player player, String permissionNode) {
        return !useEconomy() || PermissionHelper.hasPermission(player, "stargate.free." + permissionNode);
    }

    /**
     * Charges a player
     *
     * @param player <p>The player to charge</p>
     * @param amount <p>The amount to charge</p>
     * @return <p>True if the payment succeeded, or if no payment was necessary</p>
     */
    private boolean chargePlayer(Player player, double amount) {
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
    private boolean chargePlayer(Player player, UUID target, double amount) {
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
