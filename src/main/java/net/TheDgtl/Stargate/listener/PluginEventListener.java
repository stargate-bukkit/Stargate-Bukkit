package net.TheDgtl.Stargate.listener;

import net.TheDgtl.Stargate.Stargate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.logging.Level;

/**
 * Listens for and sets up any relevant plugins being loaded
 */
public class PluginEventListener implements Listener {

    /**
     * Listens for any valid economy plugins being loaded and sets up economy if necessary
     *
     * @param event <p>The triggered plugin enable event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        if (Stargate.getEconomyManager().isValidEconomyPlugin(event.getPlugin())) {
            Stargate.getEconomyManager().setupEconomy();
        }
    }

    /**
     * Listens for the economy manager plugin being unloaded and informs the console
     *
     * @param event <p>The triggered plugin unload event</p>
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(Stargate.getEconomyManager().getEconomyPlugin())) {
            Stargate.log(Level.WARNING, "Vault plugin lost.");
        }
    }

}
