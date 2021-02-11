package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StargateDeactivateEvent extends StargateEvent {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public StargateDeactivateEvent(Portal portal) {
        super("StargatDeactivateEvent", portal);

    }
}
