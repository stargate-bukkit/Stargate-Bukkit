package org.sgrewritten.stargate.api.event.portal.message;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.event.portal.CancellableStargatePortalEvent;
import org.sgrewritten.stargate.api.network.portal.Portal;

public abstract class StargateSendMessagePortalEvent extends CancellableStargatePortalEvent {
    private final MessageType type;
    private final Entity entity;

    /**
     * Instantiates a new stargate player event
     *
     * <p> Can be called both synchronously and asynchronously! </p>
     *
     * @param portal <p>The portal involved in this stargate event</p>
     * @param entity <p>The entity receiving the message</p>
     * @param type   <p>The type of message being sent</p>
     */
    public StargateSendMessagePortalEvent(Portal portal, Entity entity, MessageType type, boolean async) {
        super(portal, async);
        this.type = type;
        this.entity = entity;
    }

    public MessageType getType() {
        return type;
    }

    public Entity getEntity() {
        return entity;
    }
}
