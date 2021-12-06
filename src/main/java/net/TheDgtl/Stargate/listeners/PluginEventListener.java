package net.TheDgtl.Stargate.listeners;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import net.TheDgtl.Stargate.Stargate;

public class PluginEventListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        if(Stargate.economyManager.isValidEconomyPlugin(  event.getPlugin())) {
            Stargate.economyManager.setEconomy(event.getPlugin());
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(Stargate.economyManager.getEconomyPlugin())) {
            Stargate.log(Level.INFO,"Vault plugin lost."); 
        }
    }
}
