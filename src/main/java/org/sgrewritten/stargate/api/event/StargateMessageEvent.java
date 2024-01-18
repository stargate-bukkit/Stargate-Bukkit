package org.sgrewritten.stargate.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.format.StargateComponent;

public class StargateMessageEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private StargateComponent message;
    private boolean cancelled;

    public StargateMessageEvent(StargateComponent message){
        this.message = message;
    }

    public StargateComponent getMessage(){
        return message;
    }

    public void setMessage(StargateComponent message){
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
