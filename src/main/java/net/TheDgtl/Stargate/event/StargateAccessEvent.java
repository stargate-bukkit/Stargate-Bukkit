package net.TheDgtl.Stargate.event;

import net.TheDgtl.Stargate.network.portal.Portal;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event should be called whenever a player attempts to access a stargate
 *
 * <p>This event is triggered whenever a player enters or activates a stargate. This event can be used to override
 * whether the player should be allowed to access the stargate.</p>
 */
@SuppressWarnings("unused")
public class StargateAccessEvent extends StargateEntityEvent {

    private static final HandlerList handlers = new HandlerList();
    private boolean deny;
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
    public StargateAccessEvent(Entity travellingEntity, Portal portal, boolean deny, String denyReason) {
        super(portal, travellingEntity);

        this.deny = deny;
    }

    /**
     * Gets whether the entity should be denied access
     *
     * @return <p>Whether the entity should be denied access</p>
     */
    public boolean getDeny() {
        return this.deny;
    }

    /**
     * Sets whether to deny access to the entity
     *
     * @param deny <p>Whether to deny access to the entity</p>
     */
    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    /**
     * Gets the reason the stargate access was denied
     *
     * @return <p>The reason the stargate access was denied</p>
     */
    public String getDenyReason() {
        return denyReason;
    }

    /**
     * Sets the reason the stargate access was denied
     *
     * @param denyReason <p>The new reason why the stargate access was denied</p>
     */
    public void setDenyReason(String denyReason) {
        this.denyReason = denyReason;
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
