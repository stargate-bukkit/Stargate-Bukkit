package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.EconomyHandler;
import net.knarcraft.stargate.Stargate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginEventListener implements Listener {
    private final Stargate stargate;

    public PluginEventListener(Stargate stargate) {
        this.stargate = stargate;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (EconomyHandler.setupEconomy(stargate.getServer().getPluginManager())) {
            String vaultVersion = EconomyHandler.vault.getDescription().getVersion();
            Stargate.log.info(Stargate.getString("prefix") +
                    Stargate.replaceVars(Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(EconomyHandler.vault)) {
            Stargate.log.info(Stargate.getString("prefix") + "Vault plugin lost.");
        }
    }
}
