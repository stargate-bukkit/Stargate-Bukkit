package net.knarcraft.stargate.event;

import net.knarcraft.stargate.Portal;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StargateCloseEvent extends StargateEvent {

    private boolean force;

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public StargateCloseEvent(Portal portal, boolean force) {
        super("StargateCloseEvent", portal);

        this.force = force;
    }

    public boolean getForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

}
