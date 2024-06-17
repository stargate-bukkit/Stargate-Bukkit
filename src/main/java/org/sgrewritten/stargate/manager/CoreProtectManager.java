package org.sgrewritten.stargate.manager;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

/**
 * Logs certain events to coreprotect
 */
public class CoreProtectManager implements BlockLoggingManager {

    private CoreProtectAPI coreProtect;

    public CoreProtectManager() {
        this.setUpLogging();
    }

    @Override
    public void logPlayerInteractEvent(PlayerInteractEvent event) {
        if (coreProtect != null && !coreProtect.isEnabled()) {
            coreProtect.logInteraction(event.getPlayer().getName(), event.getInteractionPoint());
        }

    }

    @Override
    public void setUpLogging() {
        loadCoreProtect();
    }


    private void loadCoreProtect() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return;
        }
        coreProtect = ((CoreProtect) plugin).getAPI();
    }
}
