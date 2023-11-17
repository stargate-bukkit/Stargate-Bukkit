package org.sgrewritten.stargate.api.event.portal;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.sgrewritten.stargate.api.network.portal.Portal;

/**
 * This event should be called whenever a player attempts to access a stargate
 *
 * <p>This event is triggered whenever a player enters or activates a stargate. This event can be used to override
 * whether the player should be allowed to access the stargate.</p>
 */
@SuppressWarnings("unused")
public class StargateAccessPortalEvent extends DeniableStargatePortalEvent {

    private static final HandlerList handlers = new HandlerList();
    private String denyReason;
    private Entity travellingEntity;

    /**
     * Instantiates a new stargate access event
     *
     * @param travellingEntity <p>The entity travelling through a portal</p>
     * @param portal           <p>The portal involved in the event</p>
     * @param deny             <p>Whether the stargate access should be denied</p>
     * @param denyReason       <p>The reason stargate access was denied</p>
     */
    public StargateAccessPortalEvent(Entity travellingEntity, Portal portal, boolean deny, String denyReason) {
        super(portal, travellingEntity, deny, denyReason);
    }

    /**
     * Gets a handler-list containing all event handlers
     *
     * @return <p>A handler-list with all event handlers</p>
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

}
