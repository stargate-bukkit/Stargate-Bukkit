package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.entity.Entity;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * An abstract event describing any stargate event where a player is involved
 */
@SuppressWarnings("unused")
public abstract class StargateEntityPortalEvent extends StargatePortalEvent {

    private final Entity travellingEntity;

    /**
     * Instantiates a new stargate player event
     *
     * @param portal           <p>The portal involved in this stargate event</p>
     * @param travellingEntity <p>The entity travelling through a portal</p>
     */
    StargateEntityPortalEvent(Portal portal, Entity travellingEntity, boolean async) {
        super(portal, async);

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
