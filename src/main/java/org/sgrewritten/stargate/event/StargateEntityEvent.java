package org.sgrewritten.stargate.event;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * An abstract event describing any stargate event where a player is involved
 */
@SuppressWarnings("unused")
public abstract class StargateEntityEvent extends StargateEvent {

    private final Entity travellingEntity;

    /**
     * Instantiates a new stargate player event
     *
     * @param portal           <p>The portal involved in this stargate event</p>
     * @param travellingEntity <p>The entity travelling through a portal</p>
     */
    StargateEntityEvent(Portal portal, Entity travellingEntity) {
        super(portal);

        this.travellingEntity = travellingEntity;
    }

    /**
     * Gets the entity involved in this event
     *
     * @return <p>The entity involved in this event</p>
     */
    public Entity getEntity() {
        return travellingEntity;
    }

}
