package org.sgrewritten.stargate.api.event.portal.message;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.event.portal.CancellableStargatePortalEvent;
import org.sgrewritten.stargate.api.network.portal.Portal;

public abstract class StargateSendMessagePortalEvent extends CancellableStargatePortalEvent {
    private final MessageType type;
    private final Entity entity;
    private String message;

    /**
     * Instantiates a new stargate player event
     *
     * <p> Can be called both synchronously and asynchronously! </p>
     *
     * @param portal <p>The portal involved in this stargate event</p>
     * @param entity <p>The entity receiving the message</p>
     * @param type   <p>The type of message being sent</p>
     */
    public StargateSendMessagePortalEvent(Portal portal, Entity entity, MessageType type, String message, boolean async) {
        super(portal, async);
        this.type = type;
        this.entity = entity;
        this.message = message;
    }

    /**
     * @return <p>The type of message to send</p>
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @return <p>The target entity</p>
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return <p>The message that will be sent</p>
     */
    public String getMessage(){
        return this.message;
    }

    /**
     * @param message <p>The message that will be sent</p>
     */
    public void setMessage(String message){
        this.message = message;
    }
}
