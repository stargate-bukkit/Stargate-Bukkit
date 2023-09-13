package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

public class StargateSendMessagePortalEvent extends CancellableStargatePortalEvent {
    private static HandlerList handlers = new HandlerList();
    private final MessageType type;
    private final Entity entity;

    /**
     * Instantiates a new stargate player event
     *
     * @param portal           <p>The portal involved in this stargate event</p>
     * @param entity <p>The entity receiving the message</p>
     * @param type  <p>The type of message being sent</p>
     */
    public StargateSendMessagePortalEvent(Portal portal, Entity entity, MessageType type) {
        super(portal);
        this.type = type;
        this.entity = entity;
    }

    public MessageType getType() {
        return type;
    }

    public Entity getEntity(){
        return entity;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return StargateSendMessagePortalEvent.handlers;
    }

    public enum MessageType {
        DESTROY, DESTINATION_EMPTY, DENY, TELEPORT
    }
}
