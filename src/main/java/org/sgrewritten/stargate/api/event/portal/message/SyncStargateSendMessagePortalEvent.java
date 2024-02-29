package org.sgrewritten.stargate.api.event.portal.message;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.formatting.StargateComponent;

public class SyncStargateSendMessagePortalEvent extends StargateSendMessagePortalEvent {
    private static final HandlerList handlers = new HandlerList();

    /**
     * Instantiates a new stargate player event
     *
     * <p>Called synchronously with the bukkit primary thread</p>
     *
     * @param portal <p>The portal involved in this stargate event</p>
     * @param entity <p>The entity receiving the message</p>
     * @param type   <p>The type of message being sent</p>
     */
    public SyncStargateSendMessagePortalEvent(Portal portal, Entity entity, MessageType type, StargateComponent message) {
        super(portal, entity, type, message, false);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return SyncStargateSendMessagePortalEvent.handlers;
    }
}
