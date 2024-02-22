package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.Stargate;
import net.knarcraft.stargate.config.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This listener listens for any plugins being enabled or disabled to catch the loading of vault
 */
@SuppressWarnings("unused")
public class PluginEventListener implements Listener {

    private final Stargate stargate;

    /**
     * Instantiates a new plugin event listener
     *
     * @param stargate <p>A reference to the stargate plugin to </p>
     */
    public PluginEventListener(@NotNull Stargate stargate) {
        this.stargate = stargate;
    }

    /**
     * This event listens for and announces that the vault plugin was detected and enabled
     *
     * <p>Each time this event is called, the economy handler will try to enable vault</p>
     *
     * @param ignored <p>The actual event called. This is currently not used</p>
     */
    @EventHandler
    public void onPluginEnable(@NotNull PluginEnableEvent ignored) {
        if (Stargate.getEconomyConfig().setupEconomy(stargate.getServer().getPluginManager())) {
            Plugin vault = Stargate.getEconomyConfig().getVault();
            if (vault != null) {
                String vaultVersion = vault.getDescription().getVersion();
                Stargate.logInfo(Stargate.replacePlaceholders(Stargate.getString(Message.VAULT_LOADED), "%version%",
                        vaultVersion));
            }
        }
    }

    /**
     * This event listens for the vault plugin being disabled and notifies the console
     *
     * @param event <p>The event caused by disabling a plugin</p>
     */
    @EventHandler
    public void onPluginDisable(@NotNull PluginDisableEvent event) {
        if (event.getPlugin().equals(Stargate.getEconomyConfig().getVault())) {
            Stargate.logInfo("Vault plugin lost.");
        }
    }

}
