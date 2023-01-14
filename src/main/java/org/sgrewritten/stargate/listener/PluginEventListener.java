package org.sgrewritten.stargate.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.Stargate;
import org.sgrewritten.stargate.economy.StargateEconomyAPI;
import org.sgrewritten.stargate.manager.BlockLoggingManager;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Listens for and sets up any relevant plugins being loaded
 */
public class PluginEventListener implements Listener {

    private @NotNull StargateEconomyAPI economyManager;
    private @NotNull BlockLoggingManager blockLoggingManager;

    public PluginEventListener(@NotNull StargateEconomyAPI economyManager, @NotNull BlockLoggingManager blockLoggingManager) {
        this.economyManager = Objects.requireNonNull(economyManager);
        this.blockLoggingManager = Objects.requireNonNull(blockLoggingManager);
    }
    
    /**
     * Listens for any valid economy plugins being loaded and sets up economy if necessary
     *
     * @param event <p>The triggered plugin enable event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        if (isEconomyPlugin(event.getPlugin())) {
            economyManager.setupEconomy();
        }
        if(isLoggerPlugin(event.getPlugin())) {
            blockLoggingManager.setUpLogging();
        }
    }

    /**
     * Listens for the economy manager plugin being unloaded and informs the console
     *
     * @param event <p>The triggered plugin unload event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        if (isEconomyPlugin(event.getPlugin())) {
            Stargate.log(Level.WARNING, "Vault plugin lost.");
        }
        if(isLoggerPlugin(event.getPlugin())) {
            Stargate.log(Level.WARNING, "CoreProtect plugin lost.");
        }
    }

    private boolean isEconomyPlugin(Plugin plugin) {
        return plugin.getName().equals("Vault");
    }
    
    private boolean isLoggerPlugin(Plugin plugin) {
        return plugin.getName().equals("CoreProtect");
    }
}
