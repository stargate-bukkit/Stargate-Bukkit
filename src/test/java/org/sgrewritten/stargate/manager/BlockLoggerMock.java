package org.sgrewritten.stargate.manager;

import org.bukkit.event.player.PlayerInteractEvent;

public class BlockLoggerMock implements BlockLoggingManager {

    private boolean setUpIsTriggered;

    @Override
    public void setUpLogging() {
        setUpIsTriggered = true;
    }

    @Override
    public void logPlayerInteractEvent(PlayerInteractEvent event) {

    }

    public boolean hasTriggeredSetup() {
        return this.setUpIsTriggered;
    }
}
