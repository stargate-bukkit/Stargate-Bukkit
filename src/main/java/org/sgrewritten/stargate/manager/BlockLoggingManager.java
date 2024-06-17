package org.sgrewritten.stargate.manager;

import org.bukkit.event.player.PlayerInteractEvent;

public interface BlockLoggingManager {

    /**
     * Start providing support for any block logger, if present
     */
    void setUpLogging();

    /**
     * @param event <p>The event to try and logg to block logger</p>
     */
    void logPlayerInteractEvent(PlayerInteractEvent event);
}
