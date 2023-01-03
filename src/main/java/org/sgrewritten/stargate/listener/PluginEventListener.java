package org.sgrewritten.stargate.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.economy.EconomyManager;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Listens for and sets up any relevant plugins being loaded
 */
public class PluginEventListener implements Listener {

    private @NotNull StargateEconomyAPI economyManager;

    public PluginEventListener(@NotNull StargateEconomyAPI economyManager) {
        this.economyManager = Objects.requireNonNull(economyManager);
    }
    
    /**
     * Listens for any valid economy plugins being loaded and sets up economy if necessary
     *
     * @param event <p>The triggered plugin enable event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        if (isValidEconomyPlugin(event.getPlugin())) {
            economyManager.setupEconomy();
        }
    }

    /**
     * Listens for the economy manager plugin being unloaded and informs the console
     *
     * @param event <p>The triggered plugin unload event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(getEconomyPlugin())) {
            Stargate.log(Level.WARNING, "Vault plugin lost.");
        }
    }

    /**
     * Gets a Vault instance
     *
     * @return <p>A Vault instance</p>
     */
    private Plugin getEconomyPlugin() {
        return Bukkit.getPluginManager().getPlugin("Vault");
    }

    /**
     * Checks whether the given plugin is an instance of Vault
     *
     * @param plugin <p>The plugin to check</p>
     * @return <p>True if the plugin is an instance of Vault</p>
     */
    private boolean isValidEconomyPlugin(Plugin plugin) {
        Plugin vault = getEconomyPlugin();
        return vault != null && vault.equals(plugin);
    }

}
