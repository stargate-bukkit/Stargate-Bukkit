package org.sgrewritten.stargate.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StargateReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
