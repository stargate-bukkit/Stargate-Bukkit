package org.sgrewritten.stargate.api.event;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * An abstract event describing any stargate event where an entity is involved
 */
@SuppressWarnings("unused")
public abstract class StargateEntityEvent extends StargateEvent {

    private final Entity involvedEntity;

    /**
     * Instantiates a new stargate player event
     *
     * @param portal         <p>The portal involved in this stargate event</p>
     * @param involvedEntity <p>The entity involved in the event</p>
     */
    StargateEntityEvent(Portal portal, Entity involvedEntity) {
        super(portal);

        this.involvedEntity = involvedEntity;
    }

    /**
     * Gets the entity involved in this event
     *
     * @return <p>The entity involved in this event</p>
     */
    public Entity getEntity() {
        return involvedEntity;
    }

}
