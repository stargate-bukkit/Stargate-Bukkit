package org.sgrewritten.stargate.manager;

import org.bukkit.event.player.PlayerInteractEvent;

public interface BlockLoggingManager {
    
    void setUpLogging();
    
    void logPlayerInteractEvent(PlayerInteractEvent event);
}
