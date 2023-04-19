package net.knarcraft.stargate.config;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.portal.PortalSignDrawer;
import net.knarcraft.stargate.portal.property.gate.Gate;
import net.knarcraft.stargate.utility.PermissionHelper;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import java.util.Map;

/**
 * The economy config keeps track of economy config values and performs economy actions such as payment for using a gate
 */
public final class EconomyConfig {

    private Economy economy = null;
    private Plugin vault = null;

    private final Map<ConfigOption, Object> configOptions;

    /**
     * Instantiates a new economy config
     *
     * @param configOptions <p>The loaded config options to read</p>
     */
    public EconomyConfig(Map<ConfigOption, Object> configOptions) {
        this.configOptions = configOptions;
        try {
            String freeColor = (String) configOptions.get(ConfigOption.FREE_GATES_COLOR);
            PortalSignDrawer.setFreeColor(ChatColor.of(freeColor.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            PortalSignDrawer.setFreeColor(ChatColor.DARK_GREEN);
        }
    }

    /**
     * Gets the cost of using a gate without a specified cost
     *
     * @return <p>The gate use cost</p>
     */
    public int getDefaultUseCost() {
        return (Integer) configOptions.get(ConfigOption.USE_COST);
    }

    /**
     * Gets whether economy is enabled
     *
     * @return <p>Whether economy is enabled</p>
     */
    public boolean isEconomyEnabled() {
        return (boolean) configOptions.get(ConfigOption.USE_ECONOMY);
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
     * Gets whether free portals should be marked with a different coloring
     *
     * @return <p>Whether free portals should be colored</p>
     */
    public boolean drawFreePortalsColored() {
        return (boolean) configOptions.get(ConfigOption.FREE_GATES_COLORED);
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
    public boolean freeIfFreeDestination() {
        return !((boolean) configOptions.get(ConfigOption.CHARGE_FREE_DESTINATION));
    }

    /**
     * Gets whether payments should be sent to the owner of the used portal
     *
     * @return <p>Whether to send payments to the portal owner</p>
     */
    public boolean sendPaymentToOwner() {
        return (boolean) configOptions.get(ConfigOption.TO_OWNER);
    }

    /**
     * Gets the cost of creating a gate without a specified cost
     *
     * @return <p>The gate creation cost</p>
     */
    public int getDefaultCreateCost() {
        return (Integer) configOptions.get(ConfigOption.CREATE_COST);
    }

    /**
     * Gets the cost of destroying a gate without a specified cost
     *
     * @return <p>The gate destruction cost</p>
     */
    public int getDefaultDestroyCost() {
        return (Integer) configOptions.get(ConfigOption.DESTROY_COST);
    }

    /**
     * Gets the account all taxes are paid to
     *
     * @return <p>The account all taxes are paid to</p>
     */
    public String getTaxAccount() {
        return (String) configOptions.get(ConfigOption.TAX_ACCOUNT);
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
     * Gets a formatted string for an amount, adding the name of the currency
     *
     * @param amount <p>The amount to display</p>
     * @return <p>A formatted text string describing the amount</p>
     */
    public String format(int amount) {
        if (isEconomyEnabled()) {
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
        if (!isEconomyEnabled()) {
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
        configOptions.put(ConfigOption.USE_ECONOMY, false);
        return false;
    }

    /**
     * Gets whether to use economy
     *
     * @return <p>True if the user has turned on economy and economy is available</p>
     */
    public boolean useEconomy() {
        return isEconomyEnabled() && economy != null;
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
     * Determines if a player can do a gate action for free
     *
     * @param player         <p>The player to check</p>
     * @param permissionNode <p>The free.permissionNode necessary to allow free gate {action}</p>
     * @return <p></p>
     */
    private boolean isFree(Player player, String permissionNode) {
        return !useEconomy() || PermissionHelper.hasPermission(player, "stargate.free." + permissionNode);
    }

}
